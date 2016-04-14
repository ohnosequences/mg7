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


case class assignmentDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle(
  bio4j.taxonomyBundle,
  md.referenceDB
)(
  input = data.assignmentInput,
  output = data.assignmentOutput
) {
  // For the output fields implicits
  import md._

  lazy val taxonomyGraph: TitanNCBITaxonomyGraph = bio4j.taxonomyBundle.graph

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
            taxonomyGraph.getNode(taxId)
          }
        }

        // for each hit row we take the column with ID and lookup its TaxID
        val taxIds: Seq[TaxID] = hits.toSeq.map{ _.select(sseqid) }.flatMap(referenceMap.get)
        // then we generate Titan taxon nodes
        val nodes: Seq[TitanTaxonNode] = taxonomyGraph.getNodes(taxIds)
        // and return the taxon node ID corresponding to the read
        val lca: LCA = lowestCommonAncestor(nodes)

        (readId, (lca, bbh))
      }

    tsvReader.close
    blastReader.close

    // Now we will write these two types of result to two separate files
    val lcaFile = (context / "output" / "lca.csv").createIfNotExists()
    val bbhFile = (context / "output" / "bbh.csv").createIfNotExists()

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
      lca.foreach{ node => lcaWriter.writeRow(List(readId, node.id, node.name, node.rank)) }
      bbh.foreach{ node => bbhWriter.writeRow(List(readId, node.id, node.name, node.rank)) }
    }

    lcaWriter.close
    bbhWriter.close

    success(s"Results are ready",
      data.lcaCSV(lcaFile) ::
      data.bbhCSV(bbhFile) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
