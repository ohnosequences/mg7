package ohnosequences.metagenomica.flash

import ohnosequences.loquat._, utils._, dataProcessing._
import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.flash.api._
import ohnosequences.flash.data._
import ohnosequences.cosas._, typeSets._, types._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import ohnosequencesBundles.statika.Flash


trait AnyFlashData {

  // Paired end reads as input
  type ReadsType <: AnyReadsType { type EndType = pairedEndType }
  val readsType: ReadsType

  type Reads1 <: AnyPairedEnd1Fastq { type DataType = ReadsType }
  val reads1: Reads1

  type Reads2 <: AnyPairedEnd2Fastq { type DataType = ReadsType }
  val reads2: Reads2

  type Merged <: MergedReads[ReadsType, Reads1, Reads2]
  val  merged: Merged

  type Stats <: MergedReadsStats[Merged]
  val stats: Stats

  lazy val flashOptions = flash.defaults update (
    read_len(readsType.length.length)   :~:
    max_overlap(readsType.length.length) :~: ∅
  )

}


class FlashData [
  RT <: AnyReadsType { type EndType = pairedEndType }
](val readsType: RT) extends AnyFlashData {

  type ReadsType = RT

  case object reads1_ extends PairedEnd1Fastq(readsType, "reads1.fastq.gz")
  case object reads2_ extends PairedEnd2Fastq(readsType, "reads2.fastq.gz")

  case object merged_ extends MergedReads(readsType, reads1_, reads2_, flashOptions)
  case object stats_ extends MergedReadsStats(merged_)

  type Reads1 = reads1_.type
  val  reads1 = reads1_

  type Reads2 = reads2_.type
  val  reads2 = reads2_

  type Merged = merged_.type
  val  merged = merged_

  type Stats = stats_.type
  val  stats = stats_
}
