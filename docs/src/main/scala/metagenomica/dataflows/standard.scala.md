
```scala
package ohnosequences.mg7.dataflows

import ohnosequences.mg7._

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
```

## Standard Dataflow

  Standard dataflow consists of all steps of the MG7 pipeline:

  1. flash: merging paired end reads
  2. split: splitting each dataset of reads on small chunks
  3. blast: processing each chunk of reads with blast
  4. merge: merging blast chunks into complete results per original reads datasets
  5. assignment: assigning taxons (LCA and BBH)
  6. counting: counting assignments


```scala
case class StandardDataflow(
  val inputSamples: Map[ID, (S3Object, S3Object)],
  val outputS3Folder: (SampleID, StepName) => S3Folder
) extends AnyDataflow {

  lazy val flashDataMappings: List[AnyDataMapping] = inputSamples.toList.map {
    case (sampleId, (reads1S3Obj, reads2S3Obj)) =>

      DataMapping(sampleId)(
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

    DataMapping(sampleId)(
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

      DataMapping(s"${sampleId}.${n}")(
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

    DataMapping(sampleId)(
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

    DataMapping(sampleId)(
      remoteInput = mergeDM.remoteOutput,
      remoteOutput = Map(
        data.lcaCSV -> S3Resource(outputS3Folder(sampleId, "assignment") / s"${sampleId}.lca.csv"),
        data.bbhCSV -> S3Resource(outputS3Folder(sampleId, "assignment") / s"${sampleId}.bbh.csv")
      )
    )
  }

  lazy val countingDataMappings: List[AnyDataMapping] = assignmentDataMappings.map { assignmentDM =>
    val sampleId = assignmentDM.label

    DataMapping(sampleId)(
      remoteInput = assignmentDM.remoteOutput,
      remoteOutput = Map(
        data.lcaCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.counts.csv"),
        data.bbhCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.counts.csv")
      )
    )
  }

}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: ../bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: ../bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: ../bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: ../bundles/blast.scala.md
[main/scala/metagenomica/bundles/blast16s.scala]: ../bundles/blast16s.scala.md
[main/scala/metagenomica/bundles/flash.scala]: ../bundles/flash.scala.md
[main/scala/metagenomica/bundles/gis.scala]: ../bundles/gis.scala.md
[main/scala/metagenomica/data.scala]: ../data.scala.md
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