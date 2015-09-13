package ohnosequences.metagenomica

import ohnosequences.cosas._, typeSets._, types._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.flash.api._
import ohnosequences.flash.data._

import java.io.File

case object configuration {

  // TODO: move it to datasets
  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })

  trait MetagenomicaData {

    // Paired end reads as input
    type ReadsType <: AnyReadsType { type EndType = pairedEndType }
    val  readsType: ReadsType

    case object reads1 extends PairedEnd1Fastq(readsType, "reads1.fastq.gz"); type Reads1 = reads1.type
    case object reads2 extends PairedEnd2Fastq(readsType, "reads2.fastq.gz"); type Reads2 = reads2.type

    // TODO: leave free additional modifications
    lazy val flashOptions = flash.defaults update (
      read_len(readsType.length.toInt)    :~:
      max_overlap(readsType.length.toInt) :~: ∅
    )

    case object mergedReads extends MergedReads(readsType, reads1, reads2, flashOptions)

    case object mergedReadsStats extends MergedReadsStats(mergedReads)

  }

}
