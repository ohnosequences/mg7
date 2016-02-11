package ohnosequences.metagenomica

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
}

abstract class MG7Parameters[
  BR <: AnyBlastOutputRecord.For[blastn.type]
](val readsLength: illumina.Length,
  val blastOutRec: BR
// )(implicit
  // TODO: add a check for minimal set of properties in the record (like bitscore and sgi)
) extends AnyMG7Parameters {

  type BlastOutRec = BR
}
