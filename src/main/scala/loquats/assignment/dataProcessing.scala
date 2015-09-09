package ohnosequences.metagenomica.loquats.taxonomy

import  ohnosequences.metagenomica._

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
      bundles.bio4jTaxonomy
    )

    type BlastOutput <: AnyBlastOutput
    val  blastOutput: BlastOutput

    type Input  = BlastOutput :^: DNil
    // type Output =  :^: DNil

    def processData(
      dataMappingId: String,
      context: Context
    ): Instructions[OutputFiles] = ???
  }

}
