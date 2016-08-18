package ohnosequences.mg7.dataflows

import ohnosequences.mg7._, loquats._

import ohnosequences.datasets._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._

/* ## Standard Dataflow

  Standard dataflow, which will perform the following steps:

  1. **flash** merge paired-end reads
  2. **split** split each reads dataset into smaller chunks
  3. **blast** blast each chunk of reads against the reference database
  4. **assign** assign reads from each chunk to taxa (LCA and BBH)
  5. **merge** merge results for each reads dataset
  6. **count** count assignments
*/
trait AnyFullDataflow extends AnyNoFlashDataflow {

  val flashInputs: Map[SampleID, (S3Resource, S3Resource)]

  lazy val flashDataMappings: List[AnyDataMapping] =
    flashInputs.toList.map {
      case (sampleId, (reads1S3Resource, reads2S3Resource)) =>
        DataMapping(sampleId, flashDataProcessing(params))(
          remoteInput = Map(
            data.pairedReads1 -> reads1S3Resource,
            data.pairedReads2 -> reads2S3Resource
          ),
          remoteOutput = Map(
            data.mergedReads    -> S3Resource(params.outputS3Folder(sampleId, "flash") / s"${sampleId}.merged.fastq"),
            data.pair1NotMerged -> S3Resource(params.outputS3Folder(sampleId, "flash") / s"${sampleId}.pair1.not-merged.fastq"),
            data.pair2NotMerged -> S3Resource(params.outputS3Folder(sampleId, "flash") / s"${sampleId}.pair2.not-merged.fastq"),
            data.flashHistogram -> S3Resource(params.outputS3Folder(sampleId, "flash") / s"${sampleId}.hist")
          )
        )
    }

  val splitInputs: Map[SampleID, S3Resource] =
    flashDataMappings.map { flashDM => flashDM.label -> flashDM.remoteOutput(data.mergedReads) }.toMap

}

case class FullDataflow[P <: AnyMG7Parameters](val params: P)(val flashInputs: Map[SampleID, (S3Resource, S3Resource)])
extends AnyFullDataflow {

  type Params = P
}
