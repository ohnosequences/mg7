package ohnosequences.metagenomica.loquats.counting

import ohnosequences.metagenomica._
import ohnosequences.metagenomica.loquats.assignment.dataProcessing._

import ohnosequences.metagenomica.bio4j._, taxonomyTree._, titanTaxonomyTree._

import ohnosequences.loquat.dataProcessing._
import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._
import ohnosequencesBundles.statika.Blast
import ohnosequences.blast._, api._, data._
import ohnosequences.cosas._, typeSets._, types._, properties._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import java.io.{ BufferedWriter, FileWriter, File }
import ohnosequences.fastarious._, fasta._, fastq._
import ohnosequences.blast._, api._, data._, outputFields._

import scala.util.Try


case object dataProcessing {

  case object directCountsCSV extends Data(CSVDataType, "directCounts.csv")
  case object accumulatedCountsCSV extends Data(CSVDataType, "accumulatedCounts.csv")


  case object countingDataProcessing extends DataProcessingBundle(
    bundles.bio4jTaxonomy
  )(input = lcaCSV :^: DNil,
    output = directCountsCSV :^: accumulatedCountsCSV :^: DNil
  ) {

    def instructions: AnyInstructions = say("I'm counting you!")

    def processData(
      dataMappingId: String,
      context: Context
    ): Instructions[OutputFiles] = {

      import com.github.tototoshi.csv._

      val lcaReader: CSVReader = CSVReader.open( context.file(lcaCSV).javaFile )

      val directCounts: Map[TaxID, Int] = lcaReader.iterator.toStream
        .foldLeft(Map[TaxID, Int]()) { (acc, row) =>
          // TODO: use csv api?
          val taxId = row(1)
          val current: Int = acc.get(taxId).getOrElse(0)
          acc.updated(taxId, current + 1)
        }

      lcaReader.close

      // TODO: figure out some more effective algorithm
      // TODO: split it and test separately
      val accumulatedCounts: Map[TaxID, Int] = directCounts
        .foldLeft(Map[TaxID, Int]()) { case (acc, (taxId, count)) =>
          val node: Option[TitanTaxonNode] = titanTaxonNode(bundles.bio4jTaxonomy.graph, taxId)
          val ancestors: Seq[AnyTaxonNode] = node.map{ n => pathToTheRoot(n, Seq()) }.getOrElse(Seq())

          ancestors.foldLeft(acc) { (acc, node) =>
            val current: Int = acc.get(node.id).getOrElse(0)
            acc.updated(node.id, current + count)
          }
        }

      // Now we will write these two types of result to two separate files
      val directCountsFile = context / "directCounts.csv"
      val accumulatedCountsFile = context / "accumulatedCounts.csv"

      val directCountsWriter = CSVWriter.open(directCountsFile.javaFile, append = true)
      directCounts foreach { case (taxId, count) =>
        directCountsWriter.writeRow(List(taxId, count))
      }
      directCountsWriter.close

      val accumulatedCountsWriter = CSVWriter.open(accumulatedCountsFile.javaFile, append = true)
      accumulatedCounts foreach { case (taxId, count) =>
        accumulatedCountsWriter.writeRow(List(taxId, count))
      }
      accumulatedCountsWriter.close

      success(
        s"Results are written to ...",
        directCountsCSV.inFile(directCountsFile) :~:
        accumulatedCountsCSV.inFile(accumulatedCountsFile) :~:
        ∅
      )
    }
  }

}
