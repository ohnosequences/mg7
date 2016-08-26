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
  (bundles.blast +: md.referenceDBs.toSeq): _*
)(
  input  = data.blastInput,
  output = data.blastOutput
) {

  def instructions: AnyInstructions = say("Let the blasting begin!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val totalOutput = (context / "blastAll.csv").createIfNotExists()
    val noHits = (context / "no.hits").createIfNotExists()

    LazyTry {
      // NOTE: once we update to better-files 2.15.+, use `file.lineIterator` here (it's autoclosing):
      val source = io.Source.fromFile( context.inputFile(data.fastaChunk).toJava )
      val totalOutputWriter = csv.Writer(md.blastOutRec.keys)(totalOutput)

      fasta.parseFastaDropErrors(source.getLines) foreach { read =>
        println(s"\nRunning BLAST for the read ${read.getV(header).id}")

        val inFile = (context / "read.fa").overwrite(read.asString)
        val outFile = (context / "blastRead.csv").clear()

        val expr = md.blastExpr(inFile, outFile)
        println(expr.toSeq.mkString(" "))
        expr.toSeq.!!

        val blastReader = csv.Reader(md.blastOutRec.keys)(outFile)
        val allHits: Seq[csv.Row[md.BlastOutRecKeys]] = blastReader.rows.toSeq

        println(s"- There are ${allHits.length} hits")

        val prefilteredHits: Seq[csv.Row[md.BlastOutRecKeys]] = allHits.filter(md.blastFilter)

        /* We keep only those hits with the maximum pident. It is important to apply this filter *after* the one based on query coverage. */

        val filteredHits: Seq[csv.Row[md.BlastOutRecKeys]] =
          if (prefilteredHits.isEmpty) Seq()
          else {
            import md.has_pident

            val maxPident: Double = prefilteredHits.flatMap { row =>
              parseDouble( row.select(outputFields.pident) )
            }.max

            prefilteredHits.filter { row =>
              parseDouble( row.select(outputFields.pident) ).map { p =>
                (maxPident - p) <= md.pidentMaxVariation
              }.getOrElse(false)
            }
          }

        println(s"- After filtering there are ${filteredHits.length} hits")

        if (filteredHits.isEmpty) {
          println(s"- Recording read ${read.getV(header).id} in no-hits")
          noHits.appendLine(read.asString)
        } else {
          println(s"- Appending filtered results to the total chunk output")
          filteredHits.foreach { row => totalOutputWriter.addRow(row) }
        }

        blastReader.close()
      }

      // it's important to close things in the end:
      source.close()
      totalOutputWriter.close()
    } -&-
    success(
      "much blast, very success!",
      data.blastChunk(totalOutput) ::
      data.noHitsChunk(noHits) ::
      *[AnyDenotation { type Value <: FileResource }]
    )

  }
}
