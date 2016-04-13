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


case object blastBundle extends ohnosequencesBundles.statika.Blast("2.2.31")

case class blastDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle(
  blastBundle,
  md.referenceDB
)(input  = data.blastInput,
  output = data.blastOutput
) {

  def instructions: AnyInstructions = say("Let the blasting begin!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val totalOutput = (context / "blastAll.csv").createIfNotExists()
    val noHits = (context / "no.hits").createIfNotExists()

    LazyTry {
      // NOTE: once we update to better-files 2.15.+, use `file.lineIterator` here (it's autoclosing):
      val source = io.Source.fromFile( context.inputFile(data.fastaChunk).toJava )
      val totalOutputWriter = csv.newWriter(totalOutput, append = true)

      fasta.parseFastaDropErrors(source.getLines) foreach { read =>
        println(s"\nRunning BLAST for the read ${read.getV(header).id}")

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

        val csvReader = csv.Reader(defaultBlastOutRec.keys, outFile)

        val seq = csvReader.rows.toSeq

        println(s"- There are ${seq.length} hits")

        // TODO: at the moment this filter is fixed, but it should be configurable
        val filteredRows: Seq[Seq[String]] = seq.filter { row =>

          val qcovs: String = row.select(outputFields.qcovs)
          parseDouble(qcovs).map(_ > 98.0).getOrElse(false)

        }.map{ _.values }

        println(s"- After filtering only ${filteredRows.length} hits left")

        if (filteredRows.isEmpty) {
          println(s"- Recording read ${read.getV(header).id} in no-hits")
          noHits.appendLine(read.asString)
        } else {
          println(s"- Appending filtered results to the total chunk output")
          totalOutputWriter.writeAll(filteredRows)
        }

        csvReader.close()
      }

      // it's important to close things in the end:
      source.close()
      totalOutputWriter.close()
    } -&-
    success(
      "much blast, very success!",
      data.blastChunkOut(totalOutput) ::
      data.noHitsChunk(noHits) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
