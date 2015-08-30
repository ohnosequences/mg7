package ohnosequences.metagenomica.flash

import flashDataProcessing._

import ohnosequences.datasets._, s3Locations._, illumina._, reads._

import ohnosequences.cosas._, typeSets._, types._
import ohnosequences.cosas.ops.typeSets._

import ohnosequences.loquat._, utils._, configs._, dataMappings._, dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._

import ohnosequences.awstools._, regions.Region._, s3.ObjectAddress, ec2.InstanceType._
import ohnosequences.awstools.autoscaling._

import ohnosequences.flash.api._
import ohnosequences.flash.data._

import era7.project.loquats._


case object flashTest {
  val readsType = illumina.PairedEnd(bp300, InsertSize(3000))

  case object reads1 extends PairedEnd1Fastq(readsType, "ERR567374_1.fastq.gz")
  case object reads2 extends PairedEnd2Fastq(readsType, "ERR567374_2.fastq.gz")

  lazy val flashOptions = flash.defaults update (
    read_len(readsType.length.length)   :~:
    max_overlap(readsType.length.length) :~: ∅
  )

  case object merged extends MergedReads(readsType, reads1, reads2, flashOptions)
  case object stats extends MergedReadsStats(merged)

  case object testInstructions extends FlashInstructions(readsType, reads1, reads2, merged, stats, flashOptions)

  case object testConfig extends Era7LoquatConfig {

    // type FlashInstructions <: AnyFlashInstructions
    // val  flashInstructions: FlashInstructions

    val metadata: AnyArtifactMetadata = generated.metadata.Metagenomica

    val managerConfig = ManagerConfig(
      instanceType = m3_medium,
      purchaseModel = SpotAuto
    )

    val workersConfig = WorkersConfig(
      instanceType = m3_medium,
      purchaseModel = SpotAuto,
      groupSize = WorkersGroupSize(0, 1, 10)
    )

    val terminationConfig = TerminationConfig(
      terminateAfterInitialDataMappings = true
    )

    val dataMappings: List[AnyDataMapping] = List(
        DataMapping(
          id = "ERR567374_1",
          dataProcessing = testInstructions
        )(remoteInput =
            testInstructions.reads1.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/reads/ERR567374_1.fastq.gz")) :~:
            testInstructions.reads2.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/reads/ERR567374_2.fastq.gz")) :~:
            ∅,
          remoteOutput =
            testInstructions.merged.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.merged.fastq")) :~:
            testInstructions.stats.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.stats.txt")) :~:
            ∅
        )
      )

  }

  case object testLoquat extends Loquat(testConfig, testInstructions)
}
