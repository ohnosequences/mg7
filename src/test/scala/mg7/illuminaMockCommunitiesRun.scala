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

  val commonS3Prefix = S3Folder("era7p", "mg7-test/data")

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
        S3Resource(commonS3Prefix/"out"/"reads-preprocessing"/s"${id}_1_val_1.fq.gz"),
        S3Resource(commonS3Prefix/"out"/"reads-preprocessing"/s"${id}_2_val_2.fq.gz")
      ))
    }
    .toMap

  case object testParameters extends MG7Parameters(
    outputS3Folder  = { (sampleId, step) => testDefaults.outputS3Folder/sampleId/step/ },
    readsLength     = bp250,
    splitChunkSize  = 1000,
    blastCommand    = blastn,
    blastOutRec     = defaults.blastnOutputRecord,
    blastOptions    = defaults.blastnOptions.update(
        num_threads(4)              ::
        word_size(46)               ::
        evalue(BigDecimal(1E-100))  ::
        /* We're going to use all hits to do global sample-coherent assignment. But not now, so no reason for this to be huge */
        max_target_seqs(150)        ::
        /* 95% is a reasonable minimum. If it does not work, be more stringent with read preprocessing */
        perc_identity(95.0)         ::
        *[AnyDenotation]
      ).value,
    referenceDBs = Set(rna16sRefDB)
  )
  {

    /* The only basic thing we require is at least 100% **query** coverage. If we miss sequences this way, this should be solved through trimming/quality filtering */
    override def blastFilter(row: csv.Row[BlastOutRecKeys]): Boolean =
      row.select(outputFields.qcovs).toDouble  >= 100
  }

  val dataflow = FullDataflow(testParameters)(inputSamples)

  /*
    ### mg7 steps

    These objects define the mg7 pipeline steps. You need to run them in the order they are written here.

    For running them, go to the scala console and type

    ```
    era7bio.mg7test.BeiMockPipeline.xyzLoquat.deploy(era7.defaults.yourUser)
    ```
  */

  case object flashConfig extends TestLoquatConfig("flash", dataflow.flashDataMappings)
  case object flashLoquat extends Loquat(flashConfig, flashDataProcessing(testParameters))

  case object splitConfig extends TestLoquatConfig("split", dataflow.splitDataMappings)
  case object splitLoquat extends Loquat(splitConfig, splitDataProcessing(testParameters))

  case object blastConfig extends TestLoquatConfig("blast", dataflow.blastDataMappings) {
    // NOTE: we don't want to check input objects here because they are too many and
    //   checking them one by one will take too long and likely fail
    override val checkInputObjects = false

    override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, c3.large),
      purchaseModel = Spot(maxPrice = Some(0.025)),
      groupSize = AutoScalingGroupSize(0, 100, 100)
    )
  }
  case object blastLoquat extends Loquat(blastConfig, blastDataProcessing(testParameters))


  case object assignConfig extends TestLoquatConfig("assign", dataflow.assignDataMappings) {
    override val checkInputObjects = false

    override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 10)

    override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.xlarge),
      purchaseModel = Spot(maxPrice = Some(0.05)),
      groupSize = AutoScalingGroupSize(0, 6, 6)
    )
  }
  case object assignLoquat extends Loquat(assignConfig, assignDataProcessing(testParameters))


  case object mergeConfig extends TestLoquatConfig("merge", dataflow.mergeDataMappings) {
    override val skipEmptyResults = false

    override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.xlarge),
      purchaseModel = Spot(maxPrice = Some(0.05)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )
  }
  case object mergeLoquat extends Loquat(mergeConfig, mergeDataProcessing)

  case object countConfig extends TestLoquatConfig("count", dataflow.countDataMappings)
  case object countLoquat extends Loquat(countConfig, countDataProcessing)

  case object statsConfig extends TestLoquatConfig("stats", dataflow.statsDataMappings)
  case object statsLoquat extends Loquat(statsConfig, statsDataProcessing)

  case object summaryConfig extends TestLoquatConfig("summary", dataflow.summaryDataMappings)
  case object summaryLoquat extends Loquat(summaryConfig, summaryDataProcessing)
}
