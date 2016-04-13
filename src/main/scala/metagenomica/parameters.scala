package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._

import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api._
import ohnosequences.blast.api.{ outputFields => out }

import era7bio.db._


sealed trait SplitInputFormat
case object FastaInput extends SplitInputFormat
case object FastQInput extends SplitInputFormat

trait AnyMG7Parameters {

  val outputS3Folder: (SampleID, StepName) => S3Folder

  /* Flash parameters */
  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults.update(
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )


  /* Split parameters */
  /* This is the number of reads in each chunk after the `split` step */
  // TODO: would be nice to have Nat here
  val splitChunkSize: Int
  val splitInputFormat: SplitInputFormat

  /* BLAST parameters */
  type BlastCommand <: AnyBlastCommand {
    type ArgumentsVals = BlastArgumentsVals
  }
  val  blastCommand: BlastCommand

  type BlastOutRecKeys <: AnyBlastOutputFields{
    type Types <: AnyKList {
      type Bound <: AnyOutputField
      type Union <: BlastCommand#ValidOutputFields#Types#Union
    }
  }
  val  blastOutRec: BlastOutputRecord[BlastOutRecKeys]

  val blastOptions: BlastCommand#OptionsVals

  val referenceDB: AnyBlastDBRelease

  implicit val argValsToSeq: BlastOptionsToSeq[BlastArgumentsVals] = implicitly[BlastOptionsToSeq[BlastArgumentsVals]]
  implicit val optValsToSeq: BlastOptionsToSeq[BlastCommand#OptionsVals] // has to be provided implicitly

  // The minimal set of the output fields neccessary for the MG7 generic code
  implicit val has_qseqid:   out.qseqid.type   isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_sseqid:   out.sseqid.type   isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_bitscore: out.bitscore.type isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_pident:   out.pident.type   isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_qcovs:    out.qcovs.type    isOneOf BlastOutRecKeys#Types#AllTypes

  // NOTE: this is not exposed among other constructor arguments, but you can _override_ it
  val blastFilter: csv.Row[BlastOutRecKeys] => Boolean = defaultBlastFilter

  // NOTE: this default is defined here to have has_qcovs implicit in the scope
  val defaultBlastFilter: csv.Row[BlastOutRecKeys] => Boolean = { row =>

    val qcovs: String = row.select(outputFields.qcovs)
    parseDouble(qcovs).map(_ > 98.0).getOrElse(false)
  }
}

abstract class MG7Parameters[
  BC <: AnyBlastCommand { type ArgumentsVals = BlastArgumentsVals },
  BK <: AnyBlastOutputFields {
    type Types <: AnyKList {
      type Bound <: AnyOutputField
      type Union <: BC#ValidOutputFields#Types#Union
    }
  }
](val outputS3Folder: (SampleID, StepName) => S3Folder,
  val readsLength: illumina.Length,
  val splitInputFormat: SplitInputFormat = FastQInput,
  val splitChunkSize: Int = 10,
  val blastCommand: BC = blastn,
  val blastOutRec: BlastOutputRecord[BK]  = defaultBlastOutRec,
  val blastOptions: BC#OptionsVals        = defaultBlastnOptions.value,
  val referenceDB: AnyBlastDBRelease
)(implicit
  val optValsToSeq: BlastOptionsToSeq[BC#OptionsVals],
  val has_qseqid:   out.qseqid.type   isOneOf BK#Types#AllTypes,
  val has_sseqid:   out.sseqid.type   isOneOf BK#Types#AllTypes,
  val has_bitscore: out.bitscore.type isOneOf BK#Types#AllTypes,
  val has_pident:   out.pident.type   isOneOf BK#Types#AllTypes,
  val has_qcovs:    out.qcovs.type    isOneOf BK#Types#AllTypes
) extends AnyMG7Parameters {

  type BlastCommand = BC
  type BlastOutRecKeys = BK
}


import ohnosequences.blast.api.{ outputFields => out }
case object defaultBlastOutRec extends BlastOutputRecord(
  // query
  out.qseqid      :×:
  out.qstart      :×:
  out.qend        :×:
  out.qlen        :×:
  // reference
  out.sseqid      :×:
  out.sstart      :×:
  out.send        :×:
  out.slen        :×:
  // alignment
  out.evalue      :×:
  out.score       :×:
  out.bitscore    :×:
  out.length      :×:
  out.pident      :×:
  out.mismatch    :×:
  out.gaps        :×:
  out.gapopen     :×:
  out.qcovs       :×:
  |[AnyOutputField]
)
