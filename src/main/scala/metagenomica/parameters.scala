package ohnosequences.metagenomica

import ohnosequences.cosas._, typeSets._, types._
import ops.typeSets._

import ohnosequences.datasets._, illumina._

import ohnosequences.flash.api._

import ohnosequences.{ blast => b }, b.api._, b.data._

import scala.util.Try


trait AnyMG7Parameters {

  // FLASH

  type ReadsType <: AnyReadsType { type EndType = pairedEndType }
  val  readsType: ReadsType

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults update (
    read_len(readsType.length.length)   :~:
    max_overlap(readsType.length.length) :~: ∅
  )

  // BLAST

  type BlastOutRec <: AnyBlastOutputRecord
  val  blastOutRec: BlastOutRec

  // implicitly:
  val validBlastRecord: BlastOutRec#Properties CheckForAll ValidOutputRecordFor[blastn]

  type BlastExprType = BlastExpressionType[blastn, BlastOutRec]
  lazy val blastExprType: BlastExprType = new BlastExpressionType(blastn)(blastOutRec)(validBlastRecord)

  type BlastOutType = BlastOutputType[BlastExprType]
  val  blastOutType: BlastOutType = new BlastOutputType(blastExprType, "useless label")

  def blastExpr(args: ValueOf[blastn.Arguments]): BlastExpression[BlastExprType] = {
    BlastExpression(blastExprType)(
      argumentValues = args,
      // TODO: should it be configurable?
      optionValues   = blastn.defaults update (
        num_threads(1) :~:
        word_size(42) :~:
        max_target_seqs(10) :~:
        ohnosequences.blast.api.evalue(0.001)  :~:
        blastn.task(blastn.megablast) :~:
        ∅
      )
    )
  }
}

abstract class MG7Parameters[
  RT <: AnyReadsType { type EndType = pairedEndType },
  BR <: AnyBlastOutputRecord
](val readsType: RT,
  val blastOutRec: BR
)(implicit
  // TODO: add a check for minimal set of properties in the record (like bitscore and sgi)
  val validBlastRecord: BR#Properties CheckForAll ValidOutputRecordFor[blastn]
) extends AnyMG7Parameters {

  type ReadsType = RT

  type BlastOutRec = BR
}
