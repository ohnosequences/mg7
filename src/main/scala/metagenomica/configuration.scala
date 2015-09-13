package ohnosequences.metagenomica

import ohnosequences.cosas._, typeSets._, types._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.flash.api._
import ohnosequences.flash.data._

import ohnosequences.blast._, api._, data._, outputFields._

import java.io.File

case object configuration {

  // TODO: move it to datasets
  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })

  trait AnyMetagenomicaData {

    // FLASH

    type ReadsType <: AnyReadsType { type EndType = pairedEndType }
    val readsType: ReadsType

    type Reads1 <: AnyPairedEnd1Fastq { type DataType = ReadsType }
    val reads1: Reads1

    type Reads2 <: AnyPairedEnd2Fastq { type DataType = ReadsType }
    val reads2: Reads2

    type FlashInput = Reads1 :^: Reads2 :^: DNil
    val  flashInput: FlashInput

    type Merged <: MergedReads[ReadsType, Reads1, Reads2]
    val  merged: Merged

    type Stats <: MergedReadsStats[Merged]
    val stats: Stats

    type FlashOutput = Merged :^: Stats :^: DNil
    val  flashOutput: FlashOutput


    // BLAST

    type BlastRecord <: AnyBlastOutputRecord
    val  blastRecord: BlastRecord


    type BlastOutput <: AnyBlastOutput
    // {
    //   type BlastExpressionType <: AnyBlastExpressionType {
    //     type OutputRecord = BlastRecord
    //   }
    // }
    val  blastOutput: BlastOutput
  }

  abstract class MetagenomicaData [
    RT <: AnyReadsType { type EndType = pairedEndType }
  ](val readsType: RT) extends AnyMetagenomicaData {

    type ReadsType = RT

    case object reads1_ extends PairedEnd1Fastq(readsType, "reads1.fastq.gz")
    case object reads2_ extends PairedEnd2Fastq(readsType, "reads2.fastq.gz")

    type Reads1 = reads1_.type
    val  reads1 = reads1_

    type Reads2 = reads2_.type
    val  reads2 = reads2_

    val  flashInput = reads1 :^: reads2 :^: DNil

    lazy val flashOptions = flash.defaults update (
      read_len(readsType.length.toInt)   :~:
      max_overlap(readsType.length.toInt) :~: ∅
    )

    case object merged_ extends MergedReads(readsType, reads1, reads2, flashOptions)
    type Merged = merged_.type
    val  merged = merged_

    case object stats_ extends MergedReadsStats[Merged](merged)
    type Stats = stats_.type
    val  stats = stats_

    val  flashOutput = merged :^: stats :^: DNil

    // case object blastExprType extends BlastExpressionType(blastn)(blastRecord)
    // case object blastOutputType extends BlastOutputType(blastExprType, "blastn.out")
    // case object blastOutput_ extends data.BlastOutput(blastOutputType, "blast.out.csv")
    //
    // type BlastOutput = blastOutput_.type
    // val  blastOutput = blastOutput_
  }


}
