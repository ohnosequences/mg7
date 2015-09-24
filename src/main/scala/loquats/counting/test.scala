package ohnosequences.metagenomica.loquats.counting

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

import ohnosequences.awstools._, regions.Region._, ec2.InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._

import era7.project.loquats._

import java.io.File

import ohnosequences.metagenomica.loquats.assignment.dataProcessing._
import ohnosequences.metagenomica.loquats.counting.dataProcessing._


case object countingTest {

  case object testConfig extends Era7LoquatConfig {

    val metadata: AnyArtifactMetadata = generated.metadata.Metagenomica

    val managerConfig = ManagerConfig(
      instanceType = m3.medium,
      purchaseModel = SpotAuto
    )

    val workersConfig = WorkersConfig(
      instanceType = m3.medium,
      purchaseModel = SpotAuto,
      groupSize = WorkersGroupSize(0, 1, 10)
    )

    val terminationConfig = TerminationConfig(
      terminateAfterInitialDataMappings = true
    )

    val dataMappings: List[AnyDataMapping] =
      List(
        DataMapping(
          "ERR567374_1",
          countingDataProcessing
        )(remoteInput =
            lcaCSV.inS3Object(S3Object(
              "resources.ohnosequences.com",
              "16s/public-datasets/PRJEB6592/assignment-test/ERR567374_1.lca.csv"
            )) :~:
            bbhCSV.inS3Object(S3Object(
              "resources.ohnosequences.com",
              "16s/public-datasets/PRJEB6592/assignment-test/ERR567374_1.bbh.csv"
            )) :~: ∅,
          remoteOutput =
            lcaCountsCSV.inS3Object(S3Object(
              "resources.ohnosequences.com",
              "16s/public-datasets/PRJEB6592/counting-test/ERR567374_1.lca.counts.csv"
            )) :~:
            bbhCountsCSV.inS3Object(S3Object(
              "resources.ohnosequences.com",
              "16s/public-datasets/PRJEB6592/counting-test/ERR567374_1.bbh.counts.csv"
            )) :~:
            ∅
        )
      )

  }

  case object testLoquat extends Loquat(testConfig, countingDataProcessing)
}
