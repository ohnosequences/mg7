package ohnosequences.metagenomica.loquats.blast

import  ohnosequences.metagenomica._, bundles._

import ohnosequences.loquat._, dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.instructions._
import ohnosequencesBundles.statika.Blast

import ohnosequences.blast._, api._, data._, outputFields._

import ohnosequences.cosas._, types._, typeSets._, properties._, records._
import ops.typeSets._

import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._

import ohnosequences.fastarious._, fasta._, fastq._

import java.io.{ BufferedWriter, FileWriter, File }

import sys.process._


case object blastDataProcessing {

  case object blastBundle extends Blast("2.2.31")

  import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._
  import ohnosequences.awstools.regions.Region._

  case object blastCompat extends Compatible(
    amzn_ami_64bit(Ireland, Virtualization.HVM)(1),
    blastBundle,
    generated.metadata.Metagenomica
  )

  // TODO with great power comes great responsibility. Move to conf
  case object outRec extends BlastOutputRecord(
    qseqid    :&:
    qlen      :&:
    qstart    :&:
    qend      :&:
    sseqid    :&:
    slen      :&:
    sstart    :&:
    send      :&:
    bitscore  :&:
    sgi       :&: □
  )
  case object blastExprType extends BlastExpressionType(blastn)(outRec)
  case object blastOutputType extends BlastOutputType(blastExprType, "blastn.blablabla")

  private def blastExpr(args: ValueOf[blastn.Arguments]): BlastExpression[blastExprType.type] = {
    BlastExpression(blastExprType)(
      argumentValues  = args,
      // TODO whatever
      optionValues    = blastn.defaults update (num_threads(1) :~: ∅)
    )
  }

  private def writeFastaToFile(v: ValueOf[FASTA], file: File): File = {

    val bw = new BufferedWriter(new FileWriter(file))

    v.toLines foreach { l => bw.write(l) }

    bw.close()
    file
  }

  private def appendTo(append: File, to: File): File = {
    println(s"Appending [${append.getCanonicalPath}] to [${to.getCanonicalPath}]")

    val bw = new BufferedWriter(new FileWriter(to, true))

    io.Source.fromFile(append).getLines foreach { l => bw write l }

    bw.close
    to
  }


  trait AnyBlastDataProcessing extends AnyDataProcessingBundle {

    def instructions: AnyInstructions = say("Let the blasting begin!")

    // this is defined in the constructor
    // val bundleDependencies: List[AnyBundle] = List[AnyBundle](blastBundle, blast16s)

    type FastqInput <: AnyData
    val  fastqInput: FastqInput

    type BlastOutput <: AnyBlastOutput
    val  blastOutput: BlastOutput

    type Input  <: FastqInput :^: DNil
    type Output <: BlastOutput :^: DNil

    // What's this?
    // private def blastOutputFile(context: Context): File = (context / "mapping.out").javaFile

    def processData(
      dataMappingId: String,
      context: Context
    ): Instructions[OutputFiles] = {

      val totalOutput = context / "blastAll.csv"

      LazyTry {
        lazy val quartets = io.Source.fromFile( context.file(fastqInput).javaFile ).getLines.grouped(4)

        quartets map { quartet =>

          // we only care about the id and the seq here
          val read = FASTA(
              header(FastqId(quartet(0)).toFastaHeader) :~:
              fasta.sequence(FastaLines(quartet(1)))    :~: ∅
            )

          val readFile = writeFastaToFile(read, context / "read.fa")
          val outFile = context / "blastRead.csv"

          val args = blastn.arguments(
            db(blast16s.location) :~:
            query(readFile) :~:
            out(outFile) :~:
            ∅
          )

          val expr = blastExpr(args)

          // BAM!!!
          val foo = expr.cmd.!!
          println(foo)

          // we should have something in args getV out now. Append it!
          appendTo(outFile, totalOutput)

          // clean
          readFile.delete
          outFile.delete
        }
      } -&-
      success(
        "much blast, very success!",
        blastOutput.inFile(totalOutput) :~: ∅
      )

    }
  }


  class BlastDataProcessing[
    F <: AnyData,
    B <: AnyBlastOutput
  ](val fastqInput: F,
    val blastOutput: B
  )(implicit
    parseInputFiles: ParseDenotations[(F :^: DNil)#LocationsAt[FileDataLocation], File],
    outputFilesToMap: ToMap[(B :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
  ) extends DataProcessingBundle(blastBundle, blast16s)(
    input = fastqInput :^: DNil,
    output = blastOutput :^: DNil
  )(parseInputFiles, outputFilesToMap) with AnyBlastDataProcessing {

    type FastqInput = F
    type BlastOutput = B
  }

}
