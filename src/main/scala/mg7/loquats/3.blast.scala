package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.loquat._, utils.files._
import ohnosequences.statika._
import ohnosequences.blast.api._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._
import sys.process._

case class blastDataProcessing[P <: AnyMG7Parameters](val parameters: P)
extends DataProcessingBundle(
  deps = (bundles.blast +: parameters.referenceDBs.toSeq): _*
)(input  = data.blastInput,
  output = data.blastOutput
) {
  def instructions: AnyInstructions = say("Let the blasting begin!")

  type BlastRow = csv.Row[parameters.blastOutRec.Keys]

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val totalOutput = (context / "blastAll.csv").createFile
    val noHits = (context / "no.hits").createFile

    LazyTry {
      // NOTE: once we update to better-files 2.15.+, use `file.lineIterator` here (it's autoclosing):
      val source = io.Source.fromFile( context.inputFile(data.fastaChunk) )
      val totalOutputWriter = csv.Writer(parameters.blastOutRec.keys)(totalOutput)

      source.getLines.buffered.parseFastaSkipCrap.foreach { read =>
        println(s"\nRunning BLAST for the read ${read.header.id}")

        val inFile = (context / "read.fa").overwrite(read.asString)
        val outFile = (context / "blastRead.csv")
        if (outFile.exists) outFile.delete()

        val expr = parameters.blastExpr(inFile, outFile)
        println(expr.toSeq.mkString(" "))
        expr.toSeq.!!

        val blastReader = csv.Reader(parameters.blastOutRec.keys)(outFile)
        val allHits: Seq[BlastRow] = blastReader.rows.toSeq

        println(s"- There are ${allHits.length} hits")

        val prefilteredHits: Seq[BlastRow] = allHits.filter(parameters.blastFilter)

        /* We keep only those hits with the maximum pident. It is important to apply this filter *after* the one based on query coverage. */

        val filteredHits: Seq[BlastRow] =
          if (prefilteredHits.isEmpty) Seq()
          else {
            import parameters.has_pident

            val maxPident: Double = prefilteredHits.flatMap { row =>
              parseDouble( row.select(outputFields.pident) )
            }.max

            prefilteredHits.filter { row =>
              parseDouble( row.select(outputFields.pident) ).map { p =>
                (maxPident - p) <= parameters.pidentMaxVariation
              }.getOrElse(false)
            }
          }

        println(s"- After filtering there are ${filteredHits.length} hits")

        if (filteredHits.isEmpty) {
          println(s"- Recording read ${read.header.id} in no-hits")
          // TODO: check that it append lines correctly
          noHits.append(read.asString)
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
