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

trait AnyDataflow {

  val outputS3Folder: (SampleID, StepName) => S3Folder
}

/* ## Standard Dataflow

  Standard dataflow consists of all steps of the MG7 pipeline:

  1. flash: merging paired end reads
  2. split: splitting each dataset of reads on small chunks
  3. blast: processing each chunk of reads with blast
  4. merge: merging blast chunks into complete results per original reads datasets
  5. assignment: assigning taxons (LCA and BBH)
  6. counting: counting assignments
*/
case class StandardDataflow[P <: AnyMG7Parameters](val params: P)(
  val inputSamples: Map[ID, (S3Object, S3Object)],
  val outputS3Folder: (SampleID, StepName) => S3Folder
) extends AnyDataflow {

  lazy val flashDataMappings: List[AnyDataMapping] = inputSamples.toList.map {
    case (sampleId, (reads1S3Obj, reads2S3Obj)) =>

      DataMapping(sampleId, flashDataProcessing(params))(
        remoteInput = Map(
          data.pairedReads1 -> S3Resource(reads1S3Obj),
          data.pairedReads2 -> S3Resource(reads2S3Obj)
        ),
        remoteOutput = Map(
          data.mergedReads -> S3Resource(outputS3Folder(sampleId, "flash") / s"${sampleId}.merged.fastq"),
          data.flashStats  -> S3Resource(outputS3Folder(sampleId, "flash") / s"${sampleId}.stats.txt")
        )
      )
  }

  lazy val splitDataMappings: List[AnyDataMapping] = flashDataMappings.map { flashDM =>
    val sampleId = flashDM.label

    DataMapping(sampleId, splitDataProcessing(params))(
      remoteInput = Map(
        data.mergedReads -> flashDM.remoteOutput(data.mergedReads)
      ),
      remoteOutput = Map(
        data.readsChunks -> S3Resource(outputS3Folder(sampleId, "split"))
      )
    )
  }

  lazy val blastDataMappings: List[AnyDataMapping] = splitDataMappings.flatMap { splitDM =>
    val sampleId = splitDM.label

    lazy val s3 = S3.create(
      new AWSCredentialsProviderChain(
        new InstanceProfileCredentialsProvider(),
        new ProfileCredentialsProvider()
      )
    )

    lazy val chunksS3Folder: AnyS3Address = splitDM.remoteOutput(data.readsChunks).resource

    lazy val chunks: List[S3Object] = s3.listObjects(chunksS3Folder.bucket, chunksS3Folder.key)

    chunks.zipWithIndex.map { case (chunkS3Obj, n) =>

      DataMapping(s"${sampleId}.${n}", blastDataProcessing(params))(
        remoteInput = Map(
          data.readsChunk -> S3Resource(chunkS3Obj)
        ),
        remoteOutput = Map(
          data.blastChunkOut -> S3Resource(outputS3Folder(sampleId, "blast") / s"blast.${n}.csv")
        )
      )
    }
  }

  lazy val mergeDataMappings: List[AnyDataMapping] = splitDataMappings.map { splitDM =>
    val sampleId = splitDM.label

    DataMapping(sampleId, mergeDataProcessing)(
      remoteInput = Map(
        data.blastChunks -> S3Resource(outputS3Folder(sampleId, "blast"))
      ),
      remoteOutput = Map(
        data.blastResult -> S3Resource(outputS3Folder(sampleId, "merge") / s"${sampleId}.blast.csv")
      )
    )
  }

  lazy val assignmentDataMappings: List[AnyDataMapping] = mergeDataMappings.map { mergeDM =>
    val sampleId = mergeDM.label

    DataMapping(sampleId, assignmentDataProcessing(params))(
      remoteInput = mergeDM.remoteOutput,
      remoteOutput = Map(
        data.lcaCSV -> S3Resource(outputS3Folder(sampleId, "assignment") / s"${sampleId}.lca.csv"),
        data.bbhCSV -> S3Resource(outputS3Folder(sampleId, "assignment") / s"${sampleId}.bbh.csv")
      )
    )
  }

  lazy val countingDataMappings: List[AnyDataMapping] = assignmentDataMappings.map { assignmentDM =>
    val sampleId = assignmentDM.label

    DataMapping(sampleId, countingDataProcessing)(
      remoteInput = assignmentDM.remoteOutput,
      remoteOutput = Map(
        data.lcaDirectCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.direct.counts.csv"),
        data.bbhDirectCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.direct.counts.csv"),
        data.lcaAccumCountsCSV  -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.accum.counts.csv"),
        data.bbhAccumCountsCSV  -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.accum.counts.csv")
      )
    )
  }

}
