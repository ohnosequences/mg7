package ohnosequences.metagenomica.loquats.blast

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

  type BlastDataMapping = AnyDataMapping { type DataProcessing = config.DataProcessing }
  val  dataMappings: List[BlastDataMapping]
}

abstract class BlastConfig[D <: AnyFlashDataProcessing](val dataProcessing: D) extends AnyFlashConfig {

  type DataProcessing = D
}

//////////////////////////////////////////////////////////////////////////////////////

case object blastTest {

  // TODO: move it to datasets
  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })


  case object testData extends FlashData(illumina.PairedEnd(bp300, InsertSize(3000)))

  case object testDataProcessing extends FlashDataProcessing(testData)

  case object testConfig extends BlastConfig(testDataProcessing) {

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

    val dataMappings: List[BlastDataMapping] =
      List(
        DataMapping(
          "ERR567374_1",
          dataProcessing
        )(remoteInput =
          dataProcessing.data.merged.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.merged.fastq")) :~:
          ∅,
          remoteOutput =
            dataProcessing.blastOutput.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/blast-test/ERR567374_1.merged.blast.csv")) :~:
            ∅
        )
      )

  }

  case object testLoquat extends Loquat(testConfig, testDataProcessing)
}
