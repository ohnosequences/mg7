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

import java.io.File
import java.nio.file._
import collection.JavaConversions._

import sys.process._


trait AnyBlastDataProcessing extends AnyDataProcessingBundle {

  type MD <: AnyMetagenomicaData
  val md: MD

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
      // println(s"HAS NEXT: ${quartets.hasNext}")

      quartets foreach { quartet =>
        println("======================")
        println(quartet.mkString("\n"))

        // we only care about the id and the seq here
        val read: ValueOf[FASTA] = FASTA(
            header(FastqId(quartet(0)).toFastaHeader) :~:
            fasta.sequence(FastaLines(quartet(1)))    :~: ∅
          )

        val readFile = context / "read.fa"
        Files.write(
          readFile.toPath,
          asJavaIterable(read.toLines)
        )

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
        println(s"BLAST EXIT CODE: ${foo}")

        // we should have something in args getV out now. Append it!
        println(s"Appending [${outFile.path}] to [${totalOutput.path}]")
        Files.write(
          totalOutput.toPath,
          Files.readAllLines(outFile.toPath),
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
          StandardOpenOption.APPEND
        )

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
