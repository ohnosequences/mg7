package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.loquat._, utils.files._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import java.nio.file.{ Files, StandardOpenOption }
import java.io.File

case class mergeDataProcessing() extends DataProcessingBundle()(
  input  = data.mergeInput,
  output = data.mergeOutput
) {
  def instructions: AnyInstructions = say("Merging, joining, amalgamating!")

  // TODO: use streams, file-writers, etc. stuff
  def mergeChunks(dir: File, out: File): Unit = {
    // only one level in depth:
    dir.listFiles.foreach { chunkFile =>
      Files.write(
        out.path,
        Files.readAllBytes(chunkFile.path),
        StandardOpenOption.APPEND
      )
      chunkFile.delete()
    }
  }

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val blastMerged  = (context / "blast.csv").createFile
    val noHitsMerged = (context / "blast.no-hits").createFile
    val lcaMerged    = (context / "lca.csv").createFile
    val bbhMerged    = (context / "bbh.csv").createFile

    // TODO: write header for Blast output
    LazyTry { mergeChunks( context.inputFile(data.blastChunksFolder), blastMerged)  } -&-
    LazyTry { mergeChunks( context.inputFile(data.blastNoHitsFolder), noHitsMerged) } -&-
    LazyTry { mergeChunks( context.inputFile(data.lcaChunksFolder),   lcaMerged)    } -&-
    LazyTry { mergeChunks( context.inputFile(data.bbhChunksFolder),   bbhMerged)    } -&-
    success(s"Everything is merged",
      data.blastResult(blastMerged)   ::
      data.blastNoHits(noHitsMerged)  ::
      data.lcaCSV(lcaMerged)          ::
      data.bbhCSV(bbhMerged)          ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
