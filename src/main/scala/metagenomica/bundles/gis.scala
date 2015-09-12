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
  case object filterGIs extends Bundle(originalGIs) {

    val bucket = "resources.ohnosequences.com"

    val referenceFileName = "reference.gis"
    lazy val referenceFile = new File(referenceFileName)

    val filteredFileName = "filtered.gis"
    lazy val filteredFile = new File(filteredFileName)

    def key(name: String) = s"16s/${name}"


    def instructions: AnyInstructions = {

      lazy val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

      // downloading reference GIs file
      LazyTry {
        println(s"""Dowloading
          |from: s3://${bucket}/${key(referenceFileName)}
          |to: ${referenceFile.getCanonicalPath}
          |""".stripMargin)

        transferManager.download(bucket, key(referenceFileName), referenceFile).waitForCompletion
      } -&-
      LazyTry {
        val refGIs: Set[String] = io.Source.fromFile( referenceFile ).getLines.toSet

        import com.github.tototoshi.csv._
        val csvReader = CSVReader.open(originalGIs.location)(new TSVFormat {})
        val csvWriter = CSVWriter.open(filteredFile, append = true)(new TSVFormat {})

        // iterating over huge GIs file and filtering it
        csvReader foreach { row =>
          if ( refGIs.contains(row(0)) ) {
            csvWriter.writeRow(row)
            // println("added: " + row.mkString("\t"))
          }
        }

        csvReader.close
        csvWriter.close
      } -&-
      LazyTry {
        println(s"""Uploading
          |from: ${filteredFile.getCanonicalPath}
          |to: s3://${bucket}/${key(filteredFileName)}
          |""".stripMargin)

        transferManager.upload(bucket, key(filteredFileName), filteredFile).waitForCompletion
      }
    }
  }

  case object filterGIsCompat extends Compatible(
    amzn_ami_64bit(Ireland, Virtualization.HVM)(1),
    filterGIs,
    generated.metadata.Metagenomica
  )

}
