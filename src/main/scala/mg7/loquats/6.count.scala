package ohnosequences.mg7.loquats

import ohnosequences.mg7._, csv._
import ohnosequences.ncbitaxonomy._, api.{ Taxa => TaxaOps, Taxon => _, _ }, titan._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import better.files._
import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph

case object countDataProcessing extends DataProcessingBundle(
  bio4j.taxonomyBundle
)(
  input   = data.countInput,
  output  = data.countOutput
)
{

  lazy val taxonomyGraph: TitanNCBITaxonomyGraph = bio4j.taxonomyBundle.graph

  def instructions: AnyInstructions = say("I'm counting you!")

  // returns count of the given element and a filtered list (without that element)
  def count[X](x: X, list: List[X]): (Int, List[X]) =
    list.foldLeft( (0, List[X]()) ) { case ((count, rest), next) =>
      if (next == x) (count + 1, rest)
      else (count, next :: rest)
    }

  def directCounts(
    taxIDs: Taxa,
    getLineage: Taxon => Taxa
  ): Map[Taxon, (Int, Taxa)] = {

    @scala.annotation.tailrec
    def rec(
      list: Taxa,
      acc: Map[Taxon, (Int, Taxa)]
    ): Map[Taxon, (Int, Taxa)] = list match {
      case Nil => acc
      case h :: t => {
        val (n, rest) = count(h, t)
        rec(rest, acc.updated(h, (n + 1, getLineage(h))))
      }
    }

    rec(taxIDs, Map())
  }

  def accumulatedCounts(
    directCounts: Map[Taxon, (Int, Taxa)],
    getLineage: Taxon => Taxa
  ): Map[Taxon, (Int, Taxa)] = {

    directCounts.foldLeft(
      Map[Taxon, (Int, Taxa)]()
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
    // TODO create a local case class or record for the return type
    def processFile(assignsFile: File): (File, File, File, File) = {

      val assignsReader = csv.Reader(assignment.columns)(assignsFile)

      val assigns: List[(Taxon, String)] = assignsReader.rows.map { row =>
        (
          row.select(columns.Taxa),
          row.select(columns.Pident)
        )
      }.toList

      assignsReader.close()

      val taxIDs: Taxa = assigns.map(_._1)

      val averagePidents: Map[Taxon, String] = assigns
        .toStream.groupBy { _._1 } // group by taxIDs
        .map { case (taxID, pairs) =>

          val pidents: Stream[Double] =
            pairs.flatMap { case (_, pident) => parseDouble(pident) }

          taxID -> f"${averageOf(pidents)}%.2f"
        }

      // there as many assigned reads as there are tax IDs in the table
      val assignedReadsNumber: Double = taxIDs.length
      def frequency(absolute: Int): Double = absolute / assignedReadsNumber

      val lineageMap: scala.collection.mutable.Map[Taxon, Taxa] = scala.collection.mutable.Map()

      def getLineage(id: Taxon): Taxa =
        lineageMap.get(id).getOrElse {
          val lineage = taxonomyGraph.getTaxon(id)
            .map{ _.ancestors }.getOrElse( Seq() )
            .map{ _.id }
          lineageMap.update(id, lineage)
          lineage
        }

      val direct:      Map[Taxon, (Int, Taxa)] = directCounts(taxIDs, getLineage)
      val accumulated: Map[Taxon, (Int, Taxa)] = accumulatedCounts(direct, getLineage)

      val filesPrefix: String = assignsFile.name.stripSuffix(".csv")

      // TODO all this file output format-related code should be part of global configuration
      val outDirectFile = context / s"${filesPrefix}.direct.absolute.counts"
      val outAccumFile  = context / s"${filesPrefix}.accum.absolute.counts"
      val outDirectFreqFile = context / s"${filesPrefix}.direct.frequency.percentage"
      val outAccumFreqFile  = context / s"${filesPrefix}.accum.frequency.percentage"

      val csvDirectWriter     = csv.Writer(csv.counts.columns)(outDirectFile)
      val csvAccumWriter      = csv.Writer(csv.counts.columns)(outAccumFile)
      val csvDirectFreqWriter = csv.Writer(csv.counts.columns)(outDirectFreqFile)
      val csvAccumFreqWriter  = csv.Writer(csv.counts.columns)(outAccumFreqFile)

      // TODO all this file output format-related code should be part of global configuration (see #31)
      def headerRow(file: File) = Row(csv.counts.columns)(csv.counts.header(file.name.replaceAll("\\.", "-")))

      csvDirectWriter.addRow(headerRow(outDirectFile))
      csvAccumWriter.addRow(headerRow(outAccumFile))
      csvDirectFreqWriter.addRow(headerRow(outDirectFreqFile))
      csvAccumFreqWriter.addRow(headerRow(outAccumFreqFile))

      def writeCounts(
        countsMap: Map[Taxon, (Int, Taxa)],
        writerAbs: csv.Writer[csv.counts.Columns],
        writerFrq: csv.Writer[csv.counts.Columns]
      ) = countsMap foreach { case (taxa, (absoluteCount, lineage)) =>

        val node: Option[TitanTaxon] = taxonomyGraph.getTaxon(taxa)

        def row(count: String) = Row(csv.counts.columns)(
          columns.Lineage(lineage.mkString(";")) ::
          columns.Taxa(taxa) ::
          columns.TaxRank(node.map(_.rank).getOrElse("")) ::
          columns.TaxName(node.map(_.name).getOrElse("")) ::
          columns.Count(count) ::
          columns.AveragePident(averagePidents.get(taxa).getOrElse("-")) ::
          *[AnyDenotation]
        )

        writerAbs.addRow(row( absoluteCount.toString ))
        writerFrq.addRow(row( f"${frequency(absoluteCount) * 100}%.4f" ))
      }

      writeCounts(direct,      csvDirectWriter, csvDirectFreqWriter)
      writeCounts(accumulated, csvAccumWriter,  csvAccumFreqWriter)

      csvDirectWriter.close()
      csvAccumWriter.close()
      csvDirectFreqWriter.close()
      csvAccumFreqWriter.close()

      (
        outDirectFile,
        outAccumFile,
        outDirectFreqFile,
        outAccumFreqFile
      )
    }

    // TODO use data records directly instead of this ugly tuple business
    val lcaCounts = processFile( context.inputFile(data.lcaCSV) )
    val bbhCounts = processFile( context.inputFile(data.bbhCSV) )

    success(
      s"Results are written to ...",
      // LCA
      data.lcaDirectCountsCSV(lcaCounts._1)     ::
      data.lcaAccumCountsCSV(lcaCounts._2)      ::
      data.lcaDirectFreqCountsCSV(lcaCounts._3) ::
      data.lcaAccumFreqCountsCSV(lcaCounts._4)  ::
      // BBH
      data.bbhDirectCountsCSV(bbhCounts._1)     ::
      data.bbhAccumCountsCSV(bbhCounts._2)      ::
      data.bbhDirectFreqCountsCSV(bbhCounts._3) ::
      data.bbhAccumFreqCountsCSV(bbhCounts._4)  ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
