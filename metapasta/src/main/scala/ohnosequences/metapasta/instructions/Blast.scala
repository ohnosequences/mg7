package ohnosequences.metapasta.instructions

import ohnosequences.metapasta.databases.BlastDatabase16S
import ohnosequences.metapasta.Factory
import ohnosequences.awstools.s3.{LoadingManager, ObjectAddress}
import java.io.File
import org.clapper.avsl.Logger


trait Blast {
  def launch(commandLine: String, database: BlastDatabase16S, input: File, output: File, useXML: Boolean): Int
}


class BlastFactory() extends Factory[LoadingManager, Blast] {
  val logger = Logger(this.getClass)


  class _Blast extends Blast {
    override def launch(commandLine: String, database: BlastDatabase16S, input: File, output: File, useXML: Boolean): Int = {

      val command =  commandLine
        .replace("$db$", database.name)
        .replace("$output$", output.getPath)
        .replace("$input$", input.getPath)
        .replace("$out_format$", if (useXML) "5" else "6")

      logger.info("running BLAST " + command)
      import scala.sys.process._
      command.!


    }
  }

  override def build(loadingManager: LoadingManager): Blast = {
    import scala.sys.process._
    logger.info("downloading BLAST")
    val blast = ObjectAddress("resources.ohnosequences.com", "blast/ncbi-blast-2.2.25.tar.gz")
    loadingManager.download(blast, new File("ncbi-blast-2.2.25.tar.gz"))

    logger.info("extracting BLAST")
    """tar -xvf ncbi-blast-2.2.25.tar.gz""".!

    logger.info("installing BLAST")
    // new Runtime().exec()
    Seq("sh", "-c", """cp ./ncbi-blast-2.2.25+/bin/* /usr/bin""").!
    new _Blast()
  }
}