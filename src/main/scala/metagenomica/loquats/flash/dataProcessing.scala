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


class FlashDataProcessing[D <: AnyFlashData](val data: D)(implicit
  parseInputFiles: ParseDenotations[(D#Reads1 :^: D#Reads2 :^: DNil)#LocationsAt[FileDataLocation], File],
  outputFilesToMap: ToMap[(D#Merged :^: D#Stats :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
  // parseInputFiles: ParseDenotations[(data.reads1.type :^: data.reads2.type :^: DNil)#LocationsAt[FileDataLocation], File],
  // outputFilesToMap: ToMap[(data.merged.type :^: data.stats.type :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
) extends DataProcessingBundle(bundles.flash)(
  input = (data.reads1: D#Reads1) :^: (data.reads2: D#Reads2) :^: DNil,
  output = (data.merged: D#Merged) :^: (data.stats: D#Stats) :^: DNil
  // input = (data.reads1 :^: data.reads2 :^: DNil): (D#Reads1 :^: D#Reads2 :^: DNil),
  // output = (data.merged :^: data.stats :^: DNil): (D#Merged :^: D#Stats :^: DNil)
)(parseInputFiles, outputFilesToMap) {

  def instructions: AnyInstructions = say("I'll be fast a a flash!")

  // TODO FLASh stuff change options, derived from reads type
  final def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val reads1gz: file = context.file(data.reads1: D#Reads1)
    val reads2gz: file = context.file(data.reads2: D#Reads2)

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
      data.flashOptions
    )

    // run expression, hope for the best
    cmd("gunzip")(reads1gz) -&-
    cmd("gunzip")(reads2gz) -&-
    seqToInstructions(flashExpr.cmd) -&-
    success(
      s"FLASh merged reads from ${dataMappingId}, much success so fast",
      (data.merged: D#Merged).inFile(flashOutput.mergedReads)           :~:
      (data.stats: D#Stats).inFile(flashOutput.lengthNumericHistogram) :~: ∅
    )
  }
}

// class FlashDataProcessing[
//   D <: AnyFlashData
// ](val data: D)(implicit
//   val parseInputFiles: ParseDenotations[(D#Reads1 :^: D#Reads2 :^: DNil)#LocationsAt[FileDataLocation], File],
//   val outputFilesToMap: ToMap[(D#Merged :^: D#Stats :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
// ) extends AnyFlashDataProcessing {
//
//   type Data = D
//
//   type Input = D#Reads1 :^: D#Reads2 :^: DNil
//   type Output = D#Merged :^: D#Stats :^: DNil
//
//   lazy val input = (data.reads1: D#Reads1) :^: (data.reads2: D#Reads2) :^: DNil
//   lazy val output = (data.merged: D#Merged) :^: (data.stats: D#Stats) :^: DNil
// }
