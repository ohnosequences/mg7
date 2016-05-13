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
    taxIDs: List[TaxID],
    getLineage: TaxID => Seq[TaxID]
  ): Map[TaxID, (Int, Seq[TaxID])] = {

    @scala.annotation.tailrec
    def rec(
      list: List[TaxID],
      acc: Map[TaxID, (Int, Seq[TaxID])]
    ): Map[TaxID, (Int, Seq[TaxID])] = list match {
      case Nil => acc
      case h :: t => {
        val (n, rest) = count(h, t)
        rec(rest, acc.updated(h, (n + 1, getLineage(h))))
      }
    }

    rec(taxIDs, Map[TaxID, (Int, Seq[TaxID])]())
  }

  def accumulatedCounts(
    directCounts: Map[TaxID, (Int, Seq[TaxID])],
    getLineage: TaxID => Seq[TaxID]
  ): Map[TaxID, (Int, Seq[TaxID])] = {

    directCounts.foldLeft(
      Map[TaxID, (Int, Seq[TaxID])]()
    ) { case (accumCounts, (taxID, (directCount, lineage))) =>

      lineage.foldLeft(
        accumCounts
      ) { (newAccumCounts, ancestorID) =>

        val (accumulated, lineage) =
          newAccumCounts.get(ancestorID)
            .getOrElse( (0, getLineage(ancestorID)) )

        newAccumCounts.updated(
          ancestorID,
          (accumulated + directCount, lineage)
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

      val lineageMap: scala.collection.mutable.Map[TaxID, Seq[TaxID]] = scala.collection.mutable.Map()

      def getLineage(id: TaxID): Seq[TaxID] =
        lineageMap.get(id).getOrElse {
          val lineage = taxonomyGraph.getNode(id)
            .map{ _.lineage }.getOrElse( Seq() )
            .map{ _.id }
          lineageMap.update(id, lineage)
          lineage
        }

      val direct:      Map[TaxID, (Int, Seq[TaxID])] = directCounts(taxIDs, getLineage)
      val accumulated: Map[TaxID, (Int, Seq[TaxID])] = accumulatedCounts(direct, getLineage)

      val filesPrefix: String = assignsFile.name.stripSuffix(".csv")

      val outDirectFile = context / s"${filesPrefix}.direct.absolute.counts"
      val outAccumFile  = context / s"${filesPrefix}.accum.absolute.counts"
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
        file.name.replaceAll("\\.", "-"),
        csv.columnNames.Lineage
      )
      csvDirectWriter.writeRow(headerFor(outDirectFile))
      csvAccumWriter.writeRow(headerFor(outAccumFile))
      csvDirectFreqWriter.writeRow(headerFor(outDirectFreqFile))
      csvAccumFreqWriter.writeRow(headerFor(outAccumFreqFile))

      def writeCounts(
        counts: Map[TaxID, (Int, Seq[TaxID])],
        writerAbs: CSVWriter,
        writerFrq: CSVWriter
      ) = counts foreach { case (taxID, (absoluteCount, lineage)) =>

        val node: Option[TitanTaxonNode] = taxonomyGraph.getNode(taxID)

        def row(count: String) = Seq[String](
          lineage.mkString(";"),
          taxID,
          node.map(_.rank).getOrElse(""),
          node.map(_.name).getOrElse(""),
          count
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
