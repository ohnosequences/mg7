package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._

import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api._
import ohnosequences.blast.api.{ outputFields => out }

import better.files._

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

  val referenceDBs: Set[AnyReferenceDB]

  // has to be provided implicitly
  implicit val argValsToSeq: BlastOptionsToSeq[BlastArgumentsVals]
  implicit val optValsToSeq: BlastOptionsToSeq[BlastCommand#OptionsVals]

  def blastExpr(inFile: File, outFile: File): BlastExpression[BlastCommand, BlastOutputRecord[BlastOutRecKeys]] =
    BlastExpression(blastCommand)(
      outputRecord = blastOutRec,
      argumentValues =
        db(referenceDBs.map(_.blastDBName)) ::
        query(inFile) ::
        ohnosequences.blast.api.out(outFile) ::
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
    row.select(outputFields.qcovs) == "100"
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
  val blastOutRec: BlastOutputRecord[BK]  = defaults.blastnOutputRecord,
  val blastOptions: BC#OptionsVals        = defaults.blastnOptions.value,
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

case object defaults {

  // We set here all options explicitly
  val blastnOptions: blastn.Options := blastn.OptionsVals =
    blastn.options(
      /* This actually depends on the workers instance type */
      num_threads(4)              ::
      blastn.task(blastn.blastn)  ::
      evalue(BigDecimal(1E-100))  ::
      /* We're going to use all hits to do global sample-coherent assignment. But not now, so no reason for this to be huge */
      max_target_seqs(150)        ::
      strand(Strands.both)        ::
      word_size(46)               ::
      show_gis(false)             ::
      ungapped(false)             ::
      penalty(-2)                 ::
      reward(1)                   ::
      /* 95% is a reasonable minimum. If it does not work, be more stringent with read preprocessing */
      perc_identity(95.0) ::
      *[AnyDenotation]
    )

  case object blastnOutputRecord extends BlastOutputRecord(
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
}
