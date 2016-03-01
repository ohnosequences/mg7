package ohnosequences.mg7.loquats

import ohnosequences.mg7._

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
  md.referenceDB
)(
  input = data.blastInput,
  output = data.blastOutput
) {

  def instructions: AnyInstructions = say("Let the blasting begin!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val totalOutput = context / "blastAll.csv"

    LazyTry {
      // NOTE: once we update to better-files 2.15.+, use `file.lineIterator` here (it's autoclosing):
      lazy val source = io.Source.fromFile( context.inputFile(data.readsChunk).toJava )

      source.getLines.grouped(4) foreach { quartet =>
        // println(quartet.mkString("\n"))

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
          argumentValues =
            db(md.referenceDB.dbName) ::
            query(readFile) ::
            out(outFile) ::
            *[AnyDenotation],
          optionValues = md.blastOptions.value
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

      // it's important to close the stream:
      source.close()
    } -&-
    success(
      "much blast, very success!",
      data.blastChunkOut(totalOutput) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
