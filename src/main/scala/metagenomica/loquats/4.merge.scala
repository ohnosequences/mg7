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


case object mergeDataProcessing extends DataProcessingBundle()(
  input = blastChunks :^: DNil,
  output = blastResult :^: DNil
) {

  def instructions: AnyInstructions = say("Merging, joining, amalgamating!")

  def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val outputFile = context / "whole.thing"

    LazyTry {
      // only one level in depth:
      context.file(blastChunks).listFiles foreach { chunk =>

        Files.write(
          outputFile.toPath,
          Files.readAllLines(chunk.toPath),
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
          StandardOpenOption.APPEND
        )

      }
    } -&-
    success(
      s"Everything is merged in [${outputFile.path}]",
      blastResult.inFile(outputFile) :~: ∅
    )

  }
}
