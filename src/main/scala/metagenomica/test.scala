package ohnosequences.metagenomica



import ohnosequences.datasets._, dataSets._, fileLocations._, s3Locations._, illumina._, reads._

import ohnosequences.cosas._, typeSets._, types._, records._, properties._
import ohnosequences.cosas.ops.typeSets._

import ohnosequences.loquat._, utils._, configs._, dataMappings._, dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._

import ohnosequences.awstools._, regions.Region._, ec2.InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._

import ohnosequences.flash.api._
import ohnosequences.flash.data._

import ohnosequences.blast.api._
import ohnosequences.blast.data._

import ohnosequences.metagenomica.configuration._

import era7.project.loquats._

import java.io.File


case object test {
  import ohnosequences.blast.api.outputFields._

  case object outRec extends BlastOutputRecord(
      qseqid   :&:
      qlen     :&:
      qstart   :&:
      qend     :&:
      sseqid   :&:
      slen     :&:
      sstart   :&:
      send     :&:
      bitscore :&:
      sgi      :&:
      □
    )

  case object testData extends MetagenomicaData(
    readsType = illumina.PairedEnd(bp300, InsertSize(3000)),
    blastOutRec = outRec
  )

  trait AnyTestLoquatConfig extends Era7LoquatConfig { config =>

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

    // TODO: should we limit it to only MG7-related things?
    type DataProcessing <: AnyDataProcessingBundle
    val  dataProcessing: DataProcessing

    val dataMappings: List[DataMapping[DataProcessing]]
  }

  abstract class TestLoquatConfig[DP <: AnyDataProcessingBundle]
    (val dataProcessing: DP) extends AnyTestLoquatConfig {
    type DataProcessing = DP
  }

  abstract class TestLoquat[LC <: AnyTestLoquatConfig](lc: LC)
    extends Loquat[LC, LC#DataProcessing](lc, lc.dataProcessing)

}

case object testLoquats {
  import test._
  import loquats._

  val sampleIds: List[String] = List("ERR567374")
  val commonS3Prefix = S3Folder("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592")

  // !!!
  import testData._


  case object flashDataProcessing extends FlashDataProcessing(testData)

  case object flashConfig extends TestLoquatConfig(flashDataProcessing) {
    val dataMappings: List[DataMapping[DataProcessing]] = sampleIds map { sampleId =>
      DataMapping(sampleId, dataProcessing)(
        remoteInput =
          testData.reads1.inS3(commonS3Prefix / "reads" / s"${sampleId}_1.fastq.gz") :~:
          testData.reads2.inS3(commonS3Prefix / "reads" / s"${sampleId}_2.fastq.gz") :~:
          ∅,
        remoteOutput =
          testData.merged.inS3(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq") :~:
          testData.stats.inS3(commonS3Prefix / "flash-test" / s"${sampleId}.stats.txt") :~:
          ∅
      )
    }
  }

  case object flashLoquat extends TestLoquat(flashConfig)



  case object blastDataProcessing extends BlastDataProcessing(testData)

  case object blastConfig extends TestLoquatConfig(blastDataProcessing) {
    val dataMappings: List[DataMapping[DataProcessing]] = sampleIds map { sampleId =>
      DataMapping(sampleId, dataProcessing)(
        remoteInput =
          testData.merged.inS3(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq") :~:
          ∅,
        remoteOutput =
          testData.blastOut.inS3(commonS3Prefix / "blast-test" / s"${sampleId}.blast.csv") :~:
          ∅
      )
    }
  }

  case object blastLoquat extends TestLoquat(blastConfig)


  case object assignmentDataProcessing extends AssignmentDataProcessing(testData)

  case object assignmentConfig extends TestLoquatConfig(assignmentDataProcessing) {
    val dataMappings: List[DataMapping[DataProcessing]] = sampleIds map { sampleId =>
      DataMapping(sampleId, dataProcessing)(
        remoteInput =
          testData.blastOut.inS3(commonS3Prefix / "blast-test" / s"${sampleId}.blast.partial.csv") :~:
          ∅,
        remoteOutput =
          lcaCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.lca.csv") :~:
          bbhCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.bbh.csv") :~:
          ∅
      )
    }
  }

  case object assignmentLoquat extends TestLoquat(assignmentConfig)


  // not needed!
  // case object countingDataProcessing extends CountingDataProcessing(testData)

  case object countingConfig extends TestLoquatConfig(countingDataProcessing) {
    val dataMappings: List[DataMapping[DataProcessing]] = sampleIds map { sampleId =>
      DataMapping(sampleId, dataProcessing)(
        remoteInput =
          lcaCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.lca.csv") :~:
          bbhCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.bbh.csv") :~:
          ∅,
        remoteOutput =
          lcaCountsCSV.inS3(commonS3Prefix / "counting-test" / s"${sampleId}.lca.counts.csv") :~:
          bbhCountsCSV.inS3(commonS3Prefix / "counting-test" / s"${sampleId}.bbh.counts.csv") :~:
          ∅
      )
    }
  }

  case object countingLoquat extends TestLoquat(countingConfig)

}
