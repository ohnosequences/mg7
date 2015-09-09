package ohnosequences.metagenomica.bundles

import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._, profile._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.transfer._

import java.io.File


case object blast16s extends Bundle() {
  // val region = "eu-west-1"
  val bucket = "resources.ohnosequences.com"
  val name = "era7.16S.reference.sequences.0.1.0"
  val key = s"16s/${name}.tgz"

  val destination: File = new File(s"${name}.tgz")

  val location: File = new File(name)

  def instructions: AnyInstructions = {

    LazyTry {
      println(s"""Dowloading
        |from: s3://${bucket}/${key}
        |to: ${destination.getCanonicalPath}
        |""".stripMargin)

      // val transferManager = new TransferManager(new ProfileCredentialsProvider("default"))
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      val transfer = transferManager.download(bucket, key, destination)
      transfer.waitForCompletion
    } -&-
    cmd("tar")("-xvzf", destination.getCanonicalPath) -&-
    say(s"Reference database ${name} was dowloaded to ${location.getCanonicalPath}")

  }
}


case object blast16sTest {

  import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._
  import ohnosequences.awstools.regions.Region._

  case object blast16sCompat extends Compatible(
    amzn_ami_64bit(Ireland, Virtualization.HVM)(1),
    blast16s,
    generated.metadata.Metagenomica
  )

}
