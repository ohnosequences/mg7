package ohnosequences.test.mg7

import ohnosequences.test.mg7.testDefaults._
import ohnosequences.mg7._, loquats._, dataflows._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._
import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._

case object BeiMockPipeline {

  val commonS3Prefix = S3Folder("resources.ohnosequences.com", "ohnosequences/mg7/mock-communities-data/illumina/")

  // TODO move all this to the testData object
  /* For now we are only testing one sample */
  val sampleIDs: List[SampleID] = List(
    "ERR1049996"
    // "ERR1049997",
    // "ERR1049998",
    // "ERR1049999",
    // "ERR1050000",
    // "ERR1050001"
  )

  val inputSamples: Map[SampleID, (S3Resource, S3Resource)] = sampleIDs.map {
    id =>
      id -> ((
        S3Resource(commonS3Prefix / s"${id}_1_val_1.fq.gz"),
        S3Resource(commonS3Prefix / s"${id}_2_val_2.fq.gz")
      ))
    }
    .toMap

  lazy val dataflow = FullDataflow(testDefaults.Illumina.parameters)(inputSamples)

  /*
    ### mg7 steps

    These objects define the mg7 pipeline steps. You need to run them in the order they are written here.

    For running them, go to the scala console and type

    ```
    era7bio.mg7test.BeiMockPipeline.xyzLoquat.deploy(era7.defaults.yourUser)
    ```
  */
  case object flashConfig extends TestLoquatConfig("flash", dataflow.flashDataMappings)
  case object flashLoquat extends Loquat(flashConfig, flashDataProcessing(testDefaults.Illumina.parameters))

  case object splitConfig extends TestLoquatConfig("split", dataflow.splitDataMappings)
  case object splitLoquat extends Loquat(splitConfig, splitDataProcessing(testDefaults.Illumina.parameters))

  case object blastConfig extends TestLoquatConfig("blast", dataflow.blastDataMappings) {
    // NOTE: we don't want to check input objects here because they are too many and
    //   checking them one by one will take too long and likely fail
    override val checkInputObjects = false
    override lazy val workersConfig = testDefaults.Illumina.blastWorkers
  }
  case object blastLoquat extends Loquat(blastConfig, blastDataProcessing(testDefaults.Illumina.parameters))

  case object assignConfig extends TestLoquatConfig("assign", dataflow.assignDataMappings) {

    override val checkInputObjects = false
    override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 10)
    override lazy val workersConfig = testDefaults.Illumina.assignWorkers
  }
  case object assignLoquat extends Loquat(assignConfig, assignDataProcessing(testDefaults.Illumina.parameters))

  case object mergeConfig extends TestLoquatConfig("merge", dataflow.mergeDataMappings) {

    override val skipEmptyResults = false
    override lazy val workersConfig: AnyWorkersConfig = testDefaults.Illumina.mergeWorkers
  }
  case object mergeLoquat extends Loquat(mergeConfig, mergeDataProcessing)

  case object countConfig extends TestLoquatConfig("count", dataflow.countDataMappings)
  case object countLoquat extends Loquat(countConfig, countDataProcessing)
}
