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

case object testDefaults {

  /* All input test data should go in here */
  lazy val inputS3Folder  = S3Folder("resources.ohnosequences.com", "16s/public-datasets")
  /* Output test data is scoped by version */
  lazy val outputS3Folder = S3Folder("resources.ohnosequences.com", generated.metadata.mg7.artifact) / generated.metadata.mg7.version

  val defaultOutput: (SampleID, StepName) => S3Folder =
    (sampleID, step) => outputS3Folder/sampleID/step/

  lazy val referenceDBs: Set[AnyReferenceDB] = Set(rna16sRefDB)
  /*
    ## Default Illumina parameters

  */
  case object Illumina {

    lazy val blastnOptions =
      defaults.blastnOptions.update(
        num_threads(4)              ::
        word_size(46)               ::
        evalue(BigDecimal(1E-100))  ::
        /* We're going to use all hits to do global sample-coherent assignment. But not now, so no reason for this to be huge */
        max_target_seqs(150)        ::
        /* 95% is a reasonable minimum. If it does not work, be more stringent with read preprocessing */
        perc_identity(95.0)         ::
        *[AnyDenotation]
      )
      .value
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
