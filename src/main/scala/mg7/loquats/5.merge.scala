package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import better.files._

case class mergeDataProcessing() extends DataProcessingBundle()(
  input  = data.mergeInput,
  output = data.mergeOutput
) {
  def instructions: AnyInstructions = say("Merging, joining, amalgamating!")

  // TODO: use streams, file-writers, etc. stuff
  def mergeChunks(dir: File, out: File): Unit = {
    // only one level in depth:
    dir.list.foreach { chunkFile =>
      out.append( chunkFile.contentAsString )
      chunkFile.delete()
    }
  }

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val blastMerged  = (context / "blast.csv").createIfNotExists(createParents = true)
    val noHitsMerged = (context / "blast.no-hits").createIfNotExists(createParents = true)
    val lcaMerged    = (context / "lca.csv").createIfNotExists(createParents = true)
    val bbhMerged    = (context / "bbh.csv").createIfNotExists(createParents = true)

    // TODO: write header for Blast output
    LazyTry { mergeChunks( context.inputFile(data.blastChunksFolder), blastMerged)  } -&-
    LazyTry { mergeChunks( context.inputFile(data.blastNoHitsFolder), noHitsMerged) } -&-
    LazyTry { mergeChunks( context.inputFile(data.lcaChunksFolder),   lcaMerged)    } -&-
    LazyTry { mergeChunks( context.inputFile(data.bbhChunksFolder),   bbhMerged)    } -&-
    success(s"Everything is merged",
      data.blastResult(blastMerged.toJava)   ::
      data.blastNoHits(noHitsMerged.toJava)  ::
      data.lcaCSV(lcaMerged.toJava)          ::
      data.bbhCSV(bbhMerged.toJava)          ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
