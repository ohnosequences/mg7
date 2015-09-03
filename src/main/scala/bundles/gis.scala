package ohnosequences.metagenomica.bundles

import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._, profile._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.transfer._

import java.io.File


sealed class GIsBundle(name: String) extends Bundle() {
  val bucket = "resources.ohnosequences.com"
  val key = s"16s/${name}"

  val destination: File = new File(name)
  val location: File = destination

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
    say(s"GIs database ${name} was dowloaded and unpacked to ${location.getCanonicalPath}")
  }
}

/* This is what will be used in the assignment loquat */
case object filteredGIs extends GIsBundle("gi_taxid_filtered.csv")


case object filtering {

  import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._
  import ohnosequences.awstools.regions.Region._

  case object originalGIs extends GIsBundle("gi_taxid_nucl.dmp")

  // this is applied only once:
  case object filterGIs extends Bundle(blast16s, originalGIs) {

    // def giTaxIdMap: Map[Int, Int] = {
    //   // just two numbers separated with spaces
    //   val pattern = """(\d+)\s+(\d+).*""".r
    //
    //   io.Source.fromFile(location).getLines.map {
    //     case pattern(gi, taxId) => gi.toInt -> taxId.toInt
    //     // throwing an error, what else can we do...
    //     case _ => throw new Error("Wrong GIs file, can't parse something")
    //   }.toMap
    // }

    def instructions: AnyInstructions = {
      println("Starting filtering")
      // TODO: filter and upload the file
      say("something")
    }
  }

  case object filterGIsCompat extends Compatible(
    amzn_ami_64bit(Ireland, Virtualization.HVM)(1),
    filterGIs,
    generated.metadata.Metagenomica
  )

}
