package ohnosequences.mg7.loquats

import ohnosequences.mg7._, csv._, columns._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._, fastq._
import better.files._
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
      val statsWriter = csv.Writer(stats.columns)(statsCSV)

      statsWriter.writeHeader()

      statsWriter.addVals(
        SampleID(sampleID) ::
        InputPairs(  countReads( parseFastqDropErrors, reads1fastq ).toString) ::
        Merged(      countReads( parseFastqDropErrors, context.inputFile(data.mergedReads) ).toString) ::
        NotMerged(   countReads( parseFastqDropErrors, context.inputFile(data.pair1NotMerged) ).toString) ::
        NoBlasthits( countReads( parseFastaDropErrors, context.inputFile(data.blastNoHits) ).toString) ::
        *[AnyDenotation]
      )

      statsWriter.close()
    } -&-
    success(s"Stats for the [${sampleID}] are ready",
      data.sampleStatsCSV(statsCSV) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }

}
