package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica._
import ohnosequences.metagenomica.bio4j._, taxonomyTree.solution, titanTaxonomyTree._

import ohnosequences.loquat._

import ohnosequences.statika.instructions._

import ohnosequences.cosas._, typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._

import ohnosequences.{ blast => b }, b.api._, outputFields._

import java.io.{ BufferedWriter, FileWriter, File }
import scala.util.Try


case class assignmentDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle(
  bundles.bio4jNCBITaxonomy,
  bundles.filteredGIs
)(
  input = data.blastResult :^: DNil,
  output = data.lcaCSV :^: data.bbhCSV :^: DNil
) {

  def instructions: AnyInstructions = say("Let's see who is who!")

  // this method looks up particular column by its header
  private def column(row: Seq[String], header: AnyOutputField): Option[String] =
    md.blastOutRec.headers.zip(row).toMap.get(header.label)

  def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    import com.github.tototoshi.csv._

    // Reading TSV file with mapping gis-taxIds
    val gisReader: CSVReader = CSVReader.open( bundles.filteredGIs.location.toJava )(new TSVFormat {})
    val gisMap: Map[GI, TaxID] = gisReader.iterator.map { row =>
      row(0) -> row(1)
    }.toMap
    gisReader.close

    val blastReader: CSVReader = CSVReader.open( context.file(data.blastResult).toJava )

    val assignments: Map[ReadID, (LCA, BBH)] = blastReader.iterator.toStream
      .groupBy { row =>
      // grouping rows by the read id
      md.blastOutRec.headers.zip(row).toMap.get(qseqid.label)
    } flatMap {
      case (None, _) => None
      case (Some(readId), hits) => {

        val bbh: BBH =
          // this shouldn't happen, but let's be careful
          if (hits.isEmpty) None
          else {
            // best blast score is just a maximum in the `bitscore` column
            val maxRow: Seq[String] = hits.maxBy { row: Seq[String] =>
              column(row, bitscore).flatMap(parseInt).getOrElse(0)
            }
            column(maxRow, sgi).flatMap(gisMap.get)
          }

        // for each hit row we take the column with GI and lookup its TaxID
        val taxIds: List[TaxID] = hits.toList.flatMap(column(_, sgi)).flatMap(gisMap.get)
        // then we generate Titan taxon nodes
        val nodes: List[TitanTaxonNode] = titanTaxonNodes(bundles.bio4jNCBITaxonomy.graph, taxIds)
        // and return the taxon node ID corresponding to the read
        val lca: LCA = solution(nodes).node.map(_.id)

        Some( (readId, (lca, bbh)) )
      }
    }

    blastReader.close

    // Now we will write these two types of result to two separate files
    val lcaFile = context / "lca.csv"
    val bbhFile = context / "bbh.csv"

    val lcaWriter = CSVWriter.open(lcaFile.toJava , append = true)
    val bbhWriter = CSVWriter.open(bbhFile.toJava , append = true)

    assignments foreach { case (readId, (lca, bbh)) =>
      lca foreach { nodeId => lcaWriter.writeRow(List(readId, nodeId)) }
      bbh foreach { nodeId => bbhWriter.writeRow(List(readId, nodeId)) }
    }

    lcaWriter.close
    bbhWriter.close

    success(
      s"Results are written to [${lcaFile.path}] and [${bbhFile.path}]",
      data.lcaCSV.inFile(lcaFile) :~:
      data.bbhCSV.inFile(bbhFile) :~:
      ∅
    )
  }
}
