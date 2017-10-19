package ohnosequences.mg7

import ohnosequences.statika._
import ohnosequences.loquat._, utils._, files._
import ohnosequences.awstools._, s3._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import java.io.File

// TODO: the non-bundle part of the trait could be put in the blast-api lib
trait AnyReferenceDB extends AnyBundle {

  val name: String
  val blastDBS3:  S3Folder
  val id2taxasS3: S3Object

  lazy val blastDB:     File = file(blastDBS3.key)
  lazy val blastDBName: File = blastDB / s"${name.stripSuffix(".fasta")}.fasta"
  lazy val id2taxas:    File = file(s"${name}.id2taxas.tsv")

  def instructions: AnyInstructions = {
    lazy val s3client = s3.defaultClient

    LazyTry {
      s3client.download(blastDBS3, file("."))
      s3client.download(id2taxasS3, id2taxas)
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
