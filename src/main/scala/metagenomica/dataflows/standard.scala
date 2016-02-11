package ohnosequences.metagenomica.dataflows

import ohnosequences.metagenomica._

import ohnosequences.datasets._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._

trait AnyDataflow {}

/* ## Standard Dataflow

  Standard dataflow consists of all steps of the MG7 pipeline:

  1. flash: merging paired end reads
  2. split: splitting each dataset of reads on small chunks
  3. blast: processing each chunk of reads with blast
  4. merge: merging blast chunks into complete results per original reads datasets
  5. assignment: assigning taxons (LCA and BBH)
  6. counting: counting assignments
*/
case class StandardDataflow(
  val inputSamples: Map[ID, (S3Object, S3Object)],
  val outputS3Folder: S3Folder
) extends AnyDataflow {

  lazy val flashDataMappings: List[AnyDataMapping] = inputSamples.toList.map {
    case (sampleId, (reads1S3Obj, reads2S3Obj)) =>

      DataMapping(sampleId)(
        remoteInput = Map(
          data.pairedReads1 -> S3Resource(reads1S3Obj),
          data.pairedReads2 -> S3Resource(reads2S3Obj)
        ),
        remoteOutput = Map(
          data.mergedReads -> S3Resource(outputS3Folder / "flash" / sampleId / s"${sampleId}.merged.fastq"),
          data.flashStats  -> S3Resource(outputS3Folder / "flash" / sampleId / s"${sampleId}.stats.txt")
        )
      )
  }

  lazy val splitDataMappings: List[AnyDataMapping] = flashDataMappings.map { flashDM =>
    val sampleId = flashDM.id

    DataMapping(sampleId)(
      remoteInput = Map(
        data.mergedReads -> flashDM.remoteOutput(data.mergedReads)
      ),
      remoteOutput = Map(
        data.readsChunks -> S3Resource(outputS3Folder / "split" / sampleId /)
      )
    )
  }

  lazy val blastDataMappings: List[AnyDataMapping] = splitDataMappings.flatMap { splitDM =>
    val sampleId = splitDM.id

    lazy val s3 = S3.create(
      new AWSCredentialsProviderChain(
        new InstanceProfileCredentialsProvider(),
        new ProfileCredentialsProvider()
      )
    )

    lazy val chunksS3Folder: AnyS3Address = splitDM.remoteOutput(data.readsChunks).resource

    lazy val chunks: List[S3Object] = s3.listObjects(chunksS3Folder.bucket, chunksS3Folder.key)

    chunks.zipWithIndex.map { case (chunkS3Obj, n) =>

      DataMapping(s"${sampleId}.${n}")(
        remoteInput = Map(
          data.readsChunk -> S3Resource(chunkS3Obj)
        ),
        remoteOutput = Map(
          data.blastChunkOut -> S3Resource(outputS3Folder / "blast" / sampleId / s"blast.${n}.csv")
        )
      )
    }
  }

  lazy val mergeDataMappings: List[AnyDataMapping] = splitDataMappings.map { splitDM =>
    val sampleId = splitDM.id

    DataMapping(sampleId)(
      remoteInput = Map(
        data.blastChunks -> S3Resource(outputS3Folder / "blast" / sampleId /)
      ),
      remoteOutput = Map(
        data.blastResult -> S3Resource(outputS3Folder / "merge" / sampleId / s"${sampleId}.blast.csv")
      )
    )
  }

  lazy val assignmentDataMappings: List[AnyDataMapping] = mergeDataMappings.map { mergeDM =>
    val sampleId = mergeDM.id

    DataMapping(sampleId)(
      remoteInput = mergeDM.remoteOutput,
      remoteOutput = Map(
        data.lcaCSV -> S3Resource(outputS3Folder / "assignment" / sampleId / s"${sampleId}.lca.csv"),
        data.bbhCSV -> S3Resource(outputS3Folder / "assignment" / sampleId / s"${sampleId}.bbh.csv")
      )
    )
  }

  lazy val countingDataMappings: List[AnyDataMapping] = assignmentDataMappings.map { assignmentDM =>
    val sampleId = assignmentDM.id

    DataMapping(sampleId)(
      remoteInput = assignmentDM.remoteOutput,
      remoteOutput = Map(
        data.lcaCountsCSV -> S3Resource(outputS3Folder / "counting" / sampleId / s"${sampleId}.lca.counts.csv"),
        data.bbhCountsCSV -> S3Resource(outputS3Folder / "counting" / sampleId / s"${sampleId}.bbh.counts.csv")
      )
    )
  }

}
