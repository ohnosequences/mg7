package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.loquat._

import ohnosequences.statika._

import ohnosequences.blast.api._

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import ohnosequences.fastarious._, fasta._

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
      val source = io.Source.fromFile( context.inputFile(data.fastaChunk).toJava )

      fasta.parseMapFromLines(source.getLines) foreach { fastaMap =>

        val read = FASTA(
          header(FastaHeader(fastaMap(fasta.header.label))) ::
          sequence(FastaSequence(fastaMap(fasta.sequence.label))) ::
          *[AnyDenotation]
        )

        val inFile = (context / "read.fa").overwrite(read.toLines.stripSuffix("\n"))
        val outFile = (context / "blastRead.csv").clear()

        val expr = blastn(
          outputRecord = md.blastOutRec,
          argumentValues =
            db(md.referenceDB.dbName) ::
            query(inFile) ::
            out(outFile) ::
            *[AnyDenotation],
          optionValues = md.blastOptions.value
        )
        println(expr.toSeq.mkString(" "))

        // BAM!!
        expr.toSeq.!!

        // append results for this read to the total output
        totalOutput.append(outFile.contentAsString)
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
