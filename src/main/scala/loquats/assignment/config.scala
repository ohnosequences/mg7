package ohnosequences.metagenomica.loquats.assignment

import ohnosequences.datasets._, dataSets._, fileLocations._, s3Locations._, illumina._, reads._

import ohnosequences.cosas._, typeSets._, types._, properties._, records._
import ohnosequences.cosas.ops.typeSets._

import ohnosequences.loquat._, utils._, configs._, dataMappings._, dataProcessing._

import ohnosequences.blast._
import ohnosequences.blast.api._, outputFields._
import ohnosequences.blast.data._

import ohnosequences.statika.bundles._
import ohnosequences.statika.aws.api._
import ohnosequences.statika.aws.amazonLinuxAMIs._

import ohnosequences.awstools._, regions.Region._, s3.ObjectAddress, ec2.InstanceType._
import ohnosequences.awstools.autoscaling._

import era7.project.loquats._

import java.io.File

import assignmentDataProcessing._


trait AnyAssignmentConfig extends Era7LoquatConfig { config =>

  type DataProcessing <: AnyAssignmentDataProcessing
  val  dataProcessing: DataProcessing

  type AssignmentDataMapping = AnyDataMapping { type DataProcessing = config.DataProcessing }
  val  dataMappings: List[AssignmentDataMapping]
}

abstract class AssignmentConfig[D <: AnyAssignmentDataProcessing](val dataProcessing: D) extends AnyAssignmentConfig {

  type DataProcessing = D
}

//////////////////////////////////////////////////////////////////////////////////////

case object assignmenTest {

  // TODO: move it to datasets
  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })

  // case object testDataProcessing extends AssignmentDataProcessing(fastqInput, blastOutput)

  // case object testConfig extends AssignmentConfig(testDataProcessing) {
  //
  //   val metadata: AnyArtifactMetadata = generated.metadata.Metagenomica
  //
  //   val managerConfig = ManagerConfig(
  //     instanceType = m3_medium,
  //     purchaseModel = SpotAuto
  //   )
  //
  //   val workersConfig = WorkersConfig(
  //     instanceType = m3_medium,
  //     purchaseModel = SpotAuto,
  //     groupSize = WorkersGroupSize(0, 1, 10)
  //   )
  //
  //   val terminationConfig = TerminationConfig(
  //     terminateAfterInitialDataMappings = true
  //   )
  //
  //   val dataMappings: List[AssignmentDataMapping] =
  //     List(
  //       DataMapping(
  //         "ERR567374_1",
  //         dataProcessing
  //       )(remoteInput =
  //           fastqInput.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.merged.fastq")) :~:
  //           ∅,
  //         remoteOutput =
  //           blastOutput.atS3(ObjectAddress("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/blast-test/ERR567374_1.merged.blast.csv")) :~:
  //           ∅
  //       )
  //     )
  //
  // }
  //
  // case object testLoquat extends Loquat(testConfig, testDataProcessing)
}
