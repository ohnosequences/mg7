package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api.{ outputFields => out, _ }
import better.files._


sealed trait SplitInputFormat
case object FastaInput extends SplitInputFormat
case object FastQInput extends SplitInputFormat

trait AnyMG7Parameters {

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

  val referenceDBs: Set[AnyReferenceDB]

  // has to be provided implicitly
  implicit val argValsToSeq: BlastOptionsToSeq[BlastArgumentsVals]
  implicit val optValsToSeq: BlastOptionsToSeq[BlastCommand#OptionsVals]

  def blastExpr(inFile: File, outFile: File): BlastExpression[BlastCommand, BlastOutputRecord[BlastOutRecKeys]] =
    BlastExpression(blastCommand)(
      outputRecord = blastOutRec,
      argumentValues =
        db(referenceDBs.map(_.blastDBName.toJava)) ::
        query(inFile.toJava) ::
        ohnosequences.blast.api.out(outFile.toJava) ::
        *[AnyDenotation],
      optionValues = blastOptions
    )(argValsToSeq, optValsToSeq)


  // The minimal set of the output fields neccessary for the MG7 generic code
  implicit val has_qseqid:   out.qseqid.type   isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_sseqid:   out.sseqid.type   isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_bitscore: out.bitscore.type isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_pident:   out.pident.type   isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_qcovs:    out.qcovs.type    isOneOf BlastOutRecKeys#Types#AllTypes
  implicit val has_gaps:     out.gaps.type     isOneOf BlastOutRecKeys#Types#AllTypes

  // NOTE: this is not exposed among other constructor arguments, but you can _override_ it
  def blastFilter(row: csv.Row[BlastOutRecKeys]): Boolean = defaultBlastFilter(row)

  // NOTE: this default is defined here to have has_qcovs implicit in the scope
  final def defaultBlastFilter(row: csv.Row[BlastOutRecKeys]): Boolean =
    row.select(out.qcovs) == "100"

  def pidentMaxVariation: Double = 0.0
}

abstract class MG7Parameters[
  BC <: AnyBlastCommand { type ArgumentsVals = BlastArgumentsVals },
  BK <: AnyBlastOutputFields {
    type Types <: AnyKList {
      type Bound <: AnyOutputField
      type Union <: BC#ValidOutputFields#Types#Union
    }
  }
](val splitInputFormat: SplitInputFormat,
  val splitChunkSize: Int,
  val blastCommand: BC,
  val blastOutRec: BlastOutputRecord[BK],
  val blastOptions: BC#OptionsVals,
  val referenceDBs: Set[AnyReferenceDB]
)(implicit
  val argValsToSeq: BlastOptionsToSeq[BC#ArgumentsVals],
  val optValsToSeq: BlastOptionsToSeq[BC#OptionsVals],
  val has_qseqid:   out.qseqid.type   isOneOf BK#Types#AllTypes,
  val has_sseqid:   out.sseqid.type   isOneOf BK#Types#AllTypes,
  val has_bitscore: out.bitscore.type isOneOf BK#Types#AllTypes,
  val has_pident:   out.pident.type   isOneOf BK#Types#AllTypes,
  val has_qcovs:    out.qcovs.type    isOneOf BK#Types#AllTypes,
  val has_gaps:     out.gaps.type     isOneOf BK#Types#AllTypes
) extends AnyMG7Parameters {

  type BlastCommand = BC
  type BlastOutRecKeys = BK
}


/* Additional parameters for Flash */
trait AnyFlashParameters {

  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults.update(
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )
}

case class FlashParameters(val readsLength: illumina.Length) extends AnyFlashParameters
