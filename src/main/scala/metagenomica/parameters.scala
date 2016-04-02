package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._

import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api._

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

  type BlastOutRec <: AnyBlastOutputRecord.For[BlastCommand]
  val  blastOutRec: BlastOutRec

  val blastOptions: BlastCommand#OptionsVals

  val referenceDB: bundles.AnyReferenceDB

  val argValsToSeq: BlastOptionsToSeq[BlastArgumentsVals] = implicitly[BlastOptionsToSeq[BlastArgumentsVals]]
  val optValsToSeq: BlastOptionsToSeq[BlastCommand#OptionsVals] // has to be provided implicitly
}

abstract class MG7Parameters[
  BC <: AnyBlastCommand { type ArgumentsVals = BlastArgumentsVals },
  BR <: AnyBlastOutputRecord.For[BC]
](val outputS3Folder: (SampleID, StepName) => S3Folder,
  val readsLength: illumina.Length,
  val splitInputFormat: SplitInputFormat = FastQInput,
  val blastOutRec: BR = defaultBlastOutRec,
  val blastOptions: BC#OptionsVals = defaultBlastOptions.value,
  val splitChunkSize: Int = 10,
  val referenceDB: bundles.AnyReferenceDB = bundles.rnaCentral
)(implicit
  val optValsToSeq: BlastOptionsToSeq[BC#OptionsVals]
  // TODO: add a check for minimal set of properties in the record (like bitscore and sgi)
) extends AnyMG7Parameters {

  type BlastCommand = BC
  type BlastOutRec = BR
}
