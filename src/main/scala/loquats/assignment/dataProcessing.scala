package ohnosequences.metagenomica.loquats.taxonomy

import  ohnosequences.metagenomica._

import ohnosequences.metagenomica.bio4j._, taxonomyTree._, titanTaxonomyTree._

import ohnosequences.loquat._, dataProcessing._
import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._
import ohnosequencesBundles.statika.Blast
import ohnosequences.blast._, api._, data._
import ohnosequences.cosas._, typeSets._, types._, properties._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import java.io.{ BufferedWriter, FileWriter, File }
import ohnosequences.fastarious._, fasta._, fastq._
import ohnosequences.blast._, api._, data._, outputFields._

case object taxonomyDataProcessing {

  trait AnyTaxonomyDataProcessing extends AnyDataProcessingBundle {

    def instructions: AnyInstructions = say("Let's see who is who!")

    val bundleDependencies: List[AnyBundle] = List[AnyBundle](
      bundles.bio4jTaxonomy,
      bundles.filteredGIs
    )

    type BlastOutput <: AnyBlastOutput
    val  blastOutput: BlastOutput

    type Input  = BlastOutput :^: DNil
    // type Output =  :^: DNil

    def processData(
      dataMappingId: String,
      context: Context
    ): Instructions[OutputFiles] = {

      import com.github.tototoshi.csv._

      // Reading TSV file with mapping gis-taxIds
      val gisReader: CSVReader = CSVReader.open( bundles.filteredGIs.location )(new TSVFormat {})
      val gisMap: Map[String, String] = gisReader.iterator.map { row =>
        row(0) -> row(1)
      }.toMap

      val blastReader: CSVReader = CSVReader.open( context.file(blastOutput).javaFile )
      val blastRecord = blastOutput.dataType.blastExpressionType.outputRecord
      // TODO: use directly blast output/record from the blast loquat config
      val headers: Seq[String] = ??? //blastRecord.properties.mapToList(typeLabel)

      // TODO: use GIs from the blast output to retrieve taxon nodes, using titanTaxonNodes method
      val gis: List[String] = blastReader.iterator.flatMap { row =>
        val columns: Map[String, String] = headers.zip(row).toMap
        columns.get(sgi.label)
      }.toList

      // TODO: merge this with gis
      val taxIds: List[String] = gis flatMap gisMap.get

      val nodes: List[TitanTaxonNode] = titanTaxonNodes(bundles.bio4jTaxonomy.graph, taxIds)

      val lca: Solution = solution(nodes)

      // TODO: write something to the output file
      ???
    }
  }

}
