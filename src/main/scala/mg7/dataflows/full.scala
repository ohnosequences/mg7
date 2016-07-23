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

// TODO this description is outdated right? assignment is done per chunk IIRC
/* ## Standard Dataflow

  Standard dataflow, which will perform the following steps:

  1. **flash** merge paired-end reads
  2. **split** split each reads dataset into smaller chunks
  3. **blast** blast each chunk of reads against the reference database
  4. **merge** merge blast results for each reads dataset
  5. **assign** assign reads to taxa (LCA and BBH)
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

  lazy val statsDataMappings: List[AnyDataMapping] =
    List[List[AnyDataMapping]](
      flashDataMappings,
      mergeDataMappings,
      assignDataMappings
    ).flatten
    .groupBy { _.label }
    .map {
      case (sampleID: String, dms: List[AnyDataMapping]) =>
        val outputs: Map[AnyData, S3Resource] =
          dms.map{ _.remoteOutput }.foldLeft(Map[AnyData, S3Resource]()){ _ ++ _ }

        DataMapping(sampleID, statsDataProcessing)(
          remoteInput = Map[AnyData, AnyRemoteResource](
            data.sampleID       -> MessageResource(sampleID),
            data.pairedReads1   -> flashInputs(sampleID)._1,
            data.mergedReads    -> outputs(data.mergedReads),
            data.pair1NotMerged -> outputs(data.pair1NotMerged),
            data.blastNoHits    -> outputs(data.blastNoHits)
          ),
          remoteOutput = Map(
            data.sampleStatsCSV -> S3Resource(params.outputS3Folder("samples", "stats") / s"${sampleID}.stats.csv")
          )
        )
    }
    .toList

  lazy val summaryDataMappings: List[AnyDataMapping] =
    List(
      DataMapping("summmary", summaryDataProcessing)(
        remoteInput = Map(
          data.sampleStatsFolder -> S3Resource(params.outputS3Folder("samples", "stats"))
        ),
        remoteOutput = Map(
          data.summaryStatsCSV -> S3Resource(params.outputS3Folder("summary", "stats") / s"summary.csv")
        )
      )
    )
}

case class FullDataflow[P <: AnyMG7Parameters](val params: P)(val flashInputs: Map[SampleID, (S3Resource, S3Resource)]) 
extends AnyFullDataflow {

  type Params = P
}