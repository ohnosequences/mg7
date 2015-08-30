package ohnosequences.metagenomica.flash

import ohnosequences.loquat._, utils._, dataProcessing._
import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.flash.api._
import ohnosequences.flash.data._
import ohnosequences.cosas._, typeSets._, types._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import ohnosequencesBundles.statika.Flash
// import java.io.File


case object data {

  trait AnyFlashData {

    // Paired end reads as input
    type ReadsType <: AnyReadsType { type EndType = pairedEndType }
    val readsType: ReadsType

    type Reads1 <: AnyPairedEnd1Fastq { type DataType = ReadsType }
    val reads1: Reads1

    type Reads2 <: AnyPairedEnd2Fastq { type DataType = ReadsType }
    val reads2: Reads2

    type Merged = MergedReads[ReadsType, Reads1, Reads2]
    val  merged: Merged
    // case object merged extends MergedReads(readsType, reads1, reads2, flashOptions)

    type Stats = MergedReadsStats[Merged]
    lazy val stats: Stats = new MergedReadsStats(merged)
    // case object stats extends MergedReadsStats(merged)

    type     Input = Reads1 :^: Reads2 :^: DNil
    lazy val input = reads1 :^: reads2 :^: DNil

    type     Output = Merged :^: Stats :^: DNil
    lazy val output = merged :^: stats :^: DNil

    lazy val flashOptions = flash.defaults update (
      read_len(readsType.length.length)   :~:
      maxOverlap(readsType.length.length) :~: ∅
    )
  }


  class FlashData [
    RT <: AnyReadsType { type EndType = pairedEndType },
    R1 <: AnyPairedEnd1Fastq { type DataType = RT },
    R2 <: AnyPairedEnd2Fastq { type DataType = RT }
  ](val readsType: RT,
    val reads1: R1,
    val reads2: R2
  ) extends AnyFlashData {

    type ReadsType = R1#DataType
    type Reads1 = R1
    type Reads2 = R2

    lazy val merged: Merged = new MergedReads(readsType, reads1, reads2, flashOptions)
  }
}
