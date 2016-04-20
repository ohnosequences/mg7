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
extends DataProcessingBundle(
  bio4j.taxonomyBundle,
  md.referenceDB
)(
  input = data.assignInput,
  output = data.assignOutput
) {
  // For the output fields implicits
  import md._

  lazy val taxonomyGraph: TitanNCBITaxonomyGraph = bio4j.taxonomyBundle.graph

  type BlastRow = csv.Row[md.blastOutRec.Keys]

  def instructions: AnyInstructions = say("Let's see who is who!")

  def process(context: ProcessingContext[Input]): AnyInstructions { type Out <: OutputFiles } = {

    val tsvReader = CSVReader.open( md.referenceDB.id2taxa.toJava )(csv.UnixTSVFormat)
    val referenceMap: Map[ID, TaxID] = tsvReader.iterator.map{ row => row(0) -> row(1) }.toMap

    val blastReader = csv.Reader(md.blastOutRec.keys, context.inputFile(data.blastChunk))

    // Outs:
    val lcaFile = (context / "output" / "lca.csv").createIfNotExists()
    val bbhFile = (context / "output" / "bbh.csv").createIfNotExists()
    val lcaWriter = csv.newWriter(lcaFile)
    val bbhWriter = csv.newWriter(bbhFile)

    val lostInMappingFile = (context / "output" / "lost.in-mapping").createIfNotExists()
    val lostInBio4jFile   = (context / "output" / "lost.in-bio4j").createIfNotExists()


    // val assigns: Map[ReadID, (LCA, BBH)] =
    blastReader.rows
      // grouping rows by the read id
      .toStream.groupBy { _.select(qseqid) }
      // for each read evaluate LCA and BBH and write the output files
      .foreach { case (readId: ID, hits: Stream[BlastRow]) =>

        val bbh: BBH = {
          // best blast score is just a maximum in the `bitscore` column
          val maxRow = hits.maxBy { row =>
            parseInt(row.select(bitscore)).getOrElse(0)
          }
          referenceMap.get(maxRow.select(sseqid)).flatMap { taxId =>
            taxonomyGraph.getNode(taxId)
          }
        }

        // for each hit row we take the column with ID and lookup its TaxID
        val (lostInMappingRows, taxIDs): (Seq[BlastRow], Seq[TaxID]) = hits.toSeq
          .foldLeft(Seq[BlastRow](), Seq[TaxID]()) {
            case ((rows, taxIDs), row) =>
              referenceMap.get(row.select(sseqid)) match {
                case None        => (row +: rows, taxIDs)
                case Some(taxID) => (rows, taxID +: taxIDs)
              }
          }

        // then we try to retreive Titan taxon nodes
        val (lostInBio4jTaxIDs, nodes): (Seq[TaxID], Seq[TitanTaxonNode]) =
          taxIDs.distinct.foldLeft(Seq[TaxID](), Seq[TitanTaxonNode]()) {
            case ((lostTaxIDs, nodes), taxID) =>
              taxonomyGraph.getNode(taxID) match {
                case None       => (taxID +: lostTaxIDs, nodes)
                case Some(node) => (lostTaxIDs, node +: nodes)
              }
          }

        // and return the taxon node ID corresponding to the read
        val lca: LCA = Some(taxonomyGraph.lowestCommonAncestor(nodes))

        // writing results
        lca.foreach { node => lcaWriter.writeRow(List(readId, node.id, node.name, node.rank)) }
        bbh.foreach { node => bbhWriter.writeRow(List(readId, node.id, node.name, node.rank)) }

        lostInMappingRows.foreach { row => lostInMappingFile.appendLine(row.values.mkString(",")) }
        lostInBio4jTaxIDs.foreach { taxID => lostInBio4jFile.appendLine(taxID) }
      }

    tsvReader.close
    blastReader.close

    lcaWriter.close
    bbhWriter.close

    success(s"Results are ready",
      data.lcaChunk(lcaFile) ::
      data.bbhChunk(bbhFile) ::
      data.lost.inMapping(lostInMappingFile) ::
      data.lost.inBio4j(lostInBio4jFile) ::
      *[AnyDenotation { type Value <: FileResource }]
    )
  }
}
