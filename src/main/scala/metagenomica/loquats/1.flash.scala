package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.{ flash => f }, f.api._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import better.files._


case class flashDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle(
  bundles.flash
)(
  input = data.flashInput,
  output = data.flashOutput
) {

  def instructions: AnyInstructions = say("I'll be fast as a flash!")

  // TODO FLASh stuff change options, derived from reads type
  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val reads1gz: File = context.inputFile(data.pairedReads1)
    val reads2gz: File = context.inputFile(data.pairedReads2)

    val reads1fastq: File = File(reads1gz.path.toString.stripSuffix(".gz"))
    val reads2fastq: File = File(reads2gz.path.toString.stripSuffix(".gz"))

    // define input
    lazy val flashInput = FlashInputAt(
      reads1fastq,
      reads2fastq
    )

    // define output
    lazy val flashOutput = FlashOutputAt((context / "output"), prefix = "")

    // the FLASh cmd we are going to run
    lazy val flashExpr = FlashExpression(
      flash.arguments(
        f.api.input(flashInput)   ::
        f.api.output(flashOutput) ::
        *[AnyDenotation]
      ),
      md.flashOptions
    )

    // run expression, hope for the best
    cmd("gunzip")(reads1gz.path.toString) -&-
    cmd("gunzip")(reads2gz.path.toString) -&-
    seqToInstructions(flashExpr.cmd) -&-
    success(
      "FLASh merged reads, much success so fast",
      data.mergedReads(flashOutput.mergedReads) ::
      data.flashStats(flashOutput.lengthNumericHistogram) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
