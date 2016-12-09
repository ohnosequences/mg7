package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.ncbitaxonomy._, api.{ Taxa => TaxaOps, Taxon => _, _ }, titan._
import ohnosequences.mg7._, csv._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.blast.api._, outputFields._
import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph
import java.io.{ BufferedWriter, FileWriter, File }
import scala.util.Try

case class assignDataProcessing[P <: AnyMG7Parameters](val parameters: P) extends DataProcessingBundle(
  (ncbiTaxonomyBundle +: parameters.referenceDBs.toList): _*
)(
  input  = data.assignInput,
  output = data.assignOutput
) {
  def instructions: AnyInstructions = say("Let's see who is who!")

  // For the output fields implicits
  import parameters._

  private lazy val taxonomyGraph: TitanNCBITaxonomyGraph =
    ncbiTaxonomyBundle.graph

  type BlastRow = csv.Row[parameters.blastOutRec.Keys]

  // This iterates over reference DBs and merges their id2taxa tables in one Map
  private lazy val referenceMap: Map[ID, Taxa] = {
    val refMap: scala.collection.mutable.Map[ID, Taxa] = scala.collection.mutable.Map()

    parameters.referenceDBs.foreach { refDB =>
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

  private def taxIDsFor(id: ID): Taxa = referenceMap.get(id).getOrElse(Seq())


  private def writeResult(
    writer: csv.Writer[csv.assignment.Columns]
  )(readId: ID,
    assignments: List[(BlastRow, Taxa)],
    nodesFilter: Seq[TitanTaxon] => Seq[TitanTaxon]
  ): Unit = {

    // `pident` values of those hits that have maximum `bitscore`
    val pidents: Seq[Double] = assignments.flatMap{ case (row, _) => parseDouble(row.select(pident)) }
    // NOTE: currently we leave only hits with the same maximum pident,
    // so calculating average doesn't change anything, but it can be changed

    val nodes: Seq[TitanTaxon] = nodesFilter(
      assignments
        .flatMap(_._2).distinct // only distinct Tax IDs
        .flatMap(taxonomyGraph.getTaxon)
    )

    val lcaNode = nodes.lowestCommonAncestor(taxonomyGraph)

    import csv.columns._
    writer.addVals(
      ReadID(readId)        ::
      Taxa(lcaNode.id)      ::
      TaxName(lcaNode.name) ::
      TaxRank(lcaNode.rank) ::
      Pident(f"${pidents.average}%.2f") ::
      *[AnyDenotation]
    )
  }


  // TODO this is too big. Factor BBH and LCA into methods
  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {
    println(s"Started data processing with context: ${context.workingDir}")
    println(s"Input files: ${context.inputFiles}")

    lazy val blastReader = csv.Reader(parameters.blastOutRec.keys)(context.inputFile(data.blastChunk))

    // Outs:
    println(s"Creating output files...")
    lazy val lcaFile = (context / "output" / "lca.csv").createIfNotExists(createParents = true)
    println(s"Created output file [${lcaFile}]")
    lazy val bbhFile = (context / "output" / "bbh.csv").createIfNotExists(createParents = true)
    println(s"Created output file [${bbhFile}]")

    lazy val lcaWriter = csv.Writer(csv.assignment.columns)(lcaFile)
    lazy val bbhWriter = csv.Writer(csv.assignment.columns)(bbhFile)

    blastReader.rows
      // grouping rows by the read id
      .toStream.groupBy { _.select(qseqid) }
      // for each read evaluate LCA and BBH and write the output files
      .foreach { case (readId: ID, hits: Stream[BlastRow]) =>

        println(s"${hits.length} hits for the ${readId} read")
        // for each hit row we take the column with ID and lookup corresponding Taxas
        val allAssignments: List[(BlastRow, Taxa)] = hits.toList.map { row =>
          row -> taxIDsFor(row.select(sseqid))
        }

        println(s"${allAssignments.length} assignments for the ${readId} read")

        // LCA of all
        writeResult(lcaWriter)(readId, allAssignments, identity)

        // BBH (LCA of best hits with best ranks)
        writeResult(bbhWriter)(readId,
          // best hits are those that have maximum in the `bitscore` column
          allAssignments.maximumsBy { case (row, _) => parseLong(row.select(bitscore)).getOrElse(0L) },
          // BBH node is the lowest common ancestor of the most rank-specific nodes
          { nodes => nodes.maximumsBy(_.rankNumber) })
        println("Written results")
      }

    blastReader.close()

    lcaWriter.close()
    bbhWriter.close()

    success(s"Results are ready",
      data.lcaChunk(lcaFile.toJava) ::
      data.bbhChunk(bbhFile.toJava) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
