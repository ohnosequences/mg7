package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import better.files._
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

      val lines: Iterator[String] = context.inputFile(data.mergedReads).lines

      lazy val fastas: Iterator[FASTA] = params.splitInputFormat match {
        // if input is FastQ, each read is just 4 lines
        case FastQInput => lines.grouped(4).map {
          
        }
        // if it's Fasta, we parse it, group and discard unparsed pieces
        case FastaInput =>
          fasta.parseFastaFromLines(lines)
            .flatMap { _.right.toOption }
      }

      fastas
        .grouped(params.splitChunkSize)
        .zipWithIndex
        .foreach { case (chunk, n) =>
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
