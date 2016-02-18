
```scala
package ohnosequences.mg7


import ohnosequences.mg7._, loquats._, dataflows._

import ohnosequences.datasets._, illumina._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._

import ohnosequences.blast.api._, outputFields._


case object test {

  case object blastOutRec extends BlastOutputRecord(
      qseqid   :×:
      qlen     :×:
      qstart   :×:
      qend     :×:
      sseqid   :×:
      slen     :×:
      sstart   :×:
      send     :×:
      bitscore :×:
      sgi      :×:
      |[AnyOutputField]
    )

  case object testParameters extends MG7Parameters(
    readsLength = bp300,
    blastOutRec = blastOutRec
  )

  val defaultAMI = AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  trait AnyTestLoquatConfig extends AnyLoquatConfig { config =>

    val metadata: AnyArtifactMetadata = generated.metadata.mg7

    val iamRoleName = "loquat.testing"
    val logsBucketName = "loquat.testing"

    val  managerConfig = ManagerConfig(
      InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1))
    )

    val workersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )

    val terminationConfig = TerminationConfig(
      terminateAfterInitialDataMappings = true
    )

    val dataMappings: List[AnyDataMapping]

    val checkInputObjects = true
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

  val inputSamples: Map[ID, (S3Object, S3Object)] = sampleIds.map { id =>
    id -> ((
      commonS3Prefix / "reads" / s"${id}_1.fastq.gz",
      commonS3Prefix / "reads" / s"${id}_2.fastq.gz"
    ))
  }.toMap

  def outputS3Folder(sample: SampleID, step: StepName): S3Folder =
    commonS3Prefix / s"${step}-test" / sample /

  val dataflow = StandardDataflow(inputSamples, outputS3Folder)


  case object flashConfig extends TestLoquatConfig("flash", dataflow.flashDataMappings)
  case object flashLoquat extends Loquat(flashConfig, flashDataProcessing(testParameters))

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

  case object assignmentConfig extends TestLoquatConfig("assignment", dataflow.assignmentDataMappings)
  case object assignmentLoquat extends Loquat(assignmentConfig, assignmentDataProcessing(testParameters))

  case object countingConfig extends TestLoquatConfig("counting", dataflow.countingDataMappings)
  case object countingLoquat extends Loquat(countingConfig, countingDataProcessing)

}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: ../../../main/scala/metagenomica/bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: ../../../main/scala/metagenomica/bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: ../../../main/scala/metagenomica/bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: ../../../main/scala/metagenomica/bundles/blast.scala.md
[main/scala/metagenomica/bundles/blast16s.scala]: ../../../main/scala/metagenomica/bundles/blast16s.scala.md
[main/scala/metagenomica/bundles/flash.scala]: ../../../main/scala/metagenomica/bundles/flash.scala.md
[main/scala/metagenomica/bundles/gis.scala]: ../../../main/scala/metagenomica/bundles/gis.scala.md
[main/scala/metagenomica/data.scala]: ../../../main/scala/metagenomica/data.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: ../../../main/scala/metagenomica/dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../../../main/scala/metagenomica/loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../../../main/scala/metagenomica/loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../../../main/scala/metagenomica/loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../../../main/scala/metagenomica/loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../../../main/scala/metagenomica/loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../../../main/scala/metagenomica/loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../../../main/scala/metagenomica/package.scala.md
[main/scala/metagenomica/parameters.scala]: ../../../main/scala/metagenomica/parameters.scala.md
[test/scala/bundles.scala]: ../bundles.scala.md
[test/scala/lca.scala]: ../lca.scala.md
[test/scala/metagenomica/pipeline.scala]: pipeline.scala.md