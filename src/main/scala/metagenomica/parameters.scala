package ohnosequences.metagenomica

import ohnosequences.cosas._, types._, klists._, fns._

import ohnosequences.datasets._

import ohnosequences.flash.api._

import ohnosequences.{ blast => b }, b.api._

import scala.util.Try


trait AnyMG7Parameters {

  // FLASH

  // type ReadsType <: AnyReadsType { type EndType = pairedEndType }
  // val  readsType: ReadsType
  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults update (
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )

  // BLAST

  type BlastOutRec <: AnyBlastOutputRecord
  val  blastOutRec: BlastOutRec

  // implicitly:
  // val validBlastRecord: all[ValidOutputRecordFor[blastn]] isTrueOn BlastOutRec#Properties

  // type BlastExprType = BlastExpressionType[blastn, BlastOutRec]
  case object blastExprType extends BlastExpressionType[blastn, BlastOutRec](blastn)(blastOutRec)
  type BlastExprType = blastExprType.type

  // type BlastOutType = BlastOutputType[BlastExprType]
  // val  blastOutType: BlastOutType = new BlastOutputType(blastExprType, "useless label")

  def blastExpr[As <: blastn.Arguments#Raw](args: blastn.Arguments := As): AnyBlastExpression = {
    BlastExpression(blastExprType)(
      argumentValues = args,
      // TODO: should it be configurable?
      optionValues   = blastn.defaults
      // FIXME: uncomment it
      // update (
      //   num_threads(1) ::
      //   word_size(42) ::
      //   max_target_seqs(10) ::
      //   ohnosequences.blast.api.evalue(0.001)  ::
      //   blastn.task(blastn.megablast) ::
      //   *[AnyDenotation]
      // )
    )
  }
}

abstract class MG7Parameters[
  BR <: AnyBlastOutputRecord
](val readsLength: illumina.Length,
  val blastOutRec: BR
// )(implicit
  // TODO: add a check for minimal set of properties in the record (like bitscore and sgi)
  // val validBlastRecord: all[ValidOutputRecordFor[blastn]] isTrueOn BR#Properties
) extends AnyMG7Parameters {

  // type ReadsType = RT

  type BlastOutRec = BR
}
