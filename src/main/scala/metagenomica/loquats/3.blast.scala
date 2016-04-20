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

        val expr = md.blastExpr(inFile, outFile)

        println(expr.toSeq.mkString(" "))

        // BAM!!
        expr.toSeq.!!

        val csvReader = csv.Reader(md.blastOutRec.keys, outFile)

        val allHits: Seq[csv.Row[md.BlastOutRecKeys]] = csvReader.rows.toSeq

        println(s"- There are ${allHits.length} hits")

        // TODO: at the moment this filter is fixed, but it should be configurable
        val prefilteredHits: Seq[csv.Row[md.BlastOutRecKeys]] = allHits.filter(md.blastFilter)

        /* Here we pick the first pident value, which will be the maximum one, if present. Afterwards, we keep only those hits with the same pident. It is important to apply this filter *after* the one based on query coverage. */
        import md._
        val maxPident: Option[String] =
          prefilteredHits.headOption map { r => r select outputFields.pident }

        val pidentFilter: csv.Row[md.BlastOutRecKeys] => Boolean =
          row => maxPident.fold(false)(m => (row select outputFields.pident) == m)

        val filteredHits: Seq[csv.Row[md.BlastOutRecKeys]] = prefilteredHits filter pidentFilter

        println(s"- After filtering there are ${filteredHits.length} hits")

        if (filteredHits.isEmpty) {
          println(s"- Recording read ${read.getV(header).id} in no-hits")
          noHits.appendLine(read.asString)
        } else {
          println(s"- Appending filtered results to the total chunk output")
          totalOutputWriter.writeAll(filteredHits.map{ _.values })
        }

        csvReader.close()
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
