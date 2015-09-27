package ohnosequences.metagenomica

import ohnosequences.cosas._, typeSets._, types._
import ops.typeSets._

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


  case object CSVDataType extends AnyDataType { val label = "csv" }

  case object lcaCSV extends Data(CSVDataType, "lca.csv")
  case object bbhCSV extends Data(CSVDataType, "bbh.csv")

  case object lcaCountsCSV extends Data(CSVDataType, "lca.counts.csv")
  case object bbhCountsCSV extends Data(CSVDataType, "bbh.counts.csv")

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

    // implicitly:
    val validBlastRecord: BlastOutRec#Properties CheckForAll ValidOutputRecordFor[blastn]

    type BlastExprType >: BlastExpressionType[blastn, BlastOutRec]
                       <: BlastExpressionType[blastn, BlastOutRec]
    lazy val blastExprType: BlastExprType = new BlastExpressionType(blastn)(blastOutRec)(validBlastRecord)

    type BlastOutType >: BlastOutputType[BlastExprType]
                      <: BlastOutputType[BlastExprType]
    val  blastOutType: BlastOutType = new BlastOutputType(blastExprType, "useless label")

    // type BlastExpr <: AnyBlastExpression {
    //   type Tpe <: AnyBlastExpressionType {
    //     type OutputRecord = BlastOutRec
    //   }
    // }
    def blastExpr(args: ValueOf[blastn.Arguments]): BlastExpression[BlastExprType]

    type BlastOut >: BlastOutput[BlastOutType]
                  <: BlastOutput[BlastOutType]
    val  blastOut: BlastOut = new BlastOutput(blastOutType, "blast.out.csv")
  }

  abstract class MetagenomicaData[
    RT <: AnyReadsType { type EndType = pairedEndType },
    BR <: AnyBlastOutputRecord
  ](val readsType: RT,
    val blastOutRec: BR
  )(implicit
    val validBlastRecord: BR#Properties CheckForAll ValidOutputRecordFor[blastn]
  ) extends AnyMetagenomicaData {

    type ReadsType = RT

    type Reads1 = PairedEnd1Fastq[ReadsType]
    type Reads2 = PairedEnd2Fastq[ReadsType]

    type Merged = MergedReads[ReadsType, Reads1, Reads2]
    type Stats  = MergedReadsStats[Merged]


    type BlastOutRec = BR

    type BlastExprType = BlastExpressionType[blastn, BlastOutRec]
    type BlastOutType  = BlastOutputType[BlastExprType]
    type BlastOut = BlastOutput[BlastOutType]
  }

}
