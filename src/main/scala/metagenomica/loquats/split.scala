package ohnosequences.metagenomica.loquats

import ohnosequences.metagenomica.configuration._
import ohnosequences.metagenomica.bundles

import ohnosequences.loquat._, dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._

import ohnosequences.blast._, api._, data._, outputFields._

import ohnosequences.cosas._, types._, typeSets._, properties._, records._
import ops.typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.fastarious._, fasta._, fastq._

import java.io.{ BufferedWriter, FileWriter, File }
import java.nio.file._
import collection.JavaConversions._

import sys.process._


trait AnySplitDataProcessing extends AnyDataProcessingBundle {

  type MD <: AnyMetagenomicaData
  val  md: MD

  def instructions: AnyInstructions = say("Splitting, cutting, separating")

  val bundleDependencies: List[AnyBundle] = List()

  type Input = MD#Merged :^: DNil
  type Output = readsChunks.type :^: DNil
  lazy val output = readsChunks :^: DNil


  def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val outputDir = context / "chunks"

    // TODO: move it to the config
    val chunkSize = 5

    lazy val chunks: Iterator[(Seq[String], Int)] = io.Source.fromFile( context.file(md.merged: MD#Merged).javaFile )
      .getLines
      .grouped(4 * chunkSize)
      .zipWithIndex

    chunks foreach { case (chunk, n) =>
      Files.write(
        (outputDir / s"chunk.${n}.fastq").toPath,
        asJavaIterable(chunk),
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE
      )
    }

    success(
      "much blast, very success!",
      readsChunks.inFile(outputDir) :~: ∅
    )

  }
}


class SplitDataProcessing[MD0 <: AnyMetagenomicaData](val md0: MD0)(implicit
  val parseInputFiles: ParseDenotations[(MD0#Merged :^: DNil)#LocationsAt[FileDataLocation], File],
  val outputFilesToMap: ToMap[(readsChunks.type :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
) extends AnySplitDataProcessing {
  type MD = MD0
  val  md = md0

  val input = (md.merged: MD#Merged) :^: DNil
}
