
```scala
package ohnosequences.test.mg7.mock

import ohnosequences.test.mg7._, testDefaults._
import ohnosequences.mg7._, loquats._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._
import ohnosequences.awstools._, ec2._ , s3._, autoscaling._, regions._
import com.amazonaws.auth._, profile._

case object pacbio {

  case object pipeline extends MG7Pipeline(PacBioParameters) with MG7PipelineDefaults {

    override val logsS3Prefix = s3"loquat.testing" / "mg7" / "pacbio" /

    val sampleIDs: List[ID] = List(
      "stagg",
      "even"
      // NOTE: this sample name corresponds to the blast results @rtobes filtered manually
      // "even-filtered"
    )

    val inputSamples: Map[ID, S3Resource] = sampleIDs.map { id =>
      id -> S3Resource(testData.s3 / "pacbio" / s"${id}.subreads_ccs_99.fastq.filter.fastq")
    }.toMap

    val outputS3Folder = testDefaults.outputS3FolderFor("pacbio")
  }
}

```




[main/scala/mg7/bundles.scala]: ../../../../main/scala/mg7/bundles.scala.md
[main/scala/mg7/configs.scala]: ../../../../main/scala/mg7/configs.scala.md
[main/scala/mg7/csv.scala]: ../../../../main/scala/mg7/csv.scala.md
[main/scala/mg7/data.scala]: ../../../../main/scala/mg7/data.scala.md
[main/scala/mg7/defaults.scala]: ../../../../main/scala/mg7/defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../../../../main/scala/mg7/loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../../../../main/scala/mg7/loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../../../../main/scala/mg7/loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../../../../main/scala/mg7/loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../../../../main/scala/mg7/loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../../../../main/scala/mg7/loquats/6.count.scala.md
[main/scala/mg7/package.scala]: ../../../../main/scala/mg7/package.scala.md
[main/scala/mg7/parameters.scala]: ../../../../main/scala/mg7/parameters.scala.md
[main/scala/mg7/pipeline.scala]: ../../../../main/scala/mg7/pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: ../../../../main/scala/mg7/referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../counts.scala.md
[test/scala/mg7/fqnames.scala]: ../fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: ../PRJEB6592/PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: ../referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: ../taxonomy.scala.md
[test/scala/mg7/testData.scala]: ../testData.scala.md
[test/scala/mg7/testDefaults.scala]: ../testDefaults.scala.md