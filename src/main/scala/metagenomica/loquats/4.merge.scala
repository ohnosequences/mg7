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

    val blastMerged = (context / "blast.csv").createIfNotExists()
    val noHitsMerged = (context / "blast.no-hits").createIfNotExists()

    LazyTry {
      // only one level in depth:
      context.inputFile(data.blastChunksFolder).list foreach { chunkFile =>

        blastMerged.append( chunkFile.contentAsString )
      }
    } -&-
    LazyTry {
      // only one level in depth:
      context.inputFile(data.blastNoHitsFolder).list foreach { noHitsChunk =>

        noHitsMerged.append( noHitsChunk.contentAsString )
      }
    } -&-
    success(s"Everything is merged",
      data.blastResult(blastMerged) ::
      data.blastNoHits(noHitsMerged) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
