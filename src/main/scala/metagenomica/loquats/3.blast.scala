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
)(input  = data.blastInput,
  output = data.blastOutput
) {
  private val header: Seq[AnyOutputField] = md.blastOutRec.keys.types.asList

  def filterResult(blastResult: File): Iterator[Seq[String]] = {
    val csvReader = newCSVReader(blastResult)

    val filtered = csvReader.iterator.filter { values =>

      val qcovs = csv.Row(header, values).select(outputFields.qcovs)

      parseDouble(qcovs).map(_ > 99.5).getOrElse(false)
      // TODO: any other conditions? it's easy to add this to the config
    }

    csvReader.close()

    filtered
  }

  def instructions: AnyInstructions = say("Let the blasting begin!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val totalOutput = (context / "blastAll.csv").createIfNotExists()
    val noHits = (context / "no.hits").createIfNotExists()

    LazyTry {
      // NOTE: once we update to better-files 2.15.+, use `file.lineIterator` here (it's autoclosing):
      val source = io.Source.fromFile( context.inputFile(data.fastaChunk).toJava )
      val totalOutputWriter = newCSVWriter(totalOutput, append = true)

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

        val filteredRows: Iterator[Seq[String]] = filterResult(outFile)

        // if not BLAST hits, recording the read
        if (filteredRows.isEmpty) noHits.appendLine(read.asString)
        // append results for this read to the total output
        else filteredRows.foreach { totalOutputWriter.writeRow(_) }
      }

      // it's important to close the stream:
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
