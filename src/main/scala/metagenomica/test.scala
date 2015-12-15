package ohnosequences.metagenomica



import ohnosequences.datasets._, s3Locations._, illumina._

import ohnosequences.cosas._, typeSets._, types._, properties._

import ohnosequences.loquat._

import ohnosequences.statika.bundles._

import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import com.amazonaws.auth.InstanceProfileCredentialsProvider

import ohnosequences.blast.api._

import era7.project.loquats._


case object test {
  import ohnosequences.blast.api.outputFields._

  case object blastOutRec extends BlastOutputRecord(
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

  case object testParameters extends MG7Parameters(
    readsType = illumina.PairedEnd(bp300, InsertSize(3000)),
    blastOutRec = blastOutRec
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

    type DataProcessing <: AnyDataProcessingBundle
    val  dataProcessing: DataProcessing

    val dataMappings: List[DataMapping[DataProcessing]]
  }

  abstract class TestLoquatConfig[DP <: AnyDataProcessingBundle]
    (val loquatName: String, val dataProcessing: DP) extends AnyTestLoquatConfig {

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


  case object flashConfig extends TestLoquatConfig("flash", flashDataProcessing(testParameters)) {

    val dataMappings: List[DataMapping[DataProcessing]] = sampleIds map { sampleId =>
      DataMapping(sampleId, dataProcessing)(
        remoteInput =
          data.pairedReads1.inS3(commonS3Prefix / "reads" / s"${sampleId}_1.fastq.gz") :~:
          data.pairedReads2.inS3(commonS3Prefix / "reads" / s"${sampleId}_2.fastq.gz") :~:
          ∅,
        remoteOutput =
          data.mergedReads.inS3(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq") :~:
          data.flashStats.inS3(commonS3Prefix / "flash-test" / s"${sampleId}.stats.txt") :~:
          ∅
      )
    }
  }

  case object flashLoquat extends TestLoquat(flashConfig)



  case object splitConfig extends TestLoquatConfig("split", splitDataProcessing) {

    val dataMappings: List[DataMapping[DataProcessing]] = flashConfig.dataMappings.map { flashData =>
      DataMapping(flashData.id, dataProcessing)(
        // remoteInput = flashData.remoteOutput.take[(readsChunk.type := S3DataLocation) :~: ∅],
        remoteInput = flashData.remoteOutput.take[DataMapping[DataProcessing]#RemoteInput],
        remoteOutput = data.readsChunks.inS3(commonS3Prefix / "split-test" / flashData.id /) :~: ∅
      )
    }
  }

  case object splitLoquat extends TestLoquat(splitConfig)


  case object blastConfig extends TestLoquatConfig("blast", blastDataProcessing(testParameters)) {

    lazy val dataMappings: List[DataMapping[DataProcessing]] =
      splitConfig.dataMappings flatMap { splitData: DataMapping[splitDataProcessing.type] =>

        // CAUTION: this should be initialized on the manager instance
        lazy val s3 = S3.create(new InstanceProfileCredentialsProvider())
        lazy val s3address: AnyS3Address =
          splitData.remoteOutput.lookup[data.readsChunks.type := S3DataLocation].value.location
        lazy val objects: List[S3Object] = s3.listObjects(s3address.bucket, s3address.key)

        objects.zipWithIndex.map { case (obj, n) =>
          DataMapping(splitData.id, dataProcessing)(
            remoteInput = data.readsChunk.inS3(obj) :~: ∅,
            remoteOutput = data.blastChunkOut.inS3(commonS3Prefix / "blast-test" / splitData.id / s"blast.${n}.csv") :~: ∅
          )
        }
    }
  }

  case object blastLoquat extends TestLoquat(blastConfig)



  case object mergeConfig extends TestLoquatConfig("merge", mergeDataProcessing) {

    lazy val dataMappings: List[DataMapping[DataProcessing]] =
      splitConfig.dataMappings map { splitData =>

        DataMapping(splitData.id, dataProcessing)(
          remoteInput =
            // blastChunks.inS3(commonS3Prefix / "blast-test" / splitData.id /) :~:
            data.blastChunks.inS3(commonS3Prefix / "split-test" / splitData.id /) :~:
            ∅,
          remoteOutput =
            data.blastResult.inS3(commonS3Prefix / "merge-test" / s"${splitData.id}.fastq") :~:
            ∅
        )
    }
  }

  case object mergeLoquat extends TestLoquat(mergeConfig)


  case object assignmentConfig extends TestLoquatConfig("assignment", assignmentDataProcessing(testParameters)) {

    val dataMappings: List[DataMapping[DataProcessing]] = sampleIds map { sampleId =>
      DataMapping(sampleId, dataProcessing)(
        // TODO:
        remoteInput =
          data.blastResult.inS3(commonS3Prefix / "blast-test" / s"${sampleId}.blast.partial.csv") :~:
          ∅,
        remoteOutput =
          data.lcaCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.lca.csv") :~:
          data.bbhCSV.inS3(commonS3Prefix / "assignment-test" / s"${sampleId}.bbh.csv") :~:
          ∅
      )
    }
  }

  case object assignmentLoquat extends TestLoquat(assignmentConfig)


  case object countingConfig extends TestLoquatConfig("counting", countingDataProcessing) {

    val dataMappings: List[DataMapping[DataProcessing]] = assignmentConfig.dataMappings.map { assignmentData =>
      DataMapping(assignmentData.id, dataProcessing)(
        remoteInput = assignmentData.remoteOutput,
        remoteOutput =
          data.lcaCountsCSV.inS3(commonS3Prefix / "counting-test" / s"${assignmentData.id}.lca.counts.csv") :~:
          data.bbhCountsCSV.inS3(commonS3Prefix / "counting-test" / s"${assignmentData.id}.bbh.counts.csv") :~:
          ∅
      )
    }
  }

  case object countingLoquat extends TestLoquat(countingConfig)

}
