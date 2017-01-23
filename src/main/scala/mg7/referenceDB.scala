package ohnosequences.mg7

import ohnosequences.statika._
import ohnosequences.loquat.utils._
import ohnosequences.awstools._, s3._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._

// TODO: the non-bundle part of the trait could be put in the blast-api lib
trait AnyReferenceDB extends AnyBundle {

  val name: String
  val blastDBS3:  S3Folder
  val id2taxasS3: S3Object

  lazy val blastDB:     File = blastDBS3.key.toFile
  lazy val blastDBName: File = blastDB / s"${name.stripSuffix(".fasta")}.fasta"
  lazy val id2taxas:    File = file"${name}.id2taxas.tsv"

  def instructions: AnyInstructions = {

    LazyTry {
      val transferManager = new TransferManager(new DefaultAWSCredentialsProviderChain())

      transferManager.download(blastDBS3, file".".toJava)
      transferManager.download(id2taxasS3, id2taxas.toJava)
    } -&-
    say(s"Reference database ${name} was dowloaded to ${blastDB.path}")
  }
}


abstract class ReferenceDB(
  val name: String,
  val blastDBS3:  S3Folder,
  val id2taxasS3: S3Object
)
extends Bundle() with AnyReferenceDB
