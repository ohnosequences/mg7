package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._

import ohnosequences.loquat._

import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._

import better.files._
import com.github.tototoshi.csv._



case object countingDataProcessing extends DataProcessingBundle(
  bundles.bio4jNCBITaxonomy
)(input = data.countingInput,
  output = data.countingOutput
) {

  def instructions: AnyInstructions = say("I'm counting you!")

  // returns count of the given id and a filtered list (without that id)
  def count(id: ID, list: List[ID]): (Int, List[ID]) =
    list.foldLeft( (0, List[ID]()) ) { case ((count, rest), next) =>
      if (next == id) (count + 1, rest)
      else (count, next :: rest)
    }

  def directCounts(taxIds: List[TaxID]): Map[TaxID, Int] = {

    @scala.annotation.tailrec
    def rec(list: List[TaxID], acc: Map[TaxID, Int]): Map[TaxID, Int] =
      list match{
        case Nil => acc
        case h :: t => {
          val (n, rest) = count(h, t)
          rec(rest, acc.updated(h, n + 1))
        }
      }

    rec(taxIds, Map[TaxID, Int]())
  }

  // TODO: figure out some more effective algorithm
  // Caution: it uses bio4j bundle!
  def accumulatedCounts(counts: Map[TaxID, Int]): Map[TaxID, (Int, Int)] = {
    counts.foldLeft(
      Map[TaxID, (Int, Int)]()
    ) { case (acc, (id, count)) =>
      val node: Option[TitanTaxonNode] = titanTaxonNode(bundles.bio4jNCBITaxonomy.graph, id)
      val ancestors: Seq[AnyTaxonNode] = node.map{ n => pathToTheRoot(n, Seq()) }.getOrElse(Seq())

      ancestors.foldLeft(
        acc.updated(id, (count, 0))
      ) { (acc, node) =>
        val (direct, accumulated) = acc.get(node.id).getOrElse((0, 0))
        acc.updated(node.id, (direct, accumulated + count))
      }
    }
  }

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    // same thing that we do for lca and bbh
    def processFile(f: File): (File, File) = {
      val csvReader: CSVReader = CSVReader.open( f.toJava )
      val taxons: Map[TaxID, (String, String)] = csvReader.allWithHeaders.map { row =>
        row(columnNames.TaxID) -> ( (row(columnNames.TaxName), row(columnNames.TaxRank)) )
      }.toMap
      csvReader.close

      val counts: Map[TaxID, (Int, Int)] = accumulatedCounts( directCounts(taxons.keys.toList) )

      val outDirectFile = context / s"${f.name}.direct.counts"
      val outAccumFile  = context / s"${f.name}.accum.counts"

      val csvDirectWriter = CSVWriter.open(outDirectFile.toJava, append = true)
      val csvAccumWriter  = CSVWriter.open(outAccumFile.toJava, append = true)

      val header = List(
        columnNames.TaxID,
        columnNames.TaxRank,
        columnNames.TaxName,
        columnNames.Count
      )
      csvDirectWriter.writeRow(header)
      csvAccumWriter.writeRow(header)

      counts foreach { case (taxId, (direct, accum)) =>

        val (name, rank) = taxons.get(taxId).getOrElse(("", ""))
        // We write only non-zero direct counts
        if (direct > 0) { csvDirectWriter.writeRow( List(taxId, rank, name, direct) ) }
        // Accumulated counts shouldn't be ever a zero
        csvAccumWriter.writeRow( List(taxId, rank, name, accum) )
      }

      csvDirectWriter.close
      csvAccumWriter.close

      (outDirectFile, outAccumFile)
    }

    val (lcaDirectOut, lcaAccumOut) = processFile( context.inputFile(data.lcaCSV) )
    val (bbhDirectOut, bbhAccumOut) = processFile( context.inputFile(data.bbhCSV) )

    success(
      s"Results are written to ...",
      data.lcaDirectCountsCSV(lcaDirectOut) ::
      data.bbhDirectCountsCSV(bbhDirectOut) ::
      data.lcaAccumCountsCSV(lcaAccumOut) ::
      data.bbhAccumCountsCSV(bbhAccumOut) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
