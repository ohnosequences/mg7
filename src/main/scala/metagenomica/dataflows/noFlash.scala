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

/* ## No-flash Dataflow

  No-flash dataflow consists of the following steps of the MG7 pipeline:

  2. split: splitting each dataset of reads on small chunks
  3. blast: processing each chunk of reads with blast
  4. merge: merging blast chunks into complete results per original reads datasets
  5. assignment: assigning taxons (LCA and BBH)
  6. counting: counting assignments
*/
trait AnyNoFlashDataflow extends AnyDataflow {

  val splitInputs: Map[SampleID, S3Resource]

  lazy val splitDataMappings = splitInputs.toList.map { case (sampleId, readsS3Resource) =>

    DataMapping(sampleId, splitDataProcessing(params))(
      remoteInput = Map(
        data.mergedReads -> readsS3Resource
      ),
      remoteOutput = Map(
        data.fastaChunks -> S3Resource(params.outputS3Folder(sampleId, "split"))
      )
    )
  }

  lazy val blastDataMappings = splitDataMappings.flatMap { splitDM =>
    val sampleId = splitDM.label

    lazy val s3 = S3.create(
      new AWSCredentialsProviderChain(
        new InstanceProfileCredentialsProvider(),
        new ProfileCredentialsProvider()
      )
    )

    lazy val chunksS3Folder: AnyS3Address = splitDM.remoteOutput(data.fastaChunks).resource

    lazy val chunks: List[S3Object] = s3.listObjects(chunksS3Folder.bucket, chunksS3Folder.key)

    chunks.zipWithIndex.map { case (chunkS3Obj, n) =>

      DataMapping(s"${sampleId}.${n}", blastDataProcessing(params))(
        remoteInput = Map(
          data.fastaChunk -> S3Resource(chunkS3Obj)
        ),
        remoteOutput = Map(
          data.blastChunkOut -> S3Resource(params.outputS3Folder(sampleId, "blast") / s"blast.${n}.csv")
        )
      )
    }
  }

  lazy val mergeDataMappings = splitDataMappings.map { splitDM =>
    val sampleId = splitDM.label

    DataMapping(sampleId, mergeDataProcessing)(
      remoteInput = Map(
        data.blastChunks -> S3Resource(params.outputS3Folder(sampleId, "blast"))
      ),
      remoteOutput = Map(
        data.blastResult -> S3Resource(params.outputS3Folder(sampleId, "merge") / s"${sampleId}.blast.csv")
      )
    )
  }

  lazy val assignmentDataMappings = mergeDataMappings.map { mergeDM =>
    val sampleId = mergeDM.label

    DataMapping(sampleId, assignmentDataProcessing(params))(
      remoteInput = mergeDM.remoteOutput,
      remoteOutput = Map(
        data.lcaCSV -> S3Resource(params.outputS3Folder(sampleId, "assignment") / s"${sampleId}.lca.csv"),
        data.bbhCSV -> S3Resource(params.outputS3Folder(sampleId, "assignment") / s"${sampleId}.bbh.csv")
      )
    )
  }

}

case class NoFlashDataflow[P <: AnyMG7Parameters](val params: P)(
  val splitInputs: Map[SampleID, S3Resource]
) extends AnyNoFlashDataflow {

  type Params = P
}
