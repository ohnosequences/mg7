package ohnosequences.mg7.bundles

import ohnosequences.statika._
import ohnosequences.loquat.utils._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


// TODO: the non-bundle part of the trait could be put in the blast-api lib
trait AnyReferenceDB extends AnyBundle {
  // Depending on the DB you use, you have to provide a Map of it's IDs to TaxID
  val idsMap: AnyReferenceIDsMap

  val name: String
  val s3Address: AnyS3Address

  val dbLocation: File
  val dbName: File
}


// Here's the default one:
case object rna16s extends Bundle() with AnyReferenceDB {
  val idsMap = filteredGIs

  val name = "era7.16S.reference.sequences.0.1.0"
  val s3Address = S3Object("resources.ohnosequences.com", s"16s/${name}.tgz")

  val archive: File  = file"${name}.tgz"
  val dbLocation: File = file"${name}"
  val dbName: File   = dbLocation / s"${name}.fasta"

  def instructions: AnyInstructions = {

    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      transferManager.download(s3Address, archive)
    } -&-
    cmd("tar")("-xvzf", archive.path.toString) -&-
    say(s"Reference database ${name} was dowloaded to ${dbLocation.path}")
  }
}

case object rnaCentral extends Bundle() with AnyReferenceDB {
  val idsMap = rnaCentralIDsMap

  val name = "rnacentral_active"

  val s3key = "rnacentral/4.0/blastdb"
  val s3Address = S3Folder("resources.ohnosequences.com", s3key)

  val dbLocation: File = s3key.toFile
  val dbName: File = dbLocation / s"${name}.fasta"

  def instructions: AnyInstructions = {

    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      transferManager.download(s3Address, file".")
    } -&-
    say(s"Reference database ${name} was dowloaded to ${dbLocation.path}")
  }
}


// case object rna16sTest {
//
//   import ohnosequences.statika.aws._
//   import ohnosequences.awstools.ec2._
//   import ohnosequences.awstools.regions.Region._
//
//   case object rna16sCompat extends Compatible(
//     amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
//     rna16s,
//     generated.metadata.mg7
//   )
//
// }
