package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica._

import ohnosequences.loquat._

import ohnosequences.statika.instructions._

import ohnosequences.{ flash => f }, f.api._

import ohnosequences.cosas._, typeSets._

import ohnosequences.datasets._
import better.files._


case class flashDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle(
  bundles.flash
)(
  input = data.pairedReads1 :×: data.pairedReads2 :×: *[AnyData],
  output = data.mergedReads :×: data.flashStats :×: *[AnyData]
) {

  def instructions: AnyInstructions = say("I'll be fast as a flash!")

  // TODO FLASh stuff change options, derived from reads type
  final def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val reads1gz: File = context.file(data.pairedReads1)
    val reads2gz: File = context.file(data.pairedReads2)

    val reads1fastq: File = File(reads1gz.path.toString.stripSuffix(".gz"))
    val reads2fastq: File = File(reads2gz.path.toString.stripSuffix(".gz"))

    // define input
    lazy val flashInput = FlashInputAt(
      reads1fastq.toJava,
      reads2fastq.toJava
    )

    // define output
    lazy val flashOutput = FlashOutputAt((context / "output").toJava, prefix = "")

    // the FLASh cmd we are going to run
    lazy val flashExpr = FlashExpression(flash)(
      flash.arguments(
        f.api.input(flashInput)   :~:
        f.api.output(flashOutput) :~: ∅
      ),
      md.flashOptions
    )

    // run expression, hope for the best
    cmd("gunzip")(reads1gz.path.toString) -&-
    cmd("gunzip")(reads2gz.path.toString) -&-
    seqToInstructions(flashExpr.cmd) -&-
    success(
      s"FLASh merged reads from ${dataMappingId}, much success so fast",
      data.mergedReads.inFile(flashOutput.mergedReads.toScala) :~:
      data.flashStats.inFile(flashOutput.lengthNumericHistogram.toScala) :~: ∅
    )
  }
}
