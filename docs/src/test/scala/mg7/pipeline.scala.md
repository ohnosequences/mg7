
```scala
package ohnosequences.mg7


import ohnosequences.mg7._, loquats._, dataflows._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._

import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._



case object test {

  case object testParameters extends MG7Parameters(
    outputS3Folder = testOutS3Folder,
    readsLength = bp300,
    // blastCommand = blastn,
    // blastOutRec  = defaultBlastOutRec,
    // blastOptions = defaultBlastnOptions.value
    referenceDB  = era7bio.db.rna16s.release
  ) {

    // an example of how you can add some conditions to the filter predicate
    override def blastFilter(row: csv.Row[BlastOutRecKeys]): Boolean = {
      defaultBlastFilter(row) ||
      parseInt(row.select(outputFields.bitscore)).map{ _ > 42 }.getOrElse(false)
    }
  }

  val defaultAMI = AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  trait AnyTestLoquatConfig extends AnyLoquatConfig { config =>

    val metadata: AnyArtifactMetadata = generated.metadata.mg7

    val iamRoleName = "loquat.testing"
    val logsBucketName = "loquat.testing"

    val  managerConfig = ManagerConfig(
      InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1))
    )

    val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )

    val terminationConfig = TerminationConfig(
      terminateAfterInitialDataMappings = true
    )

    val dataMappings: List[AnyDataMapping]
  }

  abstract class TestLoquatConfig(
    val loquatName: String,
    val dataMappings: List[AnyDataMapping]
  ) extends AnyTestLoquatConfig


  val loquatUser = LoquatUser(
    email = "aalekhin@ohnosequences.com",
    localCredentials = new ProfileCredentialsProvider("default"),
    keypairName = "aalekhin"
  )


  val commonS3Prefix = S3Folder("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592")

  val sampleIds: List[ID] = List("ERR567374")

  def testOutS3Folder(sampleId: SampleID, step: StepName): S3Folder =
    commonS3Prefix / s"${step}-test" / sampleId /

  val inputSamples: Map[ID, (S3Resource, S3Resource)] = sampleIds.map { id =>
    id -> ((
      S3Resource(commonS3Prefix / "reads" / s"${id}_1.fastq.gz"),
      S3Resource(commonS3Prefix / "reads" / s"${id}_2.fastq.gz")
    ))
  }.toMap

  val splitInputs: Map[ID, S3Resource] = sampleIds.map { sampleId =>
    sampleId -> S3Resource(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq")
  }.toMap

  val dataflow = NoFlashDataflow(testParameters)(splitInputs)


  // case object flashConfig extends TestLoquatConfig("flash", dataflow.flashDataMappings)
  // case object flashLoquat extends Loquat(flashConfig, flashDataProcessing(testParameters))

  case object splitConfig extends TestLoquatConfig("split", dataflow.splitDataMappings)
  case object splitLoquat extends Loquat(splitConfig, splitDataProcessing(testParameters))

  case object blastConfig extends TestLoquatConfig("blast", dataflow.blastDataMappings) {
    // NOTE: we don't want to check input objects here because they are too many and
    //   checking them one by one will take too long and likely fail
    override val checkInputObjects = false
  }
  case object blastLoquat extends Loquat(blastConfig, blastDataProcessing(testParameters))

  case object mergeConfig extends TestLoquatConfig("merge", dataflow.mergeDataMappings)
  case object mergeLoquat extends Loquat(mergeConfig, mergeDataProcessing)

  case object assignConfig extends TestLoquatConfig("assign", dataflow.assignDataMappings) {

    override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 3)

    override val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, r3.large),
      purchaseModel = Spot(maxPrice = Some(0.4)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )
  }
  case object assignLoquat extends Loquat(assignConfig, assignDataProcessing(testParameters))

  case object countConfig extends TestLoquatConfig("count", dataflow.countDataMappings)
  case object countLoquat extends Loquat(countConfig, countDataProcessing)

}

```




[main/scala/mg7/bio4j/bundle.scala]: ../../../main/scala/mg7/bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: ../../../main/scala/mg7/bio4j/taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: ../../../main/scala/mg7/bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: ../../../main/scala/mg7/csv.scala.md
[main/scala/mg7/data.scala]: ../../../main/scala/mg7/data.scala.md
[main/scala/mg7/dataflow.scala]: ../../../main/scala/mg7/dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: ../../../main/scala/mg7/dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: ../../../main/scala/mg7/dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../../../main/scala/mg7/loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../../../main/scala/mg7/loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../../../main/scala/mg7/loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../../../main/scala/mg7/loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../../../main/scala/mg7/loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../../../main/scala/mg7/loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: ../../../main/scala/mg7/loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: ../../../main/scala/mg7/loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: ../../../main/scala/mg7/package.scala.md
[main/scala/mg7/parameters.scala]: ../../../main/scala/mg7/parameters.scala.md
[test/scala/mg7/counts.scala]: counts.scala.md
[test/scala/mg7/lca.scala]: lca.scala.md
[test/scala/mg7/pipeline.scala]: pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: taxonomy.scala.md