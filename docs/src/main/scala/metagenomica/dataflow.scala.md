
```scala
package ohnosequences.mg7

import ohnosequences.mg7.loquats._
import ohnosequences.loquat._
import ohnosequences.datasets._
import ohnosequences.awstools.s3._

trait AnyDataflow {

  // param-pam-pam
  type Params <: AnyMG7Parameters
  val  params: Params
```

The essential steps of any MG7 dataflow are
- BLAST

```scala
  val blastDataMappings: List[DataMapping[blastDataProcessing[Params]]]
```

- Assignment

```scala
  val assignmentDataMappings: List[DataMapping[assignmentDataProcessing[Params]]]
```

- Counting

```scala
  lazy val countingDataMappings: List[DataMapping[countingDataProcessing.type]] =
    assignmentDataMappings.map { assignmentDM =>
      val sampleId = assignmentDM.label

      DataMapping(sampleId, countingDataProcessing)(
        remoteInput = assignmentDM.remoteOutput,
        remoteOutput = Map(
          data.lcaDirectCountsCSV -> S3Resource(params.outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.direct.absolute.counts.csv"),
          data.bbhDirectCountsCSV -> S3Resource(params.outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.direct.absolute.counts.csv"),
          data.lcaAccumCountsCSV  -> S3Resource(params.outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.accum.absolute.counts.csv"),
          data.bbhAccumCountsCSV  -> S3Resource(params.outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.accum.absolute.counts.csv")
        )
      )
    }

}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: bundles/blast.scala.md
[main/scala/metagenomica/bundles/filterGIs.scala]: bundles/filterGIs.scala.md
[main/scala/metagenomica/bundles/flash.scala]: bundles/flash.scala.md
[main/scala/metagenomica/bundles/referenceDB.scala]: bundles/referenceDB.scala.md
[main/scala/metagenomica/bundles/referenceMap.scala]: bundles/referenceMap.scala.md
[main/scala/metagenomica/data.scala]: data.scala.md
[main/scala/metagenomica/dataflow.scala]: dataflow.scala.md
[main/scala/metagenomica/dataflows/noFlash.scala]: dataflows/noFlash.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: package.scala.md
[main/scala/metagenomica/parameters.scala]: parameters.scala.md
[test/scala/bundles.scala]: ../../../test/scala/bundles.scala.md
[test/scala/lca.scala]: ../../../test/scala/lca.scala.md
[test/scala/metagenomica/pipeline.scala]: ../../../test/scala/metagenomica/pipeline.scala.md