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


class FlashDataProcessing[
  RT <: AnyReadsType { type EndType = pairedEndType },
  R1 <: AnyPairedEnd1Fastq { type DataType = RT },
  R2 <: AnyPairedEnd2Fastq { type DataType = RT },
  M  <: MergedReads[RT, R1, R2],
  S  <: MergedReadsStats[M]
](val readsType: RT,
  val reads1: R1,
  val reads2: R2,
  val merged: M,
  val stats: S
)(implicit
  parseInputFiles: ParseDenotations[(R1 :^: R2 :^: DNil)#LocationsAt[FileDataLocation], File],
  outputFilesToMap: ToMap[(M :^: S :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
) extends DataProcessingBundle(bundles.flash)(
  input = reads1 :^: reads2 :^: DNil,
  output = merged :^: stats :^: DNil
)(parseInputFiles, outputFilesToMap) {

  def instructions: AnyInstructions = say("I'll be fast as a flash!")

  // TODO FLASh stuff change options, derived from reads type
  final def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val reads1gz: file = context.file(reads1)
    val reads2gz: file = context.file(reads2)

    val reads1fastq: file = reads1gz.rename( _.stripSuffix(".gz") )
    val reads2fastq: file = reads2gz.rename( _.stripSuffix(".gz") )

    // define input
    lazy val flashInput = FlashInputAt(
      new File(reads1fastq),
      new File(reads2fastq)
    )

    // define output
    lazy val flashOutput = FlashOutputAt(context / "output", prefix = "")

    lazy val flashOptions = flash.defaults update (
      read_len(readsType.length.toInt)   :~:
      max_overlap(readsType.length.toInt) :~: ∅
    )

    // the FLASh cmd we are going to run
    lazy val flashExpr = FlashExpression(flash)(
      flash.arguments(
        api.input(flashInput)   :~:
        api.output(flashOutput) :~: ∅
      ),
      flashOptions
    )

    // run expression, hope for the best
    cmd("gunzip")(reads1gz) -&-
    cmd("gunzip")(reads2gz) -&-
    seqToInstructions(flashExpr.cmd) -&-
    success(
      s"FLASh merged reads from ${dataMappingId}, much success so fast",
      merged.inFile(flashOutput.mergedReads)           :~:
      stats.inFile(flashOutput.lengthNumericHistogram) :~: ∅
    )
  }
}
