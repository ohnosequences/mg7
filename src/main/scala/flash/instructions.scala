package ohnosequences.metagenomica.flash

import ohnosequences.loquat._, utils._, dataProcessing._
import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.flash._, api._, data._
import ohnosequences.cosas._, typeSets._, types._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import ohnosequencesBundles.statika.Flash
import java.io.File

case object flashBundle extends Flash("1.2.11")

trait AnyFlashInstructions extends AnyDataProcessingBundle {

  type Data <: AnyFlashData
  val  data: Data

  type Input = Data#Input
  val  input = data.input

  type Output = Data#Output
  val  output = data.output


  def instructions: AnyInstructions = say("I'm fine")

  val bundleDependencies: List[AnyBundle] = List(flashBundle)

  // TODO FLASh stuff change options, derived from reads type
  final def processData(
    dataMappingId: String,
    context: Context
  ): AnyInstructions.withOut[OutputFiles] = {

    val reads1gz: file = context.file(data.reads1)
    val reads2gz: file = context.file(data.reads2)

    val reads1fastq: file = reads1gz.rename( _.stripSuffix(".gz") )
    val reads2fastq: file = reads2gz.rename( _.stripSuffix(".gz") )

    // define input
    lazy val flashInput = FlashInputAt(
      new File(reads1fastq),
      new File(reads2fastq)
    )

    // define output
    lazy val flashOutput = FlashOutputAt(new File("."), "")

    // the FLASh cmd we are going to run
    lazy val flashExpr = FlashExpression(flash)(
      flash.arguments(
        api.input(flashInput)     :~:
        api.output(flashOutput)   :~: ∅
      ),
      data.flashOptions
    )

    // run expression, hope for the best
    cmd("gunzip")(reads1gz) -&-
    cmd("gunzip")(reads2gz) -&-
    flashExpr.cmd -&-
    success(
      s"FLASh merged reads from ${dataMappingId}, much success so fast",
      data.merged.inFile(flashOutput.mergedReads)           :~:
      data.stats.inFile(flashOutput.lengthNumericHistogram) :~: ∅
    )
  }
}

class FlashInstructions[D <: AnyFlashData](val flashData: D) extends AnyFlashInstructions {

  type Data = D
}
