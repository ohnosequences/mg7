package ohnosequences.mg7.loquats

import ohnosequences.mg7._

import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._

import ohnosequences.loquat._

import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import better.files._


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

    import com.github.tototoshi.csv._

    // same thing that we do for lca and bbh
    def processFile(f: File): File = {
      val csvReader: CSVReader = CSVReader.open( f.toJava )
      val counts: Map[TaxID, (Int, Int)] = accumulatedCounts(
        // FIXME: use some csv api instead of row(1)
        directCounts( csvReader.iterator.map{ row => row(1) }.toList )
      )
      csvReader.close

      val outFile = context / s"${f.name}.counts"
      val csvWriter = CSVWriter.open(outFile.toJava, append = true)
      counts foreach { case (taxId, (dir, acc)) => csvWriter.writeRow( List(taxId, dir, acc) ) }
      csvWriter.close

      outFile
    }

    val lcaOut: File = processFile( context.inputFile(data.lcaCSV) )
    val bbhOut: File = processFile( context.inputFile(data.bbhCSV) )

    success(
      s"Results are written to ...",
      data.lcaCountsCSV(lcaOut) ::
      data.bbhCountsCSV(bbhOut) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
