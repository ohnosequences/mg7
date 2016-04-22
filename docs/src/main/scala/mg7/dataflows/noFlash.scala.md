
```scala
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
```

## No-flash Dataflow

  No-flash dataflow consists of the following steps of the MG7 pipeline:

  2. split: splitting each dataset of reads on small chunks
  3. blast: processing each chunk of reads with blast
  4. merge: merging blast chunks into complete results per original reads datasets
  5. assign: assigning taxons (LCA and BBH)
  6. count: count assigns


```scala
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
          data.blastChunk -> S3Resource(params.outputS3Folder(sampleId, "blast") / "chunks" / s"blast.${n}.csv"),
          data.noHitsChunk -> S3Resource(params.outputS3Folder(sampleId, "blast") / "no-hits" / s"no-hits.${n}.fa")
        )
      )
    }
  }

  lazy val assignDataMappings = splitInputs.keys.toList.flatMap { case sampleId =>

    lazy val s3 = S3.create(
      new AWSCredentialsProviderChain(
        new InstanceProfileCredentialsProvider(),
        new ProfileCredentialsProvider()
      )
    )

    lazy val chunksS3Folder: AnyS3Address = params.outputS3Folder(sampleId, "blast") / "chunks" /
    lazy val chunks: List[S3Object] = s3.listObjects(chunksS3Folder.bucket, chunksS3Folder.key)

    chunks.zipWithIndex.map { case (chunkS3Obj, n) =>

      DataMapping(sampleId, assignDataProcessing(params))(
        remoteInput = Map(
          data.blastChunk -> S3Resource(chunkS3Obj)
        ),
        remoteOutput = Map(
          data.lcaChunk -> S3Resource(params.outputS3Folder(sampleId, "assign") / "lca" / s"${sampleId}.lca.${n}.csv"),
          data.bbhChunk -> S3Resource(params.outputS3Folder(sampleId, "assign") / "bbh" / s"${sampleId}.bbh.${n}.csv")
        )
      )
    }
  }

  lazy val mergeDataMappings = splitInputs.keys.toList.map { case sampleId =>

    DataMapping(sampleId, mergeDataProcessing)(
      remoteInput = Map(
        data.blastChunksFolder -> S3Resource(params.outputS3Folder(sampleId, "blast") / "chunks" /),
        data.blastNoHitsFolder -> S3Resource(params.outputS3Folder(sampleId, "blast") / "no-hits" /),
        data.lcaChunksFolder   -> S3Resource(params.outputS3Folder(sampleId, "assign") / "lca" /),
        data.bbhChunksFolder   -> S3Resource(params.outputS3Folder(sampleId, "assign") / "bbh" /)
      ),
      remoteOutput = Map(
        data.blastResult -> S3Resource(params.outputS3Folder(sampleId, "merge") / s"${sampleId}.blast.csv"),
        data.blastNoHits -> S3Resource(params.outputS3Folder(sampleId, "merge") / s"${sampleId}.no-hits.fa"),
        data.lcaCSV      -> S3Resource(params.outputS3Folder(sampleId, "merge") / s"${sampleId}.lca.csv"),
        data.bbhCSV      -> S3Resource(params.outputS3Folder(sampleId, "merge") / s"${sampleId}.bbh.csv")
      )
    )
  }

}

case class NoFlashDataflow[P <: AnyMG7Parameters](val params: P)(
  val splitInputs: Map[SampleID, S3Resource]
) extends AnyNoFlashDataflow {

  type Params = P
}

```




[test/scala/mg7/pipeline.scala]: ../../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/lca.scala]: ../../../../test/scala/mg7/lca.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: noFlash.scala.md
[main/scala/mg7/dataflows/full.scala]: full.scala.md
[main/scala/mg7/package.scala]: ../package.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: ../bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/bio4j/bundle.scala]: ../bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: ../bio4j/taxonomyTree.scala.md
[main/scala/mg7/dataflow.scala]: ../dataflow.scala.md
[main/scala/mg7/csv.scala]: ../csv.scala.md
[main/scala/mg7/parameters.scala]: ../parameters.scala.md
[main/scala/mg7/data.scala]: ../data.scala.md
[main/scala/mg7/loquats/7.stats.scala]: ../loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: ../loquats/8.summary.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../loquats/6.count.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../loquats/4.assign.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../loquats/5.merge.scala.md