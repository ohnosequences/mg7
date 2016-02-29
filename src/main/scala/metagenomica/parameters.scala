package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import ohnosequences.flash.api._

import ohnosequences.blast.api._

import scala.util.Try


trait AnyMG7Parameters {

  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults.update(
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )

  type BlastOutRec <: AnyBlastOutputRecord.For[blastn.type]
  val  blastOutRec: BlastOutRec

  /* This is the number of reads in each chunk after the `split` step */
  // TODO: would be nice to have Nat here
  val chunkSize: Int

  val refDB: bundles.AnyBlastReferenceDB
}

abstract class MG7Parameters[
  BR <: AnyBlastOutputRecord.For[blastn.type]
](val readsLength: illumina.Length,
  val blastOutRec: BR,
  val chunkSize: Int = 5,
  val refDB: bundles.AnyBlastReferenceDB
// )(implicit
  // TODO: add a check for minimal set of properties in the record (like bitscore and sgi)
) extends AnyMG7Parameters {

  type BlastOutRec = BR
}
