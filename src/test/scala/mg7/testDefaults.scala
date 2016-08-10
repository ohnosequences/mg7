/*
  # Default MG7 test configuration and parameters
*/
package ohnosequences.test.mg7

import ohnosequences.mg7._, loquats._, dataflows._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._
import ohnosequences.db.rna16s
import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._
import ohnosequences.datasets.illumina._

case object testDefaults {

  lazy val mg7 = generated.metadata.mg7

  /* Output test data *is* scoped by version */
  lazy val outputS3Folder =
    S3Folder("resources.ohnosequences.com", mg7.organization)/mg7.artifact/mg7.version

  lazy val defaultOutput: (SampleID, StepName) => S3Folder =
    (sampleID, step) => outputS3Folder/sampleID/step/

  lazy val referenceDBs: Set[AnyReferenceDB] = Set(rna16sRefDB)
  /*
    ## Default Illumina parameters

    These parameters are a sensible default for Illumina reads.
  */
  case object Illumina {

    lazy val blastnOptions =
      defaults.blastnOptions.update(
        num_threads(4)              ::
        word_size(46)               ::
        evalue(BigDecimal(1E-100))  ::
        max_target_seqs(10000)      ::
        perc_identity(98.0)         ::
        *[AnyDenotation]
      )
      .value

    lazy val readLength = illumina.bp250

    case object parameters extends MG7Parameters(
      outputS3Folder  = testDefaults.defaultOutput,
      readsLength     = readLength,
      splitChunkSize  = 1000,
      blastCommand    = blastn,
      blastOutRec     = defaults.blastnOutputRecord,
      blastOptions    = blastnOptions,
      referenceDBs    = testDefaults.referenceDBs
    )

    lazy val blastWorkers: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, c3.large),
      purchaseModel = Spot(maxPrice = Some(0.025)),
      groupSize = AutoScalingGroupSize(0, 100, 100)
    )

    lazy val assignWorkers: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.xlarge),
      purchaseModel = Spot(maxPrice = Some(0.05)),
      groupSize = AutoScalingGroupSize(0, 6, 6)
    )

    lazy val mergeWorkers: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.xlarge),
      purchaseModel = Spot(maxPrice = Some(0.05)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )
  }


  lazy val defaultAMI = AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  trait AnyTestLoquatConfig extends AnyLoquatConfig { config =>

    lazy val metadata: AnyArtifactMetadata = generated.metadata.mg7

    val iamRoleName = "loquat.testing"
    val logsBucketName = "loquat.testing"

    lazy val  managerConfig = ManagerConfig(
      InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1))
    )

    lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, m3.medium),
      purchaseModel = Spot(maxPrice = Some(0.1)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )

    lazy val terminationConfig = TerminationConfig(
      terminateAfterInitialDataMappings = true
    )

    val dataMappings: List[AnyDataMapping]
  }

  abstract class TestLoquatConfig(
    val loquatName: String,
    val dataMappings: List[AnyDataMapping]
  )
  extends AnyTestLoquatConfig

  lazy val loquatUser = LoquatUser(
    email = "aalekhin@ohnosequences.com",
    localCredentials = new ProfileCredentialsProvider("default"),
    keypairName = "aalekhin"
  )
}
