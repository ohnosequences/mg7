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

  case object blastExprType extends BlastExpressionType[blastn, BlastOutRec](blastn)(blastOutRec)
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
