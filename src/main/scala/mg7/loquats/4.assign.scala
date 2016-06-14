package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.blast.api._, outputFields._

import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph

import java.io.{ BufferedWriter, FileWriter, File }
import scala.util.Try

import com.github.tototoshi.csv._


case class assignDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle()(
  input  = data.assignInput,
  output = data.assignOutput
) {
  // For the output fields implicits
  import md._

  override val bundleDependencies: List[AnyBundle] =
    bio4j.taxonomyBundle :: md.referenceDBs.toList

  private lazy val taxonomyGraph: TitanNCBITaxonomyGraph = bio4j.taxonomyBundle.graph

  type BlastRow = csv.Row[md.blastOutRec.Keys]

  // This iterates over reference DBs and merges their id2taxa tables in one Map
  private lazy val referenceMap: Map[ID, Seq[TaxID]] = {
    val refMap: scala.collection.mutable.Map[ID, Seq[TaxID]] = scala.collection.mutable.Map()

    md.referenceDBs.foreach { refDB =>
      val reader = newReader(refDB.id2taxas)
      reader.iterator.foreach { row =>
        refMap.update(
          // first column is the ID
          row(0),
          // second column is a sequence of tax IDs separated with ';'
          row(1).split(';').map(_.trim)
        )
      }
      reader.close
    }

    refMap.toMap
  }

  private def taxIDsFor(id: ID): Seq[TaxID] = referenceMap.get(id).getOrElse(Seq())


  def instructions: AnyInstructions = say("Let's see who is who!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val blastReader = csv.Reader(md.blastOutRec.keys, context.inputFile(data.blastChunk))

    // Outs:
    val lcaFile = (context / "output" / "lca.csv").createIfNotExists()
    val bbhFile = (context / "output" / "bbh.csv").createIfNotExists()
    val lcaWriter = csv.newWriter(lcaFile)
    val bbhWriter = csv.newWriter(bbhFile)

    val lostInMappingFile = (context / "output" / "lost.in-mapping").createIfNotExists()
    val lostInBio4jFile   = (context / "output" / "lost.in-bio4j").createIfNotExists()

    blastReader.rows
      // grouping rows by the read id
      .toStream.groupBy { _.select(qseqid) }
      // for each read evaluate LCA and BBH and write the output files
      .foreach { case (readId: ID, hits: Stream[BlastRow]) =>

        // for each hit row we take the column with ID and lookup corresponding TaxIDs
        val assignments: List[(BlastRow, Seq[TaxID])] = hits.toList.map { row =>
          row -> taxIDsFor(row.select(sseqid))
        }

        //// Best Blast Hit ////

        // best hits are those that have maximum in the `bitscore` column
        val bbhHits: List[(BlastRow, Seq[TaxID])] = maximums(assignments) { case (row, _) =>
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


        // writing results
        bbhWriter.writeRow(List(readId, bbhNode.id, bbhNode.name, bbhNode.rank, f"${averageOf(bbhPidents)}%.2f"))
        lcaWriter.writeRow(List(readId, lcaNode.id, lcaNode.name, lcaNode.rank, f"${averageOf(allPidents)}%.2f"))
      }

    blastReader.close

    lcaWriter.close
    bbhWriter.close

    success(s"Results are ready",
      data.lcaChunk(lcaFile) ::
      data.bbhChunk(bbhFile) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
