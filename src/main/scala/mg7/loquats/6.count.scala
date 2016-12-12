package ohnosequences.mg7.loquats

import ohnosequences.mg7._, csv._
import ohnosequences.ncbitaxonomy._, api.{ Taxa => TaxaOps, Taxon => _, _ }, titan._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import better.files._
import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph

case class countDataProcessing() extends DataProcessingBundle(
  deps = ncbiTaxonomyBundle
)(input  = data.countInput,
  output = data.countOutput
) {
  def instructions: AnyInstructions = say("I'm counting you!")

  lazy val taxonomyGraph: TitanNCBITaxonomyGraph =
    ncbiTaxonomyBundle.graph

  // returns count of the given element and a filtered list (without that element)
  def count[X](x: X, list: Seq[X]): (Int, Seq[X]) =
    list.foldLeft( (0, Seq[X]()) ) { case ((count, rest), next) =>
      if (next == x) (count + 1, rest)
      else (count, next +: rest)
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
      case h +: t => {
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


  /* This common code for LCA and BBH counts */
  class Counts(wd: File, prefix: String, assignsFile: File) {

    /* These are some common values that we need to evaluate once and reuse */
    lazy val assigns: List[(Taxon, String)] = {
      val assignsReader = csv.Reader(assignment.columns)(assignsFile)

      val list = assignsReader.rows.map { row =>
        row.select(columns.Taxa) ->
        row.select(columns.Pident)
      }.toList

      assignsReader.close()
      list
    }

    lazy val averagePidents: Map[Taxon, String] = assigns
      .toStream.groupBy { _._1 } // group by taxIDs
      .map { case (taxID, pairs) =>

        val pidents: Stream[Double] =
          pairs.flatMap { case (_, pident) => parseDouble(pident) }

        taxID -> f"${pidents.average}%.2f"
      }

    // there as many assigned reads as there are tax IDs in the table
    lazy val totalAssignedReads: Int = assigns.length


    case object direct extends DirectAccum("direct")
    case object accum  extends DirectAccum("accumulated")

    /* This common code for direct and accumulated counts */
    class DirectAccum(directaccum: String) {

      case object absolute extends AbsRel("absolute.counts")
      case object relative extends AbsRel("frequency.percentage")

      /* This common code for absolute and relative counts */
      class AbsRel(absrel: String) {

        lazy val file = wd / s"${prefix}.${directaccum}.${absrel}"
        lazy val writer: csv.Writer[csv.counts.Columns] = csv.Writer(csv.counts.columns)(file)

        def writeCsvHeader(): Unit =
          writer.addVals( csv.counts.header( file.name.replaceAll("\\.", "-") ) )
      }

      /* This method writes two tables (absolute and relative) with the given counts */
      // NOTE: it writes headers and closes writers in the end so it's supposed to be used once
      def writeCounts(countsMap: Map[Taxon, (Int, Taxa)]) = {
        absolute.writeCsvHeader()
        relative.writeCsvHeader()

        countsMap foreach { case (taxa, (absoluteCount, lineage)) =>

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
          val percentage: Double = absoluteCount / totalAssignedReads * 100

          absolute.writer.addRow(row( absoluteCount.toString ))
          relative.writer.addRow(row( f"${percentage}%.4f" ))
        }

        absolute.writer.close()
        relative.writer.close()
      }
    }

    /* This method processes LCA or BBH assignments computes direct and accumulated counts and writes them to the corresponding tables */
    def processAssignments(): Unit = {
      // NOTE: this mutable map is used in getLineage for memoization of the results that we get from the DB
      val lineageMap: scala.collection.mutable.Map[Taxon, Taxa] = scala.collection.mutable.Map()

      def getLineage(id: Taxon): Taxa = lineageMap.get(id).getOrElse {
        val lineage = taxonomyGraph.getTaxon(id)
          .map{ _.ancestors }.getOrElse( Seq() )
          .map{ _.id }
        lineageMap.update(id, lineage)
        lineage
      }

      val directMap:      Map[Taxon, (Int, Taxa)] = directCounts(assigns.map(_._1), getLineage)
      val accumulatedMap: Map[Taxon, (Int, Taxa)] = accumulatedCounts(directMap, getLineage)

      direct.writeCounts(directMap)
       accum.writeCounts(accumulatedMap)
    }
  }

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    case object lcaCounts extends Counts(context.workingDir, "lca", context.inputFile(data.lcaCSV))
    case object bbhCounts extends Counts(context.workingDir, "bbh", context.inputFile(data.bbhCSV))

    lcaCounts.processAssignments()
    bbhCounts.processAssignments()

    success(s"Done",
      // LCA
      data.lca.direct.absolute(lcaCounts.direct.absolute.file.toJava) ::
      data.lca.accum.absolute (lcaCounts.accum.absolute.file.toJava) ::
      data.lca.direct.relative(lcaCounts.direct.relative.file.toJava) ::
      data.lca.accum.relative (lcaCounts.accum.relative.file.toJava) ::
      // BBH
      data.bbh.direct.absolute(bbhCounts.direct.absolute.file.toJava) ::
      data.bbh.accum.absolute (bbhCounts.accum.absolute.file.toJava) ::
      data.bbh.direct.relative(bbhCounts.direct.relative.file.toJava) ::
      data.bbh.accum.relative (bbhCounts.accum.relative.file.toJava) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
