package era7bio.mg7test

import ohnosequences.mg7._, loquats._, dataflows._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._, utils._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._

import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._

import com.amazonaws.services.s3.transfer._
import com.amazonaws.auth._, profile._

import era7.defaults.loquats._

import better.files._

case object rna16sRefDB extends ReferenceDB(
  era7bio.db.rna16s.dbName,
  era7bio.db.rna16s.release.blastDBS3,
  era7bio.db.rna16s.release.id2taxasS3
)

case object referenceDBPipeline {

  val splitInputs: Map[ID, S3Resource] = Map(
    "refdb" -> S3Resource(era7bio.db.rna16s.filter2.accepted.fasta.s3)
  )

  case object testParameters extends MG7Parameters(
    outputS3Folder = { (sampleID, step) =>
      S3Folder("era7p", "mg7-test/data/out") /
        generated.metadata.mg7.version /
        sampleID /
        step /
    },
    // NOTE this does not have any influence
    readsLength = bp250,
    splitChunkSize = 100,
    splitInputFormat = FastaInput,
    blastCommand = blastn,
    blastOutRec  = defaultBlastOutRec,
    blastOptions = defaultBlastnOptions.update(
      num_threads(2)              ::
      word_size(150)              ::
      evalue(BigDecimal(1E-100))  ::
      /* We're going to use all hits to do global sample-coherent assignment. But not now, so no reason for this to be huge */
      max_target_seqs(10000)      ::
      perc_identity(98.0)         ::
      *[AnyDenotation]
    ).value,
    referenceDBs = Set(rna16sRefDB)
  )
  {

    /* The only basic thing we require is at least 100% **query** coverage. If we miss sequences this way, this should be solved through trimming/quality filtering */
    override def blastFilter(row: csv.Row[BlastOutRecKeys]): Boolean =
      ( row.select(outputFields.qcovs).toDouble >= 100 ) &&
      /* IMPORTANT: exclude the query from the results */
      ( row.select(outputFields.qseqid) != row.select(outputFields.sseqid) )
  }

  lazy val dataflow = NoFlashDataflow(testParameters)(splitInputs)

  /* This class is a default loquat configuration for this test */
  abstract class MG7TestLoquatConfig(
    val loquatName: String,
    val dataMappings: List[AnyDataMapping],
    val workersNumber: Int = 2
  ) extends Era7LoquatConfig {

    val metadata: AnyArtifactMetadata = generated.metadata.mg7

    val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1)),
      groupSize = AutoScalingGroupSize(0, workersNumber, workersNumber*2)
    )

    val terminationConfig = TerminationConfig(
      terminateAfterInitialDataMappings = true
    )
  }

  /*
    ### mg7 steps

    These objects define the mg7 pipeline steps. You need to run them in the order they are written here.

    For running them, go to the scala console and type

    ```
    era7bio.mg7test.BeiMockPipeline.xyzLoquat.deploy(era7.defaults.yourUser)
    ```
  */

  case object splitConfig extends MG7TestLoquatConfig("split", dataflow.splitDataMappings)
  case object splitLoquat extends Loquat(splitConfig, splitDataProcessing(testParameters))

  case object blastConfig extends MG7TestLoquatConfig("blast", dataflow.blastDataMappings, 100) {
    // NOTE: we don't want to check input objects here because they are too many and
    //   checking them one by one will take too long and likely fail
    override val checkInputObjects = false

    override val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, c3.large),
      purchaseModel = Spot(maxPrice = Some(0.025)),
      groupSize = AutoScalingGroupSize(0, workersNumber, workersNumber)
    )
  }
  case object blastLoquat extends Loquat(blastConfig, blastDataProcessing(testParameters))


  case object assignConfig extends MG7TestLoquatConfig("assign", dataflow.assignDataMappings, 10) {
    override val checkInputObjects = false

    override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 10)

    override val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.xlarge),
      purchaseModel = Spot(maxPrice = Some(0.05)),
      groupSize = AutoScalingGroupSize(0, workersNumber, workersNumber)
    )
  }
  case object assignLoquat extends Loquat(assignConfig, assignDataProcessing(testParameters))

  case object mergeConfig extends MG7TestLoquatConfig("merge", dataflow.mergeDataMappings) {
    override val skipEmptyResults = false

    override val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, c3.large),
      purchaseModel = Spot(maxPrice = Some(0.05)),
      groupSize = AutoScalingGroupSize(0, workersNumber, workersNumber*2)
    )
  }
  case object mergeLoquat extends Loquat(mergeConfig, mergeDataProcessing)

  case object countConfig extends MG7TestLoquatConfig("count", dataflow.countDataMappings, 1)
  case object countLoquat extends Loquat(countConfig, countDataProcessing)
}
