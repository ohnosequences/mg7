package ohnosequences.metagenomica



import ohnosequences.datasets._, illumina._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._

import ohnosequences.blast.api._

// import era7.project.loquats._


case object test {
  import ohnosequences.blast.api.outputFields._

  case object blastOutRec extends BlastOutputRecord(
      qseqid   :×:
      qlen     :×:
      qstart   :×:
      qend     :×:
      sseqid   :×:
      slen     :×:
      sstart   :×:
      send     :×:
      bitscore :×:
      sgi      :×:
      |[AnyOutputField]
    )

  case object testParameters extends MG7Parameters(
    readsLength = bp300,
    blastOutRec = blastOutRec
  )

  val defaultAMI = AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  trait AnyTestLoquatConfig extends AnyLoquatConfig { config =>

    val metadata: AnyArtifactMetadata = generated.metadata.Metagenomica

    val iamRoleName = "loquat.testing"
    val logsBucketName = "loquat.testing"

    val  managerConfig = ManagerConfig(
      InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1))
    )

    val workersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )

    val terminationConfig = TerminationConfig(
      terminateAfterInitialDataMappings = true
    )

    val dataMappings: List[AnyDataMapping]

    val checkInputObjects = true
  }

  abstract class TestLoquatConfig(val loquatName: String) extends AnyTestLoquatConfig

  abstract class TestLoquat[
    LC <: AnyTestLoquatConfig,
    DP <: AnyDataProcessingBundle
  ](lc: LC, dp: DP)
    extends Loquat[LC, DP](lc, dp)

  val testUser = LoquatUser(
    email = "aalekhin@ohnosequences.com",
    localCredentials = new ProfileCredentialsProvider("default"),
    keypairName = "aalekhin"
  )

}

case object testLoquats {
  import test._
  import loquats._

  val sampleIds: List[String] = List("ERR567374")
  val commonS3Prefix = S3Folder("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592")


  case object flashConfig extends TestLoquatConfig("flash") {

    val dataMappings: List[AnyDataMapping] = sampleIds map { sampleId =>
      DataMapping(sampleId)(
        remoteInput = Map(
          data.pairedReads1 -> S3Resource(commonS3Prefix / "reads" / s"${sampleId}_1.fastq.gz"),
          data.pairedReads2 -> S3Resource(commonS3Prefix / "reads" / s"${sampleId}_2.fastq.gz")
        ),
        remoteOutput = Map(
          data.mergedReads -> S3Resource(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq"),
          data.flashStats -> S3Resource(commonS3Prefix / "flash-test" / s"${sampleId}.stats.txt")
        )
      )
    }
  }

  case object flashLoquat extends TestLoquat(flashConfig, flashDataProcessing(testParameters))


  case object splitConfig extends TestLoquatConfig("split") {

    val dataMappings: List[AnyDataMapping] = flashConfig.dataMappings.map { flashDM =>
      DataMapping(flashDM.id)(
        remoteInput = Map(
          data.mergedReads -> flashDM.remoteOutput(data.mergedReads)
        ),
        remoteOutput = Map(
          data.readsChunks -> S3Resource(commonS3Prefix / "split-test" / flashDM.id /)
        )
      )
    }
  }

  case object splitLoquat extends TestLoquat(splitConfig, splitDataProcessing)


  case object blastConfig extends TestLoquatConfig("blast") {

    // NOTE: we don't want to check input objects here because they are too many and checking them one by one will likely fail
    override val checkInputObjects = false

    lazy val dataMappings: List[AnyDataMapping] = splitConfig.dataMappings.flatMap { splitDM =>

      lazy val s3 = S3.create(
        new AWSCredentialsProviderChain(
          new InstanceProfileCredentialsProvider(),
          new ProfileCredentialsProvider()
        )
      )

      lazy val s3address: AnyS3Address = splitDM.remoteOutput(data.readsChunks).resource
      lazy val objects: List[S3Object] = s3.listObjects(s3address.bucket, s3address.key)

      objects.zipWithIndex.map { case (obj, n) =>
        DataMapping(s"${splitDM.id}.${n}")(
          remoteInput = Map(
            data.readsChunk -> S3Resource(obj)
          ),
          remoteOutput = Map(
            data.blastChunkOut -> S3Resource(commonS3Prefix / "blast-test" / splitDM.id / s"blast.${n}.csv")
          )
        )
      }
    }
  }

  case object blastLoquat extends TestLoquat(blastConfig, blastDataProcessing(testParameters))


  case object mergeConfig extends TestLoquatConfig("merge") {

    lazy val dataMappings: List[AnyDataMapping] = splitConfig.dataMappings map { splitDM =>

      DataMapping(splitDM.id)(
        remoteInput = Map(
          data.blastChunks -> S3Resource(commonS3Prefix / "blast-test" / splitDM.id /)
        ),
        remoteOutput = Map(
          data.blastResult -> S3Resource(commonS3Prefix / "merge-test" / s"${splitDM.id}.blast.csv")
        )
      )
    }
  }

  case object mergeLoquat extends TestLoquat(mergeConfig, mergeDataProcessing)


  case object assignmentConfig extends TestLoquatConfig("assignment") {

    val dataMappings: List[AnyDataMapping] = mergeConfig.dataMappings.map { mergeDM =>
      DataMapping(mergeDM.id)(
        remoteInput = mergeDM.remoteOutput,
        // Map( data.blastResult -> mergeDM.remoteOutput(data.blastResult) ),
        remoteOutput = Map(
          data.lcaCSV -> S3Resource(commonS3Prefix / "assignment-test" / s"${mergeDM.id}.lca.csv"),
          data.bbhCSV -> S3Resource(commonS3Prefix / "assignment-test" / s"${mergeDM.id}.bbh.csv")
        )
      )
    }
  }

  case object assignmentLoquat extends TestLoquat(assignmentConfig, assignmentDataProcessing(testParameters))


  case object countingConfig extends TestLoquatConfig("counting") {

    val dataMappings: List[AnyDataMapping] = assignmentConfig.dataMappings.map { assignmentDM =>
      DataMapping(assignmentDM.id)(
        remoteInput = assignmentDM.remoteOutput,
        remoteOutput = Map(
          data.lcaCountsCSV -> S3Resource(commonS3Prefix / "counting-test" / s"${assignmentDM.id}.lca.counts.csv"),
          data.bbhCountsCSV -> S3Resource(commonS3Prefix / "counting-test" / s"${assignmentDM.id}.bbh.counts.csv")
        )
      )
    }
  }

  case object countingLoquat extends TestLoquat(countingConfig, countingDataProcessing)

}
