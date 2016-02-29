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
    blastOutRec = blastOutRec,
    refDB = bundles.blast16s
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

  val dataflow = StandardDataflow(testParameters)(inputSamples, outputS3Folder)


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
