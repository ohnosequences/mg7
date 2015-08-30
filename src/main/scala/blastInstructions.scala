package ohnosequences.metagenomica

import ohnosequences.loquat._, dataProcessing._
import ohnosequences.statika.instructions._
import ohnosequences.blast._, api._, data._
import ohnosequences.cosas._, typeSets._, types._, properties._
import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
import java.io.{ BufferedWriter, FileWriter, File }
import ohnosequences.fastarious._, fasta._, fastq._
import ohnosequences.blast._, api._, data._, outputFields._

case object blastInstructions {

  // TODO with great power comes great responsibility. Move to conf
  case object outRec extends BlastOutputRecord(qseqid :&: sseqid :&: □)
  case object outputType extends BlastOutputType(outRec) { val label = toString }

  trait AnyBlastProcess extends AnyDataProcessingBundle {

    type FastqInput <: AnyData
    val fastqInput: FastqInput

    type BlastOutput <: AnyBlastOutput
    val blastOutput: BlastOutput

    // TODO add 16s db
    type Input = FastqInput :^: DNil
    type Output = BlastOutput :^: DNil

    private def blastOutputFile(context: Context): File = (context / "mapping.out").javaFile

    def processData(
      dataMappingId: String,
      context: Context
    ): Instructions[OutputFiles] = {

      val totalOutput = context / "blastAll.csv"

      lazy val quartets = io.Source.fromFile( context.file(fastqInput).javaFile ).getLines grouped(4)

      quartets map {

        quartet => {

          // we only care about the id and the seq here
          val read = FASTA(
              header(FastqId(quartet(0)).toFastaHeader) :~:
              fasta.sequence(FastaLines(quartet(1)))    :~: ∅
            )

          val readFile = writeFastaToFile(read, context / "read.fa")
          val outFile = context / "blastRead.csv"

          val args = blastn.arguments(
            db(refDB)       :~:
            query(readFile) :~:
            out(outFile)    :~: ∅
          )

          val expr = blastExpr(args)

          // run!
          import scala.sys.process._
          expr.cmd.!

          // we should have something in args getV out now. Append it!
          appendTo(expr.argumentValues getV out, totalOutput)

          // clean
          readFile.delete; outFile.delete
        }
      }

      // everything done. Return totalOutput as BLAST output whatever
      ???
    }
  }

  private def blastExpr(args: ValueOf[blastn.Arguments]): BlastExpression[blastn, outRec.type] =
    BlastExpression(blastn)(outRec)(
      argumentValues  = args,
      // TODO whatever
      optionValues    = blastn.defaults update (num_threads(24) :~: ∅)
    )

  private def writeFastaToFile(v: ValueOf[FASTA], file: File): File = {

    val bw = new BufferedWriter(new FileWriter(file))

    v.toLines foreach { l => bw.write(l) }

    bw.close()
    file
  }

  private def appendTo(append: File, to: File): File = {

    val bw = new BufferedWriter(new FileWriter(to, true))

    io.Source.fromFile(append).getLines foreach { l => bw write l }

    bw.close
    to
  }

  private def refDB: File = ???
}
