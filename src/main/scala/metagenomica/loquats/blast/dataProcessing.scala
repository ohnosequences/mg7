package ohnosequences.metagenomica.loquats.blast

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

import sys.process._


case object blastDataProcessing {

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
      optionValues    = blastn.defaults update (
        num_threads(1) :~:
        max_target_seqs(10) :~:
        ohnosequences.blast.api.evalue(0.001)  :~:
        blastn.task(blastn.megablast) :~: ∅
      )
    )
  }

  private def writeFastaToFile(v: ValueOf[FASTA], file: File): File = {

    val bw = new BufferedWriter(new FileWriter(file))

    v.toLines foreach { l => bw.write(l); bw.newLine }

    bw.close()
    file
  }

  private def appendTo(append: File, to: File): File = {
    println(s"Appending [${append.getCanonicalPath}] to [${to.getCanonicalPath}]")

    val bw = new BufferedWriter(new FileWriter(to, true))

    io.Source.fromFile(append).getLines foreach { l => bw write l; bw.newLine }

    bw.close
    to
  }


  trait AnyBlastDataProcessing extends AnyDataProcessingBundle {

    def instructions: AnyInstructions = say("Let the blasting begin!")

    val bundleDependencies: List[AnyBundle] = List[AnyBundle](
      bundles.blast,
      bundles.blast16s
    )

    // TODO: more precise type
    type FastqInput <: AnyData
    val  fastqInput: FastqInput


    // FIXME: we should use this instead of blastCmd, but .cmd requires implicits "/
    // type BlastExpr <: AnyBlastExpression
    // val  blastExpr: BlastExpr
    // val blastCmd: Seq[String]

    type BlastOutput <: AnyBlastOutput
    val  blastOutput: BlastOutput


    type Input = FastqInput :^: DNil
    val  input = fastqInput :^: DNil: Input

    type Output = BlastOutput :^: DNil
    val  output = blastOutput :^: DNil


    def processData(
      dataMappingId: String,
      context: Context
    ): Instructions[OutputFiles] = {

      val totalOutput = context / "blastAll.csv"

      LazyTry {
        lazy val quartets = io.Source.fromFile( context.file(fastqInput).javaFile ).getLines.grouped(4)
        println(s"HAS NEXT: ${quartets.hasNext}")

        quartets foreach { quartet =>
          println("======================")
          println(quartet.mkString("\n"))

          // we only care about the id and the seq here
          val read = FASTA(
              header(FastqId(quartet(0)).toFastaHeader) :~:
              fasta.sequence(FastaLines(quartet(1)))    :~: ∅
            )

          val readFile = writeFastaToFile(read, context / "read.fa")
          val outFile = context / "blastRead.csv"

          val args = blastn.arguments(
            db(bundles.blast16s.dbName) :~:
            query(readFile) :~:
            out(outFile) :~:
            ∅
          )

          val expr = blastExpr(args)
          println(expr.cmd.mkString(" "))

          // BAM!!!
          val foo = expr.cmd.!
          println(s"EXIT CODE: ${foo}")

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
    val parseInputFiles: ParseDenotations[(F :^: DNil)#LocationsAt[FileDataLocation], File],
    val outputFilesToMap: ToMap[(B :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
  ) extends AnyBlastDataProcessing {

    type FastqInput = F
    type BlastOutput = B
  }

}
