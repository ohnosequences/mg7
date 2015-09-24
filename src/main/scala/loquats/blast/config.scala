package ohnosequences.metagenomica.loquats.blast

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

import blastDataProcessing._


trait AnyBlastConfig extends Era7LoquatConfig { config =>

  type DataProcessing <: AnyBlastDataProcessing
  val  dataProcessing: DataProcessing

  type BlastDataMapping = AnyDataMapping { type DataProcessing = config.DataProcessing }
  val  dataMappings: List[BlastDataMapping]
}

abstract class BlastConfig[D <: AnyBlastDataProcessing](val dataProcessing: D) extends AnyBlastConfig {

  type DataProcessing = D
}

//////////////////////////////////////////////////////////////////////////////////////

case object blastTest {

  // TODO: move it to datasets
  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })

  case object FastqDataType extends AnyDataType
  case object fastqInput extends Data(FastqDataType, "foo.fastq")


  case object blastOutput extends BlastOutput(blastOutputType, "blast.out.csv")


  case object testDataProcessing extends BlastDataProcessing(fastqInput, blastOutput)

  case object testConfig extends BlastConfig(testDataProcessing) {

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

    val dataMappings: List[BlastDataMapping] =
      List(
        DataMapping(
          "ERR567374_1",
          dataProcessing
        )(remoteInput =
            fastqInput.inS3Object(S3Object("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/flash-test/ERR567374_1.merged.fastq")) :~:
            ∅,
          remoteOutput =
            blastOutput.inS3Object(S3Object("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592/blast-test/ERR567374_1.merged.blast.csv")) :~:
            ∅
        )
      )

  }

  case object testLoquat extends Loquat(testConfig, testDataProcessing)
}
