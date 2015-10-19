package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica.configuration._
import ohnosequences.metagenomica.bundles

import ohnosequences.loquat._, dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._

import ohnosequences.blast._, api._, data._, outputFields._

import ohnosequences.cosas._, types._, typeSets._, properties._, records._
import ops.typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.fastarious._, fasta._, fastq._

import java.io.File
import java.nio.file._
import collection.JavaConversions._

import sys.process._


case object splitDataProcessing extends DataProcessingBundle()(
  input = readsFastq :^: DNil,
  output = readsChunks :^: DNil
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
      outputDir.mkdir

      lazy val chunks: Iterator[(Seq[String], Int)] = io.Source.fromFile( context.file(readsFastq).javaFile )
        .getLines
        .grouped(4 * chunkSize)
        .zipWithIndex

      chunks foreach { case (chunk, n) =>
        Files.write(
          (outputDir / s"chunk.${n}.fastq").toPath,
          asJavaIterable(chunk),
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE
        )
      }
    } -&-
    success(
      "much blast, very success!",
      readsChunks.inFile(outputDir) :~: ∅
    )

  }
}
