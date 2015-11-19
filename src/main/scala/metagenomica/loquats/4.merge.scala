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


case object mergeDataProcessing extends DataProcessingBundle()(
  input = data.blastChunks :^: DNil,
  output = data.blastResult :^: DNil
) {

  def instructions: AnyInstructions = say("Merging, joining, amalgamating!")

  def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val outputFile = context / "whole.thing"

    LazyTry {
      // only one level in depth:
      context.file(data.blastChunks).list foreach { chunk =>

        Files.write(
          outputFile.path,
          Files.readAllLines(chunk.path),
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
          StandardOpenOption.APPEND
        )

      }
    } -&-
    success(
      s"Everything is merged in [${outputFile.path}]",
      data.blastResult.inFile(outputFile) :~: ∅
    )

  }
}
