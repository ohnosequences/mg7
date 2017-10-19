package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.loquat._, utils.files._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._, fastq._
import collection.JavaConversions._
import java.io.File

case class splitDataProcessing(parameters: AnyMG7Parameters) extends DataProcessingBundle()(
  input  = data.splitInput,
  output = data.splitOutput
) {
  def instructions: AnyInstructions = say("Splitting, cutting, separating")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val outputDir = context / "chunks"

    LazyTry {
      outputDir.createDirectory

      val lines: Iterator[String] = context.inputFile(data.mergedReads).lines

      val fastasIterator: Iterator[String] = parameters.splitInputFormat match {
        // if input is FastQ, we parse it, convert it to FASTA and get String version
        case FastQInput => lines.parseFastqPhred33DropErrors.map(_.toFASTA.asString)
        // if it's Fasta, we parse it and get String version
        case FastaInput => lines.buffered.parseFastaSkipCrap.map(_.asString)
      }

      // group it
      fastasIterator
        .grouped(parameters.splitChunkSize)
        .zipWithIndex
        .foreach { case (chunk, n) =>

          (outputDir / s"chunk.${n}.fasta")
            .overwrite(chunk.mkString("\n"))
        }
    } -&-
    success(
      "chunk-chunk-chunk!",
      data.fastaChunks(outputDir) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
