package ohnosequences.mg7.loquats

import ohnosequences.mg7._, csv._, bio4j._, taxonomyTree._, titanTaxonomyTree._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.blast.api._, outputFields._
import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph
import java.io.{ BufferedWriter, FileWriter, File }
import scala.util.Try


case class assignDataProcessing[MD <: AnyMG7Parameters](val md: MD) extends DataProcessingBundle(
  (bio4j.taxonomyBundle :: md.referenceDBs.toList): _*
)(
  input  = data.assignInput,
  output = data.assignOutput
) {
  // For the output fields implicits
  import md._

  private lazy val taxonomyGraph: TitanNCBITaxonomyGraph = bio4j.taxonomyBundle.graph

  type BlastRow = csv.Row[md.blastOutRec.Keys]

  // This iterates over reference DBs and merges their id2taxa tables in one Map
  private lazy val referenceMap: Map[ID, Seq[Taxa]] = {
    val refMap: scala.collection.mutable.Map[ID, Seq[Taxa]] = scala.collection.mutable.Map()

    md.referenceDBs.foreach { refDB =>
      val reader = csv.Reader(csv.refDB.columns)(refDB.id2taxas)
      reader.rows.foreach { row =>
        refMap.update(
          row.select(columns.ReadID),
          // second column is a sequence of tax IDs separated with ';'
          row.select(columns.Taxa).split(';').map(_.trim)
        )
      }
      reader.close()
    }

    refMap.toMap
  }

  private def taxIDsFor(id: ID): Seq[Taxa] = referenceMap.get(id).getOrElse(Seq())


  def instructions: AnyInstructions = say("Let's see who is who!")

  // TODO this is too big. Factor BBH and LCA into methods
  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val blastReader = csv.Reader(md.blastOutRec.keys)(context.inputFile(data.blastChunk))

    // Outs:
    val lcaFile = (context / "output" / "lca.csv").createIfNotExists()
    val bbhFile = (context / "output" / "bbh.csv").createIfNotExists()
    val lcaWriter = csv.Writer(csv.assignment.columns)(lcaFile)
    val bbhWriter = csv.Writer(csv.assignment.columns)(bbhFile)

    val lostInMappingFile = (context / "output" / "lost.in-mapping").createIfNotExists()
    val lostInBio4jFile   = (context / "output" / "lost.in-bio4j").createIfNotExists()

    blastReader.rows
      // grouping rows by the read id
      .toStream.groupBy { _.select(qseqid) }
      // for each read evaluate LCA and BBH and write the output files
      .foreach { case (readId: ID, hits: Stream[BlastRow]) =>

        // for each hit row we take the column with ID and lookup corresponding Taxas
        val assignments: List[(BlastRow, Seq[Taxa])] = hits.toList.map { row =>
          row -> taxIDsFor(row.select(sseqid))
        }

        //// Best Blast Hit ////

        // best hits are those that have maximum in the `bitscore` column
        val bbhHits: List[(BlastRow, Seq[Taxa])] = maximums(assignments) { case (row, _) =>
          parseLong(row.select(bitscore)).getOrElse(0L)
        }

        // `pident` values of those hits that have maximum `bitscore`
        val bbhPidents: Seq[Double] = bbhHits.flatMap{ case (row, _) => parseDouble(row.select(pident)) }

        // nodes corresponding to the max-bitscore hits
        val bbhNodes: Seq[TitanTaxonNode] = bbhHits
          .flatMap(_._2).distinct // only distinct Tax IDs
          .flatMap(taxonomyGraph.getNode)

        // BBH node is the lowest common ancestor of the most rank-specific nodes
        val bbhNode: BBH = lowestCommonAncestor(
          maximums(bbhNodes) { _.rankNumber }
        ).getOrElse(
          // NOTE: this shouldn't ever happen, so we throw an error here
          sys.error("Failed to compute BBH; something is broken")
        )


        //// Lowest Common Ancestor ////

        // NOTE: currently we leave only hits with the same maximum pident,
        // so calculating average doesn't change anything, but it can be changed
        val allPidents: Seq[Double] = hits.flatMap{ row => parseDouble(row.select(pident)) }

        // nodes corresponding to all hits
        val allNodes: Seq[TitanTaxonNode] = assignments
          .flatMap(_._2).distinct // only distinct Tax IDs
          .flatMap(taxonomyGraph.getNode)

        val lcaNode: LCA = lowestCommonAncestor(allNodes).getOrElse(
          // NOTE: this shouldn't ever happen, so we throw an error here
          sys.error("Failed to compute LCA; something is broken")
        )

        import csv.columns._

        // writing results
        bbhWriter.addVals(
          ReadID(readId)        ::
          Taxa(bbhNode.id)      ::
          TaxName(bbhNode.name) ::
          TaxRank(bbhNode.rank) ::
          Pident(f"${averageOf(bbhPidents)}%.2f") ::
          *[AnyDenotation]
        )

        lcaWriter.addVals(
          ReadID(readId)        ::
          Taxa(lcaNode.id)      ::
          TaxName(lcaNode.name) ::
          TaxRank(lcaNode.rank) ::
          Pident(f"${averageOf(allPidents)}%.2f") ::
          *[AnyDenotation]
        )
      }

    blastReader.close()

    lcaWriter.close()
    bbhWriter.close()

    success(s"Results are ready",
      data.lcaChunk(lcaFile) ::
      data.bbhChunk(bbhFile) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
