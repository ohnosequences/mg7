package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._, fastq._
import better.files._
import com.github.tototoshi.csv._
import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph

case object statsDataProcessing extends DataProcessingBundle()(
  input   = data.statsInput,
  output  = data.statsOutput
)
{

  def countReads(
    parser: Iterator[String] => Iterator[Any],
    file: File
  ): Integer = {
    parser( file.lines ).length
  }

  def instructions: ohnosequences.statika.AnyInstructions = say("Running stats loquat")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val statsCSV: File = (context / "output" / "stats.csv").createIfNotExists()
    val sampleID: String = context.inputFile(data.sampleID).contentAsString

    val reads1gz: File = context.inputFile(data.pairedReads1)
    val reads1fastq: File = File(reads1gz.path.toString.stripSuffix(".gz"))

    cmd("gunzip")(reads1gz.path.toString) -&-
    LazyTry {
      val csvWriter = csv.newWriter(statsCSV)

      // TODO the whole method is a bit primitive
      // header:
      csvWriter.writeRow(csv.statsHeader)

      // values:
      // NOTE: careful, the order has to coincide with the header
      // TODO: use csv.Row here
      val stats: Seq[String] = Seq(
        sampleID,

        countReads( parseFastqDropErrors, reads1fastq ).toString,
        countReads( parseFastqDropErrors, context.inputFile(data.mergedReads) ).toString,
        countReads( parseFastqDropErrors, context.inputFile(data.pair1NotMerged) ).toString,

        countReads( parseFastaDropErrors, context.inputFile(data.blastNoHits) ).toString
      )

      csvWriter.writeRow(stats)

      csvWriter.close()
    } -&-
    success(s"Stats for the [${sampleID}] are ready",
      data.sampleStatsCSV(statsCSV) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }

}
