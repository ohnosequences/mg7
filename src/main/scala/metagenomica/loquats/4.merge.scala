package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import better.files._


case object mergeDataProcessing extends DataProcessingBundle()(
  input = data.mergeInput,
  output = data.mergeOutput
) {

  def instructions: AnyInstructions = say("Merging, joining, amalgamating!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val outputFile = context / "whole.thing"

    LazyTry {
      outputFile.createIfNotExists()

      // only one level in depth:
      context.inputFile(data.blastChunks).list foreach { chunkFile =>

        outputFile.append( chunkFile.contentAsString )
      }
    } -&-
    success(
      s"Everything is merged in [${outputFile.path}]",
      data.blastResult(outputFile) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
