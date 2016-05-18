package ohnosequences.mg7.loquats

import ohnosequences.mg7._
import ohnosequences.mg7.bio4j._, taxonomyTree._, titanTaxonomyTree._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.blast.api._, outputFields._

import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph

import java.io.{ BufferedWriter, FileWriter, File }
import scala.util.Try

import com.github.tototoshi.csv._


case class assignDataProcessing[MD <: AnyMG7Parameters](val md: MD)
extends DataProcessingBundle()(
  input  = data.assignInput,
  output = data.assignOutput
) {
  // For the output fields implicits
  import md._

  override val bundleDependencies: List[AnyBundle] =
    bio4j.taxonomyBundle :: md.referenceDBs.toList

  lazy val taxonomyGraph: TitanNCBITaxonomyGraph = bio4j.taxonomyBundle.graph

  type BlastRow = csv.Row[md.blastOutRec.Keys]

  // This iterates over reference DBs and merges their id2taxa tables in one Map
  lazy val referenceMap: Map[ID, Seq[TaxID]] = {
    val refMap: scala.collection.mutable.Map[ID, Seq[TaxID]] = scala.collection.mutable.Map()

    md.referenceDBs.foreach { refDB =>
      val tsvReader = CSVReader.open( refDB.id2taxa.toJava )(csv.UnixTSVFormat)
      tsvReader.iterator.foreach { row =>
        refMap.updated(
          // first column is the ID
          row(0),
          // second column is a sequence of tax IDs separated with ';'
          row(1).split(';').map(_.trim)
        )
      }
      tsvReader.close
    }

    refMap.toMap
  }

  def instructions: AnyInstructions = say("Let's see who is who!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val blastReader = csv.Reader(md.blastOutRec.keys, context.inputFile(data.blastChunk))

    // Outs:
    val lcaFile = (context / "output" / "lca.csv").createIfNotExists()
    val bbhFile = (context / "output" / "bbh.csv").createIfNotExists()
    val lcaWriter = csv.newWriter(lcaFile)
    val bbhWriter = csv.newWriter(bbhFile)

    val lostInMappingFile = (context / "output" / "lost.in-mapping").createIfNotExists()
    val lostInBio4jFile   = (context / "output" / "lost.in-bio4j").createIfNotExists()

    blastReader.rows
      // grouping rows by the read id
      .toStream.groupBy { _.select(qseqid) }
      // for each read evaluate LCA and BBH and write the output files
      .foreach { case (readId: ID, hits: Stream[BlastRow]) =>

        // for each hit row we take the column with ID and lookup its TaxID
        val nodesMap: Map[TitanTaxonNode, BlastRow] =
          hits.foldLeft(Map[TitanTaxonNode, BlastRow]()) { (acc, row) =>

            referenceMap.get(row.select(sseqid))
              .toSeq.flatten
              .flatMap(taxonomyGraph.getNode)
              .foldLeft(acc) { (accNodes, node) =>
                accNodes.updated(node, row)
              }
          }

        val bbh: BBH = {
          // best blast score is just a maximum in the `bitscore` column
          val maximum = nodesMap.maxBy { case (node, row) =>
            parseInt(row.select(bitscore)).getOrElse(0)
          }
          Some(maximum._1)
        }

        // and return the taxon node ID corresponding to the read
        val lca: LCA = lowestCommonAncestor(nodesMap.keys.toSeq)

        // NOTE: this shouldn't ever happen, so we throw an error here
        if (lca.isEmpty) sys.error("Failed to compute LCA; something is broken")

        // writing results
        lca.foreach { node => lcaWriter.writeRow(List(readId, node.id, node.name, node.rank)) }
        bbh.foreach { node => bbhWriter.writeRow(List(readId, node.id, node.name, node.rank)) }
      }

    blastReader.close

    lcaWriter.close
    bbhWriter.close

    success(s"Results are ready",
      data.lcaChunk(lcaFile) ::
      data.bbhChunk(bbhFile) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
