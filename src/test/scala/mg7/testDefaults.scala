/*
  # Default MG7 test configuration and parameters
*/
package ohnosequences.test.mg7

import ohnosequences.mg7._
import ohnosequences.loquat._
import ohnosequences.awstools._, s3._
import com.amazonaws.auth._, profile._

case object testDefaults {

  lazy val mg7 = ohnosequences.generated.metadata.mg7

  /* Output test data *is* scoped by version */
  lazy val outputS3Prefix = S3Folder("resources.ohnosequences.com", mg7.organization) / mg7.artifact / mg7.version / "test" /

  def outputS3FolderFor(pipeline: String): (SampleID, StepName) => S3Folder = { (sampleID, step) =>
    outputS3Prefix / pipeline / sampleID / step /
  }


  trait MG7PipelineDefaults extends AnyMG7Pipeline {

    val metadata = ohnosequences.generated.metadata.mg7
    val iamRoleName = "loquat.testing"
    val logsS3Prefix = s3"loquat.testing" / "mg7" / name /
    val outputS3Folder = testDefaults.outputS3FolderFor(name)

    val splitConfig  = SplitConfig(1)
    val blastConfig  = BlastConfig(100)
    val assignConfig = AssignConfig(6)
    val mergeConfig  = MergeConfig(1)
    val countConfig  = CountConfig(1)
  }

  lazy val loquatUser = LoquatUser(
    email = "aalekhin@ohnosequences.com",
    localCredentials = new ProfileCredentialsProvider("default"),
    keypairName = "aalekhin"
  )
}
