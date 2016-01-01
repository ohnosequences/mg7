package ohnosequences.metagenomica.bundles

import ohnosequences.statika._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


case object blast16s extends Bundle() {
  // val region = "eu-west-1"
  val bucket = "resources.ohnosequences.com"
  val name = "era7.16S.reference.sequences.0.1.0"
  val key = s"16s/${name}.tgz"

  val destination: File = File(s"${name}.tgz")
  val location: File = File(name)
  val dbName: File = File(s"${name}/${name}.fasta")

  def instructions: AnyInstructions = {

    LazyTry {
      println(s"""Dowloading
        |from: s3://${bucket}/${key}
        |to: ${destination.path}
        |""".stripMargin)

      // val transferManager = new TransferManager(new ProfileCredentialsProvider("default"))
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      val transfer = transferManager.download(bucket, key, destination.toJava)
      transfer.waitForCompletion
    } -&-
    cmd("tar")("-xvzf", destination.path.toString) -&-
    say(s"Reference database ${name} was dowloaded to ${location.path}")

  }
}


case object blast16sTest {

  import ohnosequences.statika.aws._
  import ohnosequences.awstools.ec2._
  import ohnosequences.awstools.regions.Region._

  case object blast16sCompat extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
    blast16s,
    generated.metadata.Metagenomica
  )

}
