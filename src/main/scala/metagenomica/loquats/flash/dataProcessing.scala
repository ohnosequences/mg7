package ohnosequences.metagenomica.loquats.flash

import ohnosequences.metagenomica.configuration._
import ohnosequences.metagenomica.bundles

import ohnosequences.loquat._, utils._, dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._

import ohnosequences.flash._
import ohnosequences.flash.api._
import ohnosequences.flash.data._

import ohnosequences.cosas._, typeSets._, types._
import ops.typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import java.io.File


trait AnyFlashDataProcessing extends AnyDataProcessingBundle {

  type MD <: AnyMetagenomicaData;
  val md: MD

  val bundleDependencies: List[AnyBundle] = List( bundles.flash )

  type Input = MD#Reads1 :^: MD#Reads2 :^: DNil
  type Output = MD#Merged :^: MD#Stats :^: DNil

  def instructions: AnyInstructions = say("I'll be fast as a flash!")

  // TODO FLASh stuff change options, derived from reads type
  final def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val reads1gz: file = context.file(md.reads1: MD#Reads1)
    val reads2gz: file = context.file(md.reads2: MD#Reads2)

    val reads1fastq: file = reads1gz.rename( _.stripSuffix(".gz") )
    val reads2fastq: file = reads2gz.rename( _.stripSuffix(".gz") )

    // define input
    lazy val flashInput = FlashInputAt(
      new File(reads1fastq),
      new File(reads2fastq)
    )

    // define output
    lazy val flashOutput = FlashOutputAt(context / "output", prefix = "")

    // the FLASh cmd we are going to run
    lazy val flashExpr = FlashExpression(flash)(
      flash.arguments(
        api.input(flashInput)   :~:
        api.output(flashOutput) :~: ∅
      ),
      md.flashOptions
    )

    // run expression, hope for the best
    cmd("gunzip")(reads1gz) -&-
    cmd("gunzip")(reads2gz) -&-
    seqToInstructions(flashExpr.cmd) -&-
    success(
      s"FLASh merged reads from ${dataMappingId}, much success so fast",
      (md.merged: MD#Merged).inFile(flashOutput.mergedReads)           :~:
      (md.stats: MD#Stats).inFile(flashOutput.lengthNumericHistogram) :~: ∅
    )
  }
}

class FlashDataProcessing[MD0 <: AnyMetagenomicaData](val md0: MD0)(implicit
  val parseInputFiles: ParseDenotations[(MD0#Reads1 :^: MD0#Reads2 :^: DNil)#LocationsAt[FileDataLocation], File],
  val outputFilesToMap: ToMap[(MD0#Merged :^: MD0#Stats :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
) extends AnyFlashDataProcessing {
  type MD = MD0
  val  md = md0

  lazy val input: Input = (md.reads1: MD#Reads1) :^: (md.reads2: MD#Reads2) :^: DNil
  lazy val output: Output = (md.merged: MD#Merged) :^: (md.stats: MD#Stats) :^: DNil
}
