package ohnosequences.metagenomica.flash

import ohnosequences.datasets._, s3Locations._

import ohnosequences.cosas.typeSets._

import ohnosequences.loquat.utils._
import ohnosequences.loquat.configs._
import ohnosequences.loquat.dataMappings._
import ohnosequences.loquat.instructions._

import ohnosequences.statika.bundles._
import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._

import ohnosequences.awstools._, regions.Region._, s3.ObjectAddress, ec2.InstanceType._
import ohnosequences.awstools.autoscaling._

import java.io.File
import era7.project._, loquats._


trait flashConfigTest extends Era7LoquatConfig {

  type FlashInstructions <: AnyFlashInstructions
  val  flashInstructions: FlashInstructions

  //val metadata: AnyArtifactMetadata = generated.metadata.CobetiaRna

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
        instructions = flashInstructions
      )(remoteInput =
          flashInstructions.reads1.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/reads/ERR567374_1.fastq.gz")) :~:
          flashInstructions.reads2.atS3("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/reads/ERR567374_2.fastq.gz") :~:
          ∅,
        remoteOutput =
          flashInstructions.merged.atS3("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.merged.fastq") :~:
          flashInstructions.stats.atS3("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.stats.txt") :~:
          ∅,
      )
    )

}


case object flashTest {
  case object testInstructions extends AnyFlashInstructions {

    type ReadsType = PairedEnd[InsertSize, bp300.type]
    val readsType = illumina.PairedEnd(bp300, InsertSize(3000))

    type Reads1 = ???
    val reads1 = ???

    type Reads2 = ???
    val reads2 = ???
  }

  case object testConfig extends flashConfigTest {

    type FlashInstructions = testInstructions.type
    val  flashInstructions = testInstructions
  }

  case object testLoquat extends Loquat(testConfig, testInstructions)
}
