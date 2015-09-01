package ohnosequences.metagenomica.bundles

import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._, profile._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.transfer._

import java.io.File


case object gis extends Bundle() {
  val bucket = "resources.ohnosequences.com"
  // FIXME: this is wrong file:
  val name = "era7.16S.reference.sequences.blastdb"
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
    cmd("tar")("xvf", destination.getCanonicalPath) -&-
    say(s"GIs database ${name} was dowloaded and unpacked to ${location.getCanonicalPath}")
  }

  def giTaxIdMap: Map[Int, Int] = {
    // just two numbers separated with spaces
    val pattern = """(\d+)\s+(\d+).*""".r

    io.Source.fromFile(location).getLines.map {
      case pattern(gi, taxId) => gi.toInt -> taxId.toInt
      // throwing an error, what else can we do...
      case _ => throw new Error("Wrong GIs file, can't parse something")
    }.toMap
  }
}


case object gisTest {

  import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._
  import ohnosequences.awstools.regions.Region._

  // just head of that dmp
  val testMap: Map[Int, Int] = Map(
    2  -> 9913,
    3  -> 9913,
    4  -> 9646,
    5  -> 9913,
    7  -> 9913,
    9  -> 9913,
    11 -> 9913,
    13 -> 9913,
    15 -> 9915,
    16 -> 9771
  )

  case object gisMapTest extends Bundle(gis) {

    def instructions: AnyInstructions = {
      lazy val bigMap = gis.giTaxIdMap
      testMap.forall { case (k, v) =>
        bigMap(k) == v
      } match {
        case true  => say("everything's cool!")
        case false => failure("something's wrong!")
      }
    }
  }

  case object gisCompat extends Compatible(
    amzn_ami_64bit(Ireland, Virtualization.HVM)(1),
    gisMapTest,
    generated.metadata.Metagenomica
  )

}
