package ohnosequences.metagenomica



import ohnosequences.datasets._, dataSets._, fileLocations._, s3Locations._, illumina._, reads._

import ohnosequences.cosas._, typeSets._, types._, records._, properties._
import ohnosequences.cosas.ops.typeSets._

import ohnosequences.loquat._, utils._

import ohnosequences.statika.bundles._
import ohnosequences.statika.aws._

import ohnosequences.awstools._, regions.Region._
import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import com.amazonaws.auth.InstanceProfileCredentialsProvider

import ohnosequences.flash.api._
import ohnosequences.flash.data._

import ohnosequences.blast.api._
import ohnosequences.blast.data._

import ohnosequences.metagenomica.configuration._

import era7.project.loquats._

import better.files._


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

    // val managerConfig = ManagerConfig(
    //   instanceType = m3.medium,
    //   purchaseModel = SpotAuto
    // )

    val workersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = SpotAuto,
      groupSize = AutoScalingGroupSize(0, 1, 10)
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
          readsFastq.inS3(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq") :~:
          testData.stats.inS3(commonS3Prefix / "flash-test" / s"${sampleId}.stats.txt") :~:
          ∅
      )
    }
  }

  case object flashLoquat extends TestLoquat(flashConfig)



  case object splitConfig extends TestLoquatConfig(splitDataProcessing) {

    val dataMappings: List[DataMapping[DataProcessing]] = flashConfig.dataMappings.map { flashData =>
      DataMapping(flashData.id, dataProcessing)(
        // remoteInput = flashData.remoteOutput.take[(readsFastq.type := S3DataLocation) :~: ∅],
        remoteInput = flashData.remoteOutput.take[DataMapping[DataProcessing]#RemoteInput],
        remoteOutput =
          readsChunks.inS3(commonS3Prefix / "split-test" / flashData.id /) :~:
          ∅
      )
    }
  }

  case object splitLoquat extends TestLoquat(splitConfig)




  case object blastDataProcessing extends BlastDataProcessing(testData)

  case object blastConfig extends TestLoquatConfig(blastDataProcessing) {

    lazy val dataMappings: List[DataMapping[DataProcessing]] =
      splitConfig.dataMappings flatMap { splitData: DataMapping[splitDataProcessing.type] =>

        // CAUTION: this should be initialized on the manager instance
        lazy val s3 = S3.create(new InstanceProfileCredentialsProvider())
        lazy val s3address: AnyS3Address =
          splitData.remoteOutput.lookup[readsChunks.type := S3DataLocation].value.location
        lazy val objects: List[S3Object] = s3.listObjects(s3address.bucket, s3address.key)

        objects.zipWithIndex.map { case (obj, n) =>
          DataMapping(splitData.id, dataProcessing)(
            remoteInput = readsFastq.inS3(obj) :~: ∅,
            remoteOutput =
              testData.blastOut.inS3(commonS3Prefix / "blast-test" / splitData.id / s"blast.${n}.csv") :~:
              ∅
          )
        }
    }
  }

  case object blastLoquat extends TestLoquat(blastConfig)



  case object mergeConfig extends TestLoquatConfig(mergeDataProcessing) {

    lazy val dataMappings: List[DataMapping[DataProcessing]] =
      splitConfig.dataMappings map { splitData =>

        DataMapping(splitData.id, dataProcessing)(
          remoteInput =
            // blastChunks.inS3(commonS3Prefix / "blast-test" / splitData.id /) :~:
            blastChunks.inS3(commonS3Prefix / "split-test" / splitData.id /) :~:
            ∅,
          remoteOutput =
            blastResult.inS3(commonS3Prefix / "merge-test" / s"${splitData.id}.fastq") :~:
            ∅
        )
    }
  }

  case object mergeLoquat extends TestLoquat(mergeConfig)



  // case object assignmentDataProcessing extends AssignmentDataProcessing(testData)
  //
  // case object assignmentConfig extends TestLoquatConfig(assignmentDataProcessing) {
  //
  //   val dataMappings: List[DataMapping[DataProcessing]] = sampleIds map { sampleId =>
  //     DataMapping(sampleId, dataProcessing)(
  //       // TODO:
  //       remoteInput =
  //         testData.blastOut.inS3(commonS3Prefix / "blast-test" / s"${sampleId}.blast.partial.csv") :~:
  //         ∅,
  //       remoteOutput =
  //         lcaCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.lca.csv") :~:
  //         bbhCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.bbh.csv") :~:
  //         ∅
  //     )
  //   }
  // }
  //
  // case object assignmentLoquat extends TestLoquat(assignmentConfig)
  //
  //
  // // not needed!
  // // case object countingDataProcessing extends CountingDataProcessing(testData)
  //
  // case object countingConfig extends TestLoquatConfig(countingDataProcessing) {
  //
  //   val dataMappings: List[DataMapping[DataProcessing]] = assignmentConfig.dataMappings.map { assignmentData =>
  //     DataMapping(assignmentData.id, dataProcessing)(
  //       remoteInput = assignmentData.remoteOutput,
  //       remoteOutput =
  //         lcaCountsCSV.inS3(commonS3Prefix / "counting-test" / s"${assignmentData.id}.lca.counts.csv") :~:
  //         bbhCountsCSV.inS3(commonS3Prefix / "counting-test" / s"${assignmentData.id}.bbh.counts.csv") :~:
  //         ∅
  //     )
  //   }
  // }
  //
  // case object countingLoquat extends TestLoquat(countingConfig)

}
