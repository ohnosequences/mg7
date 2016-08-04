package ohnosequences.mg7.loquats

import ohnosequences.mg7._, csv._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._
import better.files._
import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph

case object summaryDataProcessing extends DataProcessingBundle()(
  input   = data.summaryInput,
  output  = data.summaryOutput
)
{

  def instructions: ohnosequences.statika.AnyInstructions = say("Running summary loquat")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val summaryCSV: File = (context / "output" / "summary.csv").createIfNotExists()

    LazyTry {
      val csvWriter = csv.Writer(stats.columns)(summaryCSV)
      csvWriter.writeHeader()

      // only one level in depth:
      context.inputFile(data.sampleStatsFolder).list.foreach { sampleStatsFile =>

        // an ugly way to drop the header
        val row = csv.Reader(csv.stats.columns)(sampleStatsFile).rows.drop(1).next()

        csvWriter.addRow(row)
      }

      csvWriter.close()
    } -&-
    success(s"Summary stats are ready",
      data.summaryStatsCSV(summaryCSV) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }

}
