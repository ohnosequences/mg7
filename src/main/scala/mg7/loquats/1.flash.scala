package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.loquat._, utils.files._
import ohnosequences.statika._
import ohnosequences.{ flash => f }, f.api._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import java.io.File


case class flashDataProcessing(val parameters: AnyFlashParameters)
extends DataProcessingBundle(
  deps = bundles.flash
)(input  = data.flashInput,
  output = data.flashOutput
) {
  def instructions: AnyInstructions = say("I'll be fast as a flash!")

  // TODO FLASh stuff change options, derived from reads type
  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val reads1gz: File = context.inputFile(data.pairedReads1)
    val reads2gz: File = context.inputFile(data.pairedReads2)

    val reads1fastq: File = file(reads1gz.path.toString.stripSuffix(".gz"))
    val reads2fastq: File = file(reads2gz.path.toString.stripSuffix(".gz"))

    // define input
    lazy val flashInput = FlashInputAt(
      reads1fastq,
      reads2fastq
    )

    // define output
    lazy val flashOutput = FlashOutputAt((context / "output"), prefix = "")

    // the FLASh cmd we are going to run
    lazy val flashExpr = flash(
      f.api.input(flashInput)   ::
      f.api.output(flashOutput) ::
      *[AnyDenotation],
      parameters.flashOptions.value
    )

    cmd("gunzip")(reads1gz.path.toString) -&-
    cmd("gunzip")(reads2gz.path.toString) -&-
    seqToInstructions(flashExpr.toSeq)    -&-
    success(
      "FLASh merged reads, much success so fast",
      data.mergedReads(flashOutput.mergedReads) ::
      data.pair1NotMerged(flashOutput.pair1NotMerged) ::
      data.pair2NotMerged(flashOutput.pair2NotMerged) ::
      data.flashHistogram(flashOutput.lengthNumericHistogram) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
