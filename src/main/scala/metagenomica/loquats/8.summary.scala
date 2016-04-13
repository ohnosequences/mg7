package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._

import ohnosequences.loquat._

import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._

import better.files._
import com.github.tototoshi.csv._

import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph


case object summaryDataProcessing extends DataProcessingBundle()(
  input = data.summaryInput,
  output = data.summaryOutput
) {

  def countReads(file: File): Integer = {
    val source = io.Source.fromFile( file.toJava )
    val readsNumber = fasta.parseMap( source.getLines ).length
    source.close()
    readsNumber
  }

  def countLines(file: File): Integer = { file.lines.length }


  def instructions: ohnosequences.statika.AnyInstructions = say("Running summary loquat")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val summaryCSV: File = (context / "output" / "summary.csv").createIfNotExists()

    LazyTry {
      val csvWriter = csv.newWriter(summaryCSV)
      csvWriter.writeRow(csv.columnNames.statsHeader)

      // only one level in depth:
      context.inputFile(data.sampleStatsFolder).list foreach { sampleStats =>

        val csvReader = csv.newReader(sampleStats)
        val rows = csvReader.allWithHeaders().map{ _.values.toSeq }

        csvWriter.writeAll(rows)

        csvReader.close()
      }

      csvWriter.close()
    } -&-
    success(s"Summary stats are ready",
      data.summaryStatsCSV(summaryCSV) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }

}
