package ohnosequences.metagenomica.flash

import ohnosequences.loquat._, utils._, dataProcessing._
import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.flash._, api._, data._
import ohnosequences.cosas._, typeSets._, types._
import ops.typeSets._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import ohnosequencesBundles.statika.Flash
import java.io.File

case object flashDataProcessing {

  implicit def genericParser[D <: AnyData](implicit d: D): DenotationParser[D, FileDataLocation, File] =
    new DenotationParser(d, d.label)({ f: File => Some(FileDataLocation(f)) })


  case object flashBundle extends Flash("1.2.11")

  trait AnyFlashInstructions extends AnyDataProcessingBundle {

    // Paired end reads as input
    type ReadsType <: AnyReadsType { type EndType = pairedEndType }
    val readsType: ReadsType

    type Reads1 <: AnyPairedEnd1Fastq { type DataType = ReadsType }
    val reads1: Reads1

    type Reads2 <: AnyPairedEnd2Fastq { type DataType = ReadsType }
    val reads2: Reads2

    type Merged <: MergedReads[ReadsType, Reads1, Reads2]
    val  merged: Merged

    type Stats <: MergedReadsStats[Merged]
    val stats: Stats //= new MergedReadsStats(merged)

    type     Input = Reads1 :^: Reads2 :^: DNil
    lazy val input = reads1 :^: reads2 :^: DNil

    type     Output = Merged :^: Stats :^: DNil
    lazy val output = merged :^: stats :^: DNil


    val flashOptions: ValueOf[flash.Options]

    def instructions: AnyInstructions = say("I'm fine")

    val bundleDependencies: List[AnyBundle] = List(flashBundle)

    // TODO FLASh stuff change options, derived from reads type
    final def processData(
      dataMappingId: String,
      context: Context
    ): Instructions[OutputFiles] = {

      val reads1gz: file = context.file(reads1)
      val reads2gz: file = context.file(reads2)

      val reads1fastq: file = reads1gz.rename( _.stripSuffix(".gz") )
      val reads2fastq: file = reads2gz.rename( _.stripSuffix(".gz") )

      // define input
      lazy val flashInput = FlashInputAt(
        new File(reads1fastq),
        new File(reads2fastq)
      )

      // define output
      lazy val flashOutput = FlashOutputAt(context / "output", prefix = "")

      // the FLASh cmd we are going to run
      lazy val flashExpr = FlashExpression(flash)(
        flash.arguments(
          api.input(flashInput)   :~:
          api.output(flashOutput) :~: ∅
        ),
        flashOptions
      )

      // run expression, hope for the best
      cmd("gunzip")(reads1gz) -&-
      cmd("gunzip")(reads2gz) -&-
      flashExpr.cmd -&-
      success(
        s"FLASh merged reads from ${dataMappingId}, much success so fast",
        (merged).inFile(flashOutput.mergedReads)           :~:
        (stats).inFile(flashOutput.lengthNumericHistogram) :~: ∅
      )
    }
  }

  class FlashInstructions[
    RT <: AnyReadsType { type EndType = pairedEndType },
    R1 <: AnyPairedEnd1Fastq { type DataType = RT },
    R2 <: AnyPairedEnd2Fastq { type DataType = RT },
    M <: MergedReads[RT, R1, R2],
    S <: MergedReadsStats[M]
  ](val readsType: RT,
    val reads1: R1,
    val reads2: R2,
    val merged: M,
    val stats: S,
    val flashOptions: ValueOf[flash.Options]
  )(implicit
    val parseInputFiles: ParseDenotations[(R1 :^: R2 :^: DNil)#LocationsAt[FileDataLocation], File],
    val outputFilesToMap: ToMap[(M :^: S :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
  ) extends AnyFlashInstructions {

    type ReadsType = RT
    type Reads1 = R1
    type Reads2 = R2

    type Merged = M
    type Stats = S
  }

}
