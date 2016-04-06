package ohnosequences.mg7.bundles

import ohnosequences.mg7._

import ohnosequences.statika._
import ohnosequences.loquat.utils._
import ohnosequences.awstools.s3.S3Object

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._
import com.github.tototoshi.csv._


trait AnyReferenceIDsMap extends AnyBundle {
  val name: String
  val s3Address: S3Object

  lazy val destination: File = File(name)

  def instructions: AnyInstructions = {

    LazyTry {
      // val transferManager = new TransferManager(new ProfileCredentialsProvider("default"))
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      transferManager.download(s3Address, destination)
    } -&-
    say(s"Reference IDs mapping ${name} was dowloaded and unpacked to ${destination.path}")
  }

  def mapping: Map[ID, TaxID] = {
    // Reading TSV file with mapping something-taxId
    // FIXME: once we generate the new DB, use here UnixCSVFormat
    val tsvReader: CSVReader = CSVReader.open( destination.toJava )(new TSVFormat {})

    val idsMap: Map[ID, TaxID] = tsvReader.iterator.map { row =>
      row(0) -> row(1)
    }.toMap

    tsvReader.close

    idsMap
  }
}



/* This is what will be used in the assignment loquat */
case object filteredGIs extends Bundle() with AnyReferenceIDsMap {
  val name = "gi_taxid_filtered.csv"
  val s3Address = S3Object("resources.ohnosequences.com", s"16s/${name}")
}

case object rnaCentralIDsMap extends Bundle() with AnyReferenceIDsMap {
  val name = "id2taxa.filtered.4.0.tsv"
  val s3Address = S3Object("resources.ohnosequences.com", s"rnacentral/4.0/${name}")
}
