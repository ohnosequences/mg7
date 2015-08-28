package ohnosequences.metagenomica

import ohnosequences.loquat.instructions._
import ohnosequences.statika.instructions._
import ohnosequences.flash._, api._, data._
import ohnosequences.cosas._, typeSets._, types._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import java.io.File

case object flashInstructions {

  trait AnyFlashInstructions extends AnyInstructionsBundle { instr =>

    // Paired end reads as input
    type ReadsType <: AnyReadsType { type EndType = pairedEndType }
    val readsType: ReadsType

    type Reads1 <: AnyPairedEnd1Fastq { type DataType = instr.ReadsType }
    val reads1: Reads1

    type Reads2 <: AnyPairedEnd2Fastq { type DataType = instr.ReadsType }
    val reads2: Reads2

    // TODO FLASh stuff change options, derived from reads type
    lazy val flashOptions = flash.defaults update (
      read_len(readsType.length.length)   :~:
      maxOverlap(readsType.length.length) :~: ∅
    )

    type Merged = MergedReads[ReadsType, Reads1, Reads2]
    lazy val merged: Merged = new MergedReads(readsType, reads1, reads2, flashOptions)

    type Stats = MergedReadsStats[Merged]
    val stats: Stats = new MergedReadsStats(merged)

    type Input  = Reads1 :^: Reads2 :^: DNil
    lazy val input = reads1 :^: reads2 :^: DNil
    type Output = Merged :^: Stats :^: DNil
    lazy val output = merged :^: stats :^: DNil

    final def processData(
      dataMappingId: String,
      inputFiles: InputFiles
    ): AnyInstructions.withOut[OutputFiles] = {

      // define input
      lazy val flashInput = FlashInputAt(
        getFile(inputFiles, reads1),
        getFile(inputFiles, reads2)
      )

      // define output
      lazy val flashOutput = FlashOutputAt(new File("."), "")

      // the FLASh cmd we are going to run
      lazy val flashExpr = FlashExpression(flash)(
        flash.arguments(
          api.input(flashInput)     :~:
          api.output(flashOutput)   :~: ∅
        ),
        flashOptions
      )

      // run expression, hope for the best
      flashExpr.cmd -&- success(
        s"FLASh merged reads from ${dataMappingId}, much success so fast",
        (merged inFile flashOutput.mergedReads)             :~:
        (stats  inFile flashOutput.lengthNumericHistogram)  :~: ∅
      )
    }
  }

}
