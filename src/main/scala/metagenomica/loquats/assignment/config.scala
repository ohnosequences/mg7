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

import ohnosequences.metagenomica.loquats.assignment.dataProcessing._


abstract class AnyAssignmentConfig extends Era7LoquatConfig { config =>

  // type DataProcessing = assignmentDataProcessing.type
  // val  dataProcessing = assignmentDataProcessing: DataProcessing

  type AssignmentDataMapping = AnyDataMapping { type DataProcessing = assignmentDataProcessing.type }
  val  dataMappings: List[AssignmentDataMapping]
}

// abstract class AssignmentConfig[D <: AnyAssignmentDataProcessing](val dataProcessing: D) extends AnyAssignmentConfig {
//
//   type DataProcessing = D
// }

//////////////////////////////////////////////////////////////////////////////////////

case object assignmenTest {

  case object testConfig extends AnyAssignmentConfig {

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

    val dataMappings: List[AssignmentDataMapping] =
      List(
        DataMapping(
          "ERR567374_1",
          assignmentDataProcessing
        )(remoteInput =
            blastOutput.atS3(ObjectAddress(
              "resources.ohnosequences.com",
              "16s/public-datasets/PRJEB6592/blast-test/ERR567374_1.blast.partial.csv"
            )) :~: ∅,
          remoteOutput =
            lcaCSV.atS3(ObjectAddress(
              "resources.ohnosequences.com",
              "16s/public-datasets/PRJEB6592/assignment-test/ERR567374_1.lca.csv"
            )) :~:
            bbhCSV.atS3(ObjectAddress(
              "resources.ohnosequences.com",
              "16s/public-datasets/PRJEB6592/assignment-test/ERR567374_1.bbh.csv"
            )) :~:
            ∅
        )
      )

  }

  case object testLoquat extends Loquat(testConfig, assignmentDataProcessing)
}
