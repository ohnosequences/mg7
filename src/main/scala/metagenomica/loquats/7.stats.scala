package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._

import ohnosequences.loquat._

import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.fastarious._, fasta._

import better.files._
import com.github.tototoshi.csv._

import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph


case object statsDataProcessing extends DataProcessingBundle()(
  input = data.statsInput,
  output = data.statsOutput
) {

  def countReads(file: File): Integer = {
    val source = io.Source.fromFile( file.toJava )
    val readsNumber = fasta.parseMap( source.getLines ).length
    source.close()
    readsNumber
  }

  def countLines(file: File): Integer = { file.lines.length }


  def instructions: ohnosequences.statika.AnyInstructions = say("Running stats loquat")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val statsCSV: File = context / "output" / "stats.csv"
    val sampleID: String = context.inputFile(data.sampleID).contentAsString

    LazyTry {
      // NOTE: careful, the order has to coincide:
      val stats: Map[String, String] = csv.columnNames.statsHeader.zip(List(
        sampleID,
        countReads( context.inputFile(data.pairedReads1) ).toString,
        countReads( context.inputFile(data.mergedReads) ).toString,
        countReads( context.inputFile(data.pair1NotMerged) ).toString,
        countReads( context.inputFile(data.blastNoHits) ).toString
      )).toMap

      val csvWriter = csv.newWriter(statsCSV)

      // header:
      csvWriter.writeRow(stats.keys.toSeq)
      // values:
      csvWriter.writeRow(stats.values.toSeq)

      csvWriter.close()
    } -&-
    success(s"Stats for the [${sampleID}] are ready",
      data.sampleStatsCSV(statsCSV) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }

}
