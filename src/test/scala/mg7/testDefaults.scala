/*
  # Default MG7 test configuration and parameters
*/
package ohnosequences.test.mg7

import ohnosequences.mg7._, loquats._
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

  lazy val mg7 = ohnosequences.generated.metadata.mg7

  /* Output test data *is* scoped by version */
  lazy val commonS3Prefix = S3Folder("resources.ohnosequences.com", mg7.organization) /
    mg7.artifact /
    mg7.version /
    "test" /

  def outputS3FolderFor(pipeline: String): (SampleID, StepName) => S3Folder = { (sampleID, step) =>
    commonS3Prefix / pipeline / sampleID / step /
  }

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
      ).value

    case object parameters extends MG7Parameters(
      splitChunkSize  = 1000,
      blastCommand    = blastn,
      blastOutRec     = defaults.blastnOutputRecord,
      blastOptions    = blastnOptions,
      referenceDBs    = Set(rna16sRefDB)
    )
  }

  case object PacBio {

    lazy val blastnOptions =
      defaults.blastnOptions.update(
        reward(1)                   ::
        penalty(-2)                 ::
        word_size(72)               ::
        perc_identity(98.5)         ::
        max_target_seqs(10000)      ::
        evalue(BigDecimal(1e-100))  ::
        *[AnyDenotation]
      ).value

    case object parameters extends MG7Parameters(
      splitInputFormat  = FastQInput,
      splitChunkSize    = 100,
      blastCommand      = blastn,
      blastOptions      = blastnOptions,
      blastOutRec       = defaults.blastnOutputRecord,
      referenceDBs      = Set(rna16sRefDB)
    )
  }


  trait MG7PipelineDefaults extends AnyMG7Pipeline {

    val metadata = ohnosequences.generated.metadata.mg7
    val iamRoleName = "loquat.testing"
    val logsBucketName = "loquat.testing"

    override val splitConfig  = SplitConfig(1)
    override val blastConfig  = BlastConfig(100)
    override val assignConfig = AssignConfig(6)
    override val mergeConfig  = MergeConfig(1)
    override val countConfig  = CountConfig(1)
  }

  lazy val loquatUser = LoquatUser(
    email = "aalekhin@ohnosequences.com",
    localCredentials = new ProfileCredentialsProvider("default"),
    keypairName = "aalekhin"
  )
}
