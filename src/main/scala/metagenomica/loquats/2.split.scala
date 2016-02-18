package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import better.files._
import java.nio.file._
import collection.JavaConversions._


case class splitDataProcessing(params: AnyMG7Parameters) extends DataProcessingBundle()(
  input = data.splitInput,
  output = data.splitOutput
) {

  def instructions: AnyInstructions = say("Splitting, cutting, separating")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val outputDir = context / "chunks"

    LazyTry {
      outputDir.createDirectories()

      lazy val chunks: Iterator[(Seq[String], Int)] =
        context.inputFile(data.mergedReads)
          .lines
          .grouped(4 * params.chunkSize)
          .zipWithIndex

      chunks foreach { case (chunk, n) =>
        (outputDir / s"chunk.${n}.fastq")
          .createIfNotExists()
          .overwrite(chunk.mkString("\n"))
      }
    } -&-
    success("chunk-chunk-chunk!",
      data.readsChunks(outputDir) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
