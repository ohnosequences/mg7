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
  md.referenceDB
)(
  input = data.assignmentInput,
  output = data.assignmentOutput
) {
  // For the output fields implicits
  import md._

  def instructions: AnyInstructions = say("Let's see who is who!")


  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val tsvReader = CSVReader.open( md.referenceDB.id2taxa.toJava )(csv.UnixTSVFormat)

    val referenceMap: Map[ID, TaxID] = tsvReader.iterator.map{ row => row(0) -> row(1) }.toMap

    val blastReader = csv.Reader(md.blastOutRec.keys, context.inputFile(data.blastResult))

    val assignments: Map[ReadID, (LCA, BBH)] = blastReader.rows
      // grouping rows by the read id
      .toStream.groupBy { _.select(qseqid) }
      .map { case (readId, hits) =>

        val bbh: BBH = {
          // best blast score is just a maximum in the `bitscore` column
          val maxRow = hits.maxBy { row =>
            parseInt(row.select(bitscore)).getOrElse(0)
          }
          referenceMap.get(maxRow.select(sseqid)).flatMap { taxId =>
            titanTaxonNode(bundles.bio4jNCBITaxonomy.graph, taxId)
          }
        }

        // for each hit row we take the column with ID and lookup its TaxID
        val taxIds: List[TaxID] = hits.toList.map{ _.select(sseqid) }.flatMap(referenceMap.get)
        // then we generate Titan taxon nodes
        val nodes: List[TitanTaxonNode] = titanTaxonNodes(bundles.bio4jNCBITaxonomy.graph, taxIds)
        // and return the taxon node ID corresponding to the read
        val lca: LCA = solution(nodes).node

        (readId, (lca, bbh))
      }

    tsvReader.close
    blastReader.close

    // Now we will write these two types of result to two separate files
    val lcaFile    = (context / "output" / "lca.csv").createIfNotExists()
    val bbhFile    = (context / "output" / "bbh.csv").createIfNotExists()
    val no_lcaFile = (context / "output" / "lca.not-assigned").createIfNotExists()
    val no_bbhFile = (context / "output" / "bbh.not-assigned").createIfNotExists()


    val lcaWriter = csv.newWriter(lcaFile)
    val bbhWriter = csv.newWriter(bbhFile)

    // writing headers first:
    val header = List(
      csv.columnNames.ReadID,
      csv.columnNames.TaxID,
      csv.columnNames.TaxName,
      csv.columnNames.TaxRank
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
