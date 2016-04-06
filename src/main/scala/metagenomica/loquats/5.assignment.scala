package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.mg7.bio4j._, taxonomyTree.solution, titanTaxonomyTree._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import ohnosequences.{ blast => b }, b.api._, outputFields._

import java.io.{ BufferedWriter, FileWriter, File }
import scala.util.Try

import com.github.tototoshi.csv._

case class assignmentDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle(
  bundles.bio4jNCBITaxonomy,
  md.referenceDB.idsMap
)(
  input = data.assignmentInput,
  output = data.assignmentOutput
) {

  def instructions: AnyInstructions = say("Let's see who is who!")

  private val headers: Seq[String] = md.blastOutRec.keys.types.asList.map{ _.label }

  // this method looks up particular column by its header
  private def column(row: Seq[String], header: AnyOutputField): Option[String] =
    headers.zip(row).toMap.get(header.label)

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val referenceMapping: Map[ID, TaxID] = md.referenceDB.idsMap.mapping

    val blastReader: CSVReader = CSVReader.open( context.inputFile(data.blastResult).toJava )

    val assignments: Map[ReadID, (LCA, BBH)] = blastReader.iterator.toStream
      // grouping rows by the read id
      .groupBy { column(_, qseqid) }
      .flatMap {
        case (None, _) => None
        case (_, hits) if hits.isEmpty => None
        case (Some(readId), hits) => {

          val bbh: BBH = {
            // best blast score is just a maximum in the `bitscore` column
            val maxRow: Seq[String] = hits.maxBy { row: Seq[String] =>
              column(row, bitscore).flatMap(parseInt).getOrElse(0)
            }
            column(maxRow, sseqid).flatMap(referenceMapping.get).flatMap { taxId =>
              titanTaxonNode(bundles.bio4jNCBITaxonomy.graph, taxId)
            }
          }

          // for each hit row we take the column with ID and lookup its TaxID
          val taxIds: List[TaxID] = hits.toList.flatMap(column(_, sseqid)).flatMap(referenceMapping.get)
          // then we generate Titan taxon nodes
          val nodes: List[TitanTaxonNode] = titanTaxonNodes(bundles.bio4jNCBITaxonomy.graph, taxIds)
          // and return the taxon node ID corresponding to the read
          val lca: LCA = solution(nodes).node //.map(_.id)

          Some( (readId, (lca, bbh)) )
        }
      }

    blastReader.close

    // Now we will write these two types of result to two separate files
    val lcaFile    = (context / "output" / "lca.csv").createIfNotExists()
    val bbhFile    = (context / "output" / "bbh.csv").createIfNotExists()
    val no_lcaFile = (context / "output" / "lca.not-assigned").createIfNotExists()
    val no_bbhFile = (context / "output" / "bbh.not-assigned").createIfNotExists()


    val lcaWriter = newCSVWriter(lcaFile)
    val bbhWriter = newCSVWriter(bbhFile)

    // writing headers first:
    val header = List(
      columnNames.ReadID,
      columnNames.TaxID,
      columnNames.TaxName,
      columnNames.TaxRank
    )
    lcaWriter.writeRow(header)
    bbhWriter.writeRow(header)

    assignments foreach { case (readId, (lca, bbh)) =>
      lca match {
        case Some(node) => lcaWriter.writeRow(List(readId, node.id, node.name, node.rank))
        // TODO: it should also write one of the subject sequences ID
        case None => no_lcaFile.appendLine(readId)
      }
      bbh match {
        case Some(node) => bbhWriter.writeRow(List(readId, node.id, node.name, node.rank))
        // TODO: it should also write one of the subject sequences ID
        case None => no_bbhFile.appendLine(readId)
      }
    }

    lcaWriter.close
    bbhWriter.close

    success(s"Results are ready",
      // LCA
      data.lcaCSV(lcaFile) ::
      data.lcaNotAssigned(no_lcaFile) ::
      // BBH
      data.bbhCSV(bbhFile) ::
      data.bbhNotAssigned(no_bbhFile) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
