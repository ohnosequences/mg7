package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._

import better.files._
import collection.JavaConversions._


case class splitDataProcessing(params: AnyMG7Parameters) extends DataProcessingBundle()(
  input = data.splitInput,
  output = data.splitOutput
) {

  def instructions: AnyInstructions = say("Splitting, cutting, separating")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val outputDir = context / "chunks"
    val readsCount = outputDir / "reads-count"

    LazyTry {
      outputDir.createDirectories()

      val lines: Iterator[String] = context.inputFile(data.mergedReads).lines

      val fastasIterator: Iterator[String] = params.splitInputFormat match {
        // if input is FastQ, we parse it, convert it to FASTA and get String version
        case FastQInput => fastq.parseFastqDropErrors(lines).map(_.toFASTA.asString)
        // if it's Fasta, we parse it and get String version
        case FastaInput => fasta.parseFastaDropErrors(lines).map(_.asString)
      }

      val (fastas1, fastas2) = fastasIterator.duplicate

      // group it
      fastas1
        .grouped(params.splitChunkSize)
        .zipWithIndex
        .foreach { case (chunk, n) =>

          (outputDir / s"chunk.${n}.fasta")
            .overwrite(chunk.mkString("\n"))
        }

      readsCount.overwrite(fastas2.length.toString)
    } -&-
    success("chunk-chunk-chunk!",
      data.fastaChunks(outputDir) ::
      data.totalReadsNumber(readsCount) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
