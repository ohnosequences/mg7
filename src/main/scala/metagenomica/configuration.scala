package ohnosequences.metagenomica

import ohnosequences.cosas._, typeSets._, types._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.flash.api._
import ohnosequences.flash.data._

import ohnosequences.blast._, api._, data._, outputFields._

import java.io.File
import scala.util.Try


case object configuration {

  // TODO: move it to datasets
  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })


  case object CSVDataType extends AnyDataType { val label = "fastq" }
  case object lcaCSV extends Data(CSVDataType, "lca.csv")
  case object bbhCSV extends Data(CSVDataType, "bbh.csv")

  type ID = String
  type GI = ID
  type TaxID = ID
  type ReadID = ID
  type NodeID = ID

  type LCA = Option[NodeID]
  type BBH = Option[NodeID]

  // TODO: move it somewhere up for global use
  def parseInt(str: String): Option[Int] = Try(str.toInt).toOption



  trait AnyMetagenomicaData {

    // FLASH

    type ReadsType <: AnyReadsType { type EndType = pairedEndType }
    val  readsType: ReadsType

    type Reads1 >: PairedEnd1Fastq[ReadsType]
                <: PairedEnd1Fastq[ReadsType]
    lazy val reads1: Reads1 = new PairedEnd1Fastq(readsType, "reads1.fastq.gz")

    type Reads2 >: PairedEnd2Fastq[ReadsType]
                <: PairedEnd2Fastq[ReadsType]
    lazy val reads2: Reads2 = new PairedEnd2Fastq(readsType, "reads2.fastq.gz")


    // TODO: make it free
    lazy val flashOptions = flash.defaults update (
      read_len(readsType.length.length)   :~:
      max_overlap(readsType.length.length) :~: ∅
    )

    type Merged >: MergedReads[ReadsType, Reads1, Reads2]
                <: MergedReads[ReadsType, Reads1, Reads2]
    lazy val merged: Merged = new MergedReads(readsType, reads1, reads2, flashOptions)

    type Stats >: MergedReadsStats[Merged]
               <: MergedReadsStats[Merged]
    lazy val stats: Stats = new MergedReadsStats(merged)


    // BLAST

    type BlastOutRec <: AnyBlastOutputRecord
    val  blastOutRec: BlastOutRec

    type BlastExprType >: BlastExpressionType[blastn, BlastOutRec]
                       <: BlastExpressionType[blastn, BlastOutRec]
    val  blastExprType: BlastExprType

    type BlastOutType >: BlastOutputType[BlastExprType]
                      <: BlastOutputType[BlastExprType]
    val  blastOutType: BlastOutType

    def blastExpr(args: ValueOf[blastn.Arguments]): BlastExpression[BlastExprType]

    type BlastOut >: BlastOutput[BlastOutType]
                  <: BlastOutput[BlastOutType]
    val  blastOut: BlastOut
  }

  // case object outRec extends BlastOutputRecord(
  //   qseqid    :&:
  //   qlen      :&:
  //   qstart    :&:
  //   qend      :&:
  //   sseqid    :&:
  //   slen      :&:
  //   sstart    :&:
  //   send      :&:
  //   bitscore  :&:
  //   sgi       :&: □
  // )

  // case object blastExprType extends BlastExpressionType(blastn)(outRec)

  // case object blastOutputType extends BlastOutputType(blastExprType, "blastn.blablabla")

  // private def blastExpr(args: ValueOf[blastn.Arguments]): BlastExpression[blastExprType.type] = {
  //   BlastExpression(blastExprType)(
  //     argumentValues  = args,
  //     // TODO whatever
  //     optionValues    = blastn.defaults update (
  //       num_threads(1) :~:
  //       max_target_seqs(10) :~:
  //       ohnosequences.blast.api.evalue(0.001)  :~:
  //       blastn.task(blastn.megablast) :~: ∅
  //     )
  //   )
  // }

}
