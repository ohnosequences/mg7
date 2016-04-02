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
    val noHits = context / "no.hits"

    LazyTry {
      // NOTE: once we update to better-files 2.15.+, use `file.lineIterator` here (it's autoclosing):
      val source = io.Source.fromFile( context.inputFile(data.fastaChunk).toJava )

      fasta.parseFastaDropErrors(source.getLines) foreach { read =>

        val inFile = (context / "read.fa").overwrite(read.asString)
        val outFile = (context / "blastRead.csv").clear()

        val expr = BlastExpression(md.blastCommand)(
          outputRecord = md.blastOutRec,
          argumentValues =
            db(Set(md.referenceDB.dbName)) ::
            query(inFile) ::
            out(outFile) ::
            *[AnyDenotation],
          optionValues = md.blastOptions
        )(md.argValsToSeq, md.optValsToSeq)

        println(expr.toSeq.mkString(" "))

        // BAM!!
        expr.toSeq.!!

        val output = outFile.contentAsString
        // if not BLAST hits, recording read's header
        if (output.isEmpty) noHits.appendLine(read.getV(header).toString)
        // append results for this read to the total output
        else totalOutput.append(output)
      }

      // it's important to close the stream:
      source.close()
    } -&-
    success(
      "much blast, very success!",
      data.blastChunkOut(totalOutput) ::
      data.noHitsHeaders(noHits) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
