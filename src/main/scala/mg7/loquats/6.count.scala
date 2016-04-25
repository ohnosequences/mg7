package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._

import ohnosequences.loquat._

import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._

import better.files._
import com.github.tototoshi.csv._

import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph


case object countDataProcessing extends DataProcessingBundle(
  bio4j.taxonomyBundle
)(input = data.countInput,
  output = data.countOutput
) {

  lazy val taxonomyGraph: TitanNCBITaxonomyGraph = bio4j.taxonomyBundle.graph

  def instructions: AnyInstructions = say("I'm counting you!")

  // returns count of the given element and a filtered list (without that element)
  def count[X](x: X, list: List[X]): (Int, List[X]) =
    list.foldLeft( (0, List[X]()) ) { case ((count, rest), next) =>
      if (next == x) (count + 1, rest)
      else (count, next :: rest)
    }

  def directCounts(
    nodes: List[AnyTaxonNode]
  ): Map[AnyTaxonNode, Int] = {

    @scala.annotation.tailrec
    def rec(
      list: List[AnyTaxonNode],
      acc: Map[AnyTaxonNode, Int]
    ): Map[AnyTaxonNode, Int] = list match {
      case Nil => acc
      case h :: t => {
        val (n, rest) = count(h, t)
        rec(rest, acc.updated(h, n + 1))
      }
    }

    rec(nodes, Map[AnyTaxonNode, Int]())
  }

  def accumulatedCounts(
    directCounts: Map[AnyTaxonNode, Int]
  ): Map[AnyTaxonNode, Int] = {

    directCounts.foldLeft(
      Map[AnyTaxonNode, Int]()
    ) { case (accumCounts, (node, directCount)) =>

      node.lineage.foldLeft(
        accumCounts
      ) { (newAccumCounts, ancestor) =>

        val accumulated = newAccumCounts.get(ancestor).getOrElse(0)

        newAccumCounts.updated(
          ancestor,
          accumulated + directCount
        )
      }
    }
  }

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    // same thing that we do for lca and bbh
    def processFile(assignsFile: File): (File, File, File, File) = {

      val assignsReader: CSVReader = csv.newReader(assignsFile)
      val taxIDs: List[TaxID] = assignsReader.allWithHeaders.map { row => row(csv.columnNames.TaxID) }
      assignsReader.close

      // there as many assigned reads as there are tax IDs in the table
      val assignedReadsNumber: Double = taxIDs.length
      def frequency(absolute: Int): Double = absolute / assignedReadsNumber

      def nodes: List[AnyTaxonNode] = taxIDs.flatMap(taxonomyGraph.getNode(_))

      val direct:      Map[AnyTaxonNode, Int] = directCounts(nodes)
      val accumulated: Map[AnyTaxonNode, Int] = accumulatedCounts(direct)

      val filesPrefix: String = assignsFile.name.stripSuffix(".csv")

      val outDirectFile = context / s"${filesPrefix}.direct.counts"
      val outAccumFile  = context / s"${filesPrefix}.accum.counts"
      val outDirectFreqFile = context / s"${filesPrefix}.direct.frequency.counts"
      val outAccumFreqFile  = context / s"${filesPrefix}.accum.frequency.counts"

      val csvDirectWriter = csv.newWriter(outDirectFile)
      val csvAccumWriter  = csv.newWriter(outAccumFile)
      val csvDirectFreqWriter = csv.newWriter(outDirectFreqFile)
      val csvAccumFreqWriter  = csv.newWriter(outAccumFreqFile)

      def headerFor(file: File) = List(
        csv.columnNames.TaxID,
        csv.columnNames.TaxRank,
        csv.columnNames.TaxName,
        file.name.replaceAll("\\.", "-")
      )
      csvDirectWriter.writeRow(headerFor(outDirectFile))
      csvAccumWriter.writeRow(headerFor(outAccumFile))
      csvDirectFreqWriter.writeRow(headerFor(outDirectFreqFile))
      csvAccumFreqWriter.writeRow(headerFor(outAccumFreqFile))

      def writeCounts(
        counts: Map[AnyTaxonNode, Int],
        writerAbs: CSVWriter,
        writerFrq: CSVWriter
      ) = counts foreach { case (node, absoluteCount) =>

        def row(count: String) = Seq[String](
          node.id,
          node.rank,
          node.name,
          count,
          node.lineage.map(_.id).mkString("; ")
        )

        writerAbs.writeRow(row( absoluteCount.toString ))
        writerFrq.writeRow(row( f"${frequency(absoluteCount)}%.6f" ))
      }

      writeCounts(direct,      csvDirectWriter, csvDirectFreqWriter)
      writeCounts(accumulated, csvAccumWriter,  csvAccumFreqWriter)

      csvDirectWriter.close
      csvAccumWriter.close
      csvDirectFreqWriter.close
      csvAccumFreqWriter.close

      (
        outDirectFile,
        outAccumFile,
        outDirectFreqFile,
        outAccumFreqFile
      )
    }

    val lcaCounts = processFile( context.inputFile(data.lcaCSV) )
    val bbhCounts = processFile( context.inputFile(data.bbhCSV) )

    success(
      s"Results are written to ...",
      // LCA
      data.lcaDirectCountsCSV(lcaCounts._1) ::
      data.lcaAccumCountsCSV(lcaCounts._2) ::
      data.lcaDirectFreqCountsCSV(lcaCounts._3) ::
      data.lcaAccumFreqCountsCSV(lcaCounts._4) ::
      // BBH
      data.bbhDirectCountsCSV(bbhCounts._1) ::
      data.bbhAccumCountsCSV(bbhCounts._2) ::
      data.bbhDirectFreqCountsCSV(bbhCounts._3) ::
      data.bbhAccumFreqCountsCSV(bbhCounts._4) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
