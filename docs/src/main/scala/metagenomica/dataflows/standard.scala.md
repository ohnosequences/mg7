
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

## Standard Dataflow

  Standard dataflow consists of all steps of the MG7 pipeline:

  1. flash: merging paired end reads
  2. split: splitting each dataset of reads on small chunks
  3. blast: processing each chunk of reads with blast
  4. merge: merging blast chunks into complete results per original reads datasets
  5. assignment: assigning taxons (LCA and BBH)
  6. counting: counting assignments


```scala
trait AnyFullDataflow extends AnyNoFlashDataflow {

  val flashInputs: Map[SampleID, (S3Resource, S3Resource)]

  lazy val flashDataMappings = flashInputs.toList.map {
    case (sampleId, (reads1S3Resource, reads2S3Resource)) =>

      DataMapping(sampleId, flashDataProcessing(params))(
        remoteInput = Map(
          data.pairedReads1 -> reads1S3Resource,
          data.pairedReads2 -> reads2S3Resource
        ),
        remoteOutput = Map(
          data.mergedReads -> S3Resource(params.outputS3Folder(sampleId, "flash") / s"${sampleId}.merged.fastq"),
          data.flashStats  -> S3Resource(params.outputS3Folder(sampleId, "flash") / s"${sampleId}.stats.txt")
        )
      )
  }

  val splitInputs: Map[SampleID, S3Resource] = flashDataMappings.map { flashDM =>
    flashDM.label -> flashDM.remoteOutput(data.mergedReads)
  }.toMap
}

case class FullDataflow[P <: AnyMG7Parameters](val params: P)(
  val flashInputs: Map[SampleID, (S3Resource, S3Resource)]
) extends AnyFullDataflow {

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