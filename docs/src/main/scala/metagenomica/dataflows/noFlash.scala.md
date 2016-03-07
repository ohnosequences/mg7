
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
  5. assignment: assigning taxons (LCA and BBH)
  6. counting: counting assignments


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

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: ../bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: ../bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: ../bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: ../bundles/blast.scala.md
[main/scala/metagenomica/bundles/filterGIs.scala]: ../bundles/filterGIs.scala.md
[main/scala/metagenomica/bundles/flash.scala]: ../bundles/flash.scala.md
[main/scala/metagenomica/bundles/referenceDB.scala]: ../bundles/referenceDB.scala.md
[main/scala/metagenomica/bundles/referenceMap.scala]: ../bundles/referenceMap.scala.md
[main/scala/metagenomica/data.scala]: ../data.scala.md
[main/scala/metagenomica/dataflow.scala]: ../dataflow.scala.md
[main/scala/metagenomica/dataflows/noFlash.scala]: noFlash.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../package.scala.md
[main/scala/metagenomica/parameters.scala]: ../parameters.scala.md
[test/scala/bundles.scala]: ../../../../test/scala/bundles.scala.md
[test/scala/lca.scala]: ../../../../test/scala/lca.scala.md
[test/scala/metagenomica/pipeline.scala]: ../../../../test/scala/metagenomica/pipeline.scala.md