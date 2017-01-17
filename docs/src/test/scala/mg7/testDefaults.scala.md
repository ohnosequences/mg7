
# Default MG7 test configuration and parameters


```scala
package ohnosequences.test.mg7

import ohnosequences.mg7._, loquats._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._
import ohnosequences.db.rna16s
import ohnosequences.awstools._, ec2._ , s3._, autoscaling._, regions._
import com.amazonaws.auth._, profile._
import ohnosequences.datasets.illumina._

case object testDefaults {

  lazy val mg7 = ohnosequences.generated.metadata.mg7
```

Output test data *is* scoped by version

```scala
  lazy val outputS3Prefix = S3Folder("resources.ohnosequences.com", mg7.organization) / mg7.artifact / mg7.version / "test" /

  def outputS3FolderFor(pipeline: String): (SampleID, StepName) => S3Folder = { (sampleID, step) =>
    outputS3Prefix / pipeline / sampleID / step /
  }


  trait MG7PipelineDefaults extends AnyMG7Pipeline {

    val metadata = ohnosequences.generated.metadata.mg7
    val iamRoleName = "loquat.testing"
    val logsS3Prefix = s3"loquat.testing" / "mg7" / name /
    val outputS3Folder = testDefaults.outputS3FolderFor(name)

    val splitConfig  = SplitConfig(1)
    val blastConfig  = BlastConfig(100)
    val assignConfig = AssignConfig(6)
    val mergeConfig  = MergeConfig(1)
    val countConfig  = CountConfig(1)
  }

  lazy val loquatUser = LoquatUser(
    email = "aalekhin@ohnosequences.com",
    localCredentials = new ProfileCredentialsProvider("default"),
    keypairName = "aalekhin"
  )
}

```




[main/scala/mg7/bundles.scala]: ../../../main/scala/mg7/bundles.scala.md
[main/scala/mg7/configs.scala]: ../../../main/scala/mg7/configs.scala.md
[main/scala/mg7/csv.scala]: ../../../main/scala/mg7/csv.scala.md
[main/scala/mg7/data.scala]: ../../../main/scala/mg7/data.scala.md
[main/scala/mg7/defaults.scala]: ../../../main/scala/mg7/defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../../../main/scala/mg7/loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../../../main/scala/mg7/loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../../../main/scala/mg7/loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../../../main/scala/mg7/loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../../../main/scala/mg7/loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../../../main/scala/mg7/loquats/6.count.scala.md
[main/scala/mg7/package.scala]: ../../../main/scala/mg7/package.scala.md
[main/scala/mg7/parameters.scala]: ../../../main/scala/mg7/parameters.scala.md
[main/scala/mg7/pipeline.scala]: ../../../main/scala/mg7/pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: ../../../main/scala/mg7/referenceDB.scala.md
[test/scala/mg7/counts.scala]: counts.scala.md
[test/scala/mg7/fqnames.scala]: fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: mock/illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: mock/pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: PRJEB6592/PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: taxonomy.scala.md
[test/scala/mg7/testData.scala]: testData.scala.md
[test/scala/mg7/testDefaults.scala]: testDefaults.scala.md