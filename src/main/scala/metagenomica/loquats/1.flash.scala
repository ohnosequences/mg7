package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica.configuration._
import ohnosequences.metagenomica.bundles

import ohnosequences.loquat._, utils._

import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._
import ohnosequences.statika.results._

import ohnosequences.flash._
import ohnosequences.flash.api._
import ohnosequences.flash.data._

import ohnosequences.cosas._, typeSets._, types._
import ops.typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import better.files._


trait AnyFlashDataProcessing extends AnyDataProcessingBundle {

  type MD <: AnyMetagenomicaData
  val md: MD

  val bundleDependencies: List[AnyBundle] = List( bundles.flash )

  type Input = MD#Reads1 :^: MD#Reads2 :^: DNil
  type Output = readsFastq.type :^: MD#Stats :^: DNil

  def instructions: AnyInstructions = say("I'll be fast as a flash!")

  // TODO FLASh stuff change options, derived from reads type
  final def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val reads1gz: File = context.file(md.reads1: MD#Reads1)
    val reads2gz: File = context.file(md.reads2: MD#Reads2)

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
        api.input(flashInput)   :~:
        api.output(flashOutput) :~: ∅
      ),
      md.flashOptions
    )

    // run expression, hope for the best
    cmd("gunzip")(reads1gz.path.toString) -&-
    cmd("gunzip")(reads2gz.path.toString) -&-
    seqToInstructions(flashExpr.cmd) -&-
    success(
      s"FLASh merged reads from ${dataMappingId}, much success so fast",
      // (md.merged: MD#Merged).inFile(flashOutput.mergedReads)           :~:
      readsFastq.inFile(flashOutput.mergedReads.toScala) :~:
      (md.stats: MD#Stats).inFile(flashOutput.lengthNumericHistogram.toScala) :~: ∅
    )
  }
}

class FlashDataProcessing[MD0 <: AnyMetagenomicaData](val md0: MD0)(implicit
  val parseInputFiles: ParseDenotations[(MD0#Reads1 :^: MD0#Reads2 :^: DNil)#LocationsAt[FileDataLocation], File],
  val outputFilesToMap: ToMap[(readsFastq.type :^: MD0#Stats :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
) extends AnyFlashDataProcessing {
  type MD = MD0
  val  md = md0

  lazy val input: Input = (md.reads1: MD#Reads1) :^: (md.reads2: MD#Reads2) :^: DNil
  lazy val output: Output = readsFastq :^: (md.stats: MD#Stats) :^: DNil
}
