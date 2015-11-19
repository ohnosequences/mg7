package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica._

import ohnosequences.loquat._

import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._

import ohnosequences.{ blast => b }, b.api._, b.data._, outputFields._

import ohnosequences.cosas._, types._, typeSets._, properties._, records._
import ops.typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.fastarious.fasta._
import ohnosequences.fastarious.fastq._

import better.files._
import java.nio.file._
import collection.JavaConversions._

import sys.process._


case object splitDataProcessing extends DataProcessingBundle()(
  input = data.mergedReads :^: DNil,
  output = data.readsChunks :^: DNil
) {

  def instructions: AnyInstructions = say("Splitting, cutting, separating")

  def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val outputDir = context / "chunks"

    // TODO: move it to the config
    val chunkSize = 1

    LazyTry {
      outputDir.createDirectories()

      lazy val chunks: Iterator[(Seq[String], Int)] =
        io.Source.fromFile( context.file(data.mergedReads).toJava )
          .getLines
          .grouped(4 * chunkSize)
          .zipWithIndex

      chunks foreach { case (chunk, n) =>
        Files.write(
          (outputDir / s"chunk.${n}.fastq").path,
          asJavaIterable(chunk),
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE
        )
      }
    } -&-
    success(
      "much blast, very success!",
      data.readsChunks.inFile(outputDir) :~: ∅
    )

  }
}
