package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.blast.api._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import ohnosequences.fastarious._
import ohnosequences.fastarious.fasta._
import ohnosequences.fastarious.fastq._

import better.files._

import sys.process._


case class blastDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle(
  bundles.blast,
  bundles.blast16s
)(
  input = data.blastInput,
  output = data.blastOutput
) {

  def instructions: AnyInstructions = say("Let the blasting begin!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val totalOutput = context / "blastAll.csv"

    LazyTry {
      lazy val quartets = io.Source.fromFile( context.inputFile(data.readsChunk).toJava ).getLines.grouped(4)

      quartets foreach { quartet =>
        println(quartet.mkString("\n"))

        // we only care about the id and the seq here
        val read = FASTA(
            header(FastqId(quartet(0)).toFastaHeader) ::
            fasta.sequence(FastaLines(quartet(1)))    ::
            *[AnyDenotation]
          )

        val readFile = context / "read.fa"
        readFile
          .createIfNotExists()
          .appendLines(read.toLines: _*)

        val outFile = context / "blastRead.csv"

        val expr = blastn(
          outputRecord = md.blastOutRec,
          argumentValues = blastn.arguments(
            db(bundles.blast16s.dbName) ::
            query(readFile) ::
            out(outFile) ::
            *[AnyDenotation]
          ).value,
          optionValues   = blastn.defaults.update(
            num_threads(1) ::
            word_size(42) ::
            max_target_seqs(10) ::
            evalue(0.001) ::
            blastn.task(blastn.megablast) ::
            *[AnyDenotation]
          ).value
        )
        println(expr.toSeq.mkString(" "))

        // BAM!!!
        val exitCode = expr.toSeq.!
        println(s"BLAST EXIT CODE: ${exitCode}")

        // we should have something in args getV out now. Append it!
        println(s"Appending [${outFile.path}] to [${totalOutput.path}]")
        totalOutput
          .createIfNotExists()
          .append(outFile.contentAsString)

        // clean up
        readFile.delete(true)
        outFile.delete(true)
      }
    } -&-
    success(
      "much blast, very success!",
      data.blastChunkOut(totalOutput) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
