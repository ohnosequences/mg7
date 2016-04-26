
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

```scala
  val splitDataMappings: List[DataMapping[splitDataProcessing]]
```

- BLAST

```scala
  val blastDataMappings: List[DataMapping[blastDataProcessing[Params]]]
```

- Assignment

```scala
  val assignDataMappings: List[DataMapping[assignDataProcessing[Params]]]
```

- Merge

```scala
  val mergeDataMappings: List[DataMapping[mergeDataProcessing.type]]
```

- Counting

```scala
  lazy val countDataMappings: List[DataMapping[countDataProcessing.type]] =
    mergeDataMappings.map { case mergeDM =>
      val sampleId = mergeDM.label

      DataMapping(sampleId, countDataProcessing)(
        remoteInput = Map(
          lookup(data.lcaCSV, mergeDM.remoteOutput),
          lookup(data.bbhCSV, mergeDM.remoteOutput)
        ),
        remoteOutput = Map(
          data.lcaDirectCountsCSV     -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.direct.absolute.counts.csv"),
          data.lcaAccumCountsCSV      -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.accum.absolute.counts.csv"),
          data.lcaDirectFreqCountsCSV -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.direct.frequency.counts.csv"),
          data.lcaAccumFreqCountsCSV  -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.accum.frequency.counts.csv"),
          data.bbhDirectCountsCSV     -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.direct.absolute.counts.csv"),
          data.bbhAccumCountsCSV      -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.accum.absolute.counts.csv"),
          data.bbhDirectFreqCountsCSV -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.direct.frequency.counts.csv"),
          data.bbhAccumFreqCountsCSV  -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.accum.frequency.counts.csv")
        )
      )
    }

}

```




[main/scala/mg7/bio4j/bundle.scala]: bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: bio4j/taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/dataflow.scala]: dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[test/scala/mg7/counts.scala]: ../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/lca.scala]: ../../../test/scala/mg7/lca.scala.md
[test/scala/mg7/pipeline.scala]: ../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../test/scala/mg7/taxonomy.scala.md