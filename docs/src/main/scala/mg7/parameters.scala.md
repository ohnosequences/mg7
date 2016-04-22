
```scala
package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._

import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api._
import ohnosequences.blast.api.{ outputFields => out }

import era7bio.db._

import better.files._


sealed trait SplitInputFormat
case object FastaInput extends SplitInputFormat
case object FastQInput extends SplitInputFormat

trait AnyMG7Parameters {

  val outputS3Folder: (SampleID, StepName) => S3Folder
```

Flash parameters

```scala
  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults.update(
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )
```

Split parameters
This is the number of reads in each chunk after the `split` step

```scala
  // TODO: would be nice to have Nat here
  val splitChunkSize: Int
  val splitInputFormat: SplitInputFormat
```

BLAST parameters

```scala
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

  // has to be provided implicitly
  implicit val argValsToSeq: BlastOptionsToSeq[BlastArgumentsVals]
  implicit val optValsToSeq: BlastOptionsToSeq[BlastCommand#OptionsVals]

  def blastExpr(inFile: File, outFile: File):
    BlastExpression[BlastCommand, BlastOutputRecord[BlastOutRecKeys]] = {

    BlastExpression(blastCommand)(
      outputRecord = blastOutRec,
      argumentValues =
        db(Set(referenceDB.dbName)) ::
        query(inFile) ::
        ohnosequences.blast.api.out(outFile) ::
        *[AnyDenotation],
      optionValues = blastOptions
    )(argValsToSeq, optValsToSeq)
  }


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
  final def defaultBlastFilter(row: csv.Row[BlastOutRecKeys]): Boolean = {
    row.select(outputFields.qcovs) == "100" &&
    row.select(outputFields.gaps) == "0"
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

```




[test/scala/mg7/pipeline.scala]: ../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/lca.scala]: ../../../test/scala/mg7/lca.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: dataflows/noFlash.scala.md
[main/scala/mg7/dataflows/full.scala]: dataflows/full.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/bio4j/bundle.scala]: bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: bio4j/taxonomyTree.scala.md
[main/scala/mg7/dataflow.scala]: dataflow.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/loquats/7.stats.scala]: loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: loquats/8.summary.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md