package ohnosequences.metagenomica.flash

import ohnosequences.datasets._, dataSets._, fileLocations._, s3Locations._, illumina._, reads._

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

import java.io.File



trait AnyFlashConfig extends Era7LoquatConfig { config =>

  type DataProcessing <: AnyFlashDataProcessing
  val  dataProcessing: DataProcessing

  type FlashDataMappring = AnyDataMapping { type DataProcessing = config.DataProcessing }
  val  dataMappings: List[FlashDataMappring]
}

abstract class FlashConfig[D <: AnyFlashDataProcessing](val dataProcessing: D) extends AnyFlashConfig {

  type DataProcessing = D
}

//////////////////////////////////////////////////////////////////////////////////////

case object flashTest {

  // TODO: move it to datasets
  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })


  case object testData extends FlashData(illumina.PairedEnd(bp300, InsertSize(3000)))

  case object testDataProcessing extends FlashDataProcessing(testData)

  case object testConfig extends FlashConfig(testDataProcessing) {

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

    val dataMappings: List[FlashDataMappring] =
      List(
        DataMapping(
          "ERR567374_1",
          dataProcessing
        )(remoteInput =
            dataProcessing.data.reads1.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/reads/ERR567374_1.fastq.gz")) :~:
            dataProcessing.data.reads2.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/reads/ERR567374_2.fastq.gz")) :~:
            ∅,
          remoteOutput =
            dataProcessing.data.merged.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.merged.fastq")) :~:
            dataProcessing.data.stats.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.stats.txt")) :~:
            ∅
        )
      )

  }

  case object testLoquat extends Loquat(testConfig, testDataProcessing)
}
