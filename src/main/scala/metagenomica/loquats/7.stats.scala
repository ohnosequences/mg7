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


  def instructions: ohnosequences.statika.AnyInstructions = ???

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val statsCSV: File = context / "output" / "stats.csv"
    val sampleID: String = context.inputFile(data.sampleID).contentAsString

    LazyTry {
      val stats = Map[String, String](
        "Sample-ID"        -> sampleID,
        "Input-pairs"      -> countReads( context.inputFile(data.pairedReads1) ).toString,
        "Merged"           -> countReads( context.inputFile(data.mergedReads) ).toString,
        "Not-merged"       -> countReads( context.inputFile(data.pair1NotMerged) ).toString,
        "No-Blast-hits"    -> countReads( context.inputFile(data.blastNoHits) ).toString,
        "LCA-not-assigned" -> countLines( context.inputFile(data.lcaNotAssigned) ).toString,
        "BBH-not-assigned" -> countLines( context.inputFile(data.bbhNotAssigned) ).toString
      )

      val csvWriter = CSVWriter.open(statsCSV.toJava, append = true)

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
