package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica.configuration._
import ohnosequences.metagenomica.bundles
import ohnosequences.metagenomica.bio4j._, taxonomyTree.solution, titanTaxonomyTree._

import ohnosequences.loquat.dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._

import ohnosequences.cosas._, types._, typeSets._, properties._, records._
import ops.typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.fastarious._, fasta._, fastq._
import ohnosequences.blast._, api._, data._, outputFields._

import java.io.{ BufferedWriter, FileWriter, File }
import scala.util.Try


trait AnyAssignmentDataProcessing extends AnyDataProcessingBundle {

  type MD <: AnyMetagenomicaData
  val md: MD

  type Input = MD#BlastOut :^: DNil
  type Output = lcaCSV.type :^: bbhCSV.type :^: DNil
  lazy val output = lcaCSV :^: bbhCSV :^: DNil

  val bundleDependencies: List[AnyBundle] = List[AnyBundle](
    bundles.bio4jTaxonomy,
    bundles.filteredGIs
  )

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
    val gisReader: CSVReader = CSVReader.open( bundles.filteredGIs.location )(new TSVFormat {})
    val gisMap: Map[GI, TaxID] = gisReader.iterator.map { row =>
      row(0) -> row(1)
    }.toMap
    gisReader.close

    val blastReader: CSVReader = CSVReader.open( context.file(md.blastOut: MD#BlastOut).javaFile )

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
        val nodes: List[TitanTaxonNode] = titanTaxonNodes(bundles.bio4jTaxonomy.graph, taxIds)
        // and return the taxon node ID corresponding to the read
        val lca: LCA = solution(nodes).node.map(_.id)

        Some( (readId, (lca, bbh)) )
      }
    }

    blastReader.close

    // Now we will write these two types of result to two separate files
    val lcaFile = context / "lca.csv"
    val bbhFile = context / "bbh.csv"

    val lcaWriter = CSVWriter.open(lcaFile.javaFile , append = true)
    val bbhWriter = CSVWriter.open(bbhFile.javaFile , append = true)

    assignments foreach { case (readId, (lca, bbh)) =>
      lca foreach { nodeId => lcaWriter.writeRow(List(readId, nodeId)) }
      bbh foreach { nodeId => bbhWriter.writeRow(List(readId, nodeId)) }
    }

    lcaWriter.close
    bbhWriter.close

    success(
      s"Results are written to [${lcaFile.path}] and [${bbhFile.path}]",
      lcaCSV.inFile(lcaFile) :~:
      bbhCSV.inFile(bbhFile) :~:
      ∅
    )
  }
}


class AssignmentDataProcessing[MD0 <: AnyMetagenomicaData](val md0: MD0)(implicit
  val parseInputFiles: ParseDenotations[(MD0#BlastOut :^: DNil)#LocationsAt[FileDataLocation], File],
  val outputFilesToMap: ToMap[(lcaCSV.type :^: bbhCSV.type :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
) extends AnyAssignmentDataProcessing {
  type MD = MD0
  val  md = md0

  val input = (md.blastOut: MD#BlastOut) :^: DNil
}
