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

import sys.process._


trait AnyBlastDataProcessing extends AnyDataProcessingBundle {

  type MD <: AnyMetagenomicaData
  val md: MD

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


  def instructions: AnyInstructions = say("Let the blasting begin!")

  val bundleDependencies: List[AnyBundle] = List[AnyBundle](
    bundles.blast,
    bundles.blast16s
  )


  type Input = readsFastq.type :^: DNil
  lazy val input = readsFastq :^: DNil

  type Output = MD#BlastOut :^: DNil


  def processData(
    dataMappingId: String,
    context: Context
  ): Instructions[OutputFiles] = {

    val totalOutput = context / "blastAll.csv"

    LazyTry {
      lazy val quartets = io.Source.fromFile( context.file(readsFastq).javaFile ).getLines.grouped(4)
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

        val expr = md.blastExpr(args)
        println(expr.toSeq.mkString(" "))

        // BAM!!!
        val foo = expr.toSeq.!
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
      (md.blastOut: MD#BlastOut).inFile(totalOutput) :~: ∅
    )

  }
}


class BlastDataProcessing[MD0 <: AnyMetagenomicaData](val md0: MD0)(implicit
  val parseInputFiles: ParseDenotations[(readsFastq.type :^: DNil)#LocationsAt[FileDataLocation], File],
  val outputFilesToMap: ToMap[(MD0#BlastOut :^: DNil)#LocationsAt[FileDataLocation], AnyData, FileDataLocation]
) extends AnyBlastDataProcessing {
  type MD = MD0
  val  md = md0

  val output = (md.blastOut: MD#BlastOut) :^: DNil
}
