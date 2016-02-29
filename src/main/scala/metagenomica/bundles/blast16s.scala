package ohnosequences.mg7.bundles

import ohnosequences.statika._
import ohnosequences.loquat.utils._
import ohnosequences.awstools.s3.S3Object

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


// TODO: the non-bundle part of the trait could be put in the blast-api lib
trait AnyBlastReferenceDB extends AnyBundle {
  val name: String

  val s3Address: S3Object

  val destination: File = File(s"${name}.tgz")
  val location: File = File(name)

  val dbName: File = File(s"${name}/${name}.fasta")

  def instructions: AnyInstructions = {

    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      transferManager.download(s3Address, destination)
    } -&-
    cmd("tar")("-xvzf", destination.path.toString) -&-
    say(s"Reference database ${name} was dowloaded to ${location.path}")

  }
}

abstract class BlastReferenceDB(
  val name: String,
  val s3Address: S3Object
) extends Bundle() with AnyBlastReferenceDB


// Here's the default one:
case object blast16s extends Bundle() with AnyBlastReferenceDB {

  val name = "era7.16S.reference.sequences.0.1.0"
  val s3Address = S3Object("resources.ohnosequences.com", s"16s/${name}.tgz")
}


case object blast16sTest {

  import ohnosequences.statika.aws._
  import ohnosequences.awstools.ec2._
  import ohnosequences.awstools.regions.Region._

  case object blast16sCompat extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
    blast16s,
    generated.metadata.mg7
  )

}
