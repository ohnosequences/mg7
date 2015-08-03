package ohnosequences.metapasta.instructions

import ohnosequences.metapasta.databases.LastDatabase16S
import ohnosequences.metapasta.Factory
import ohnosequences.awstools.s3.{LoadingManager, ObjectAddress}
import java.io.File
import org.clapper.avsl.Logger


trait Last {
  def launch(commandLine: String, database: LastDatabase16S, input: File, output: File, fastaInput: Boolean): Int
}


class LastFactory extends Factory[LoadingManager, Last] {
  val logger = Logger(this.getClass)

  class _Last extends Last {
    override def launch(commandLine: String, database: LastDatabase16S, input: File, output: File, fastaInput: Boolean): Int = {

      val command =  commandLine
        .replace("$db$", database.name)
        .replace("$output$", output.getPath)
        .replace("$input$", input.getPath)
        .replace("$format$", if (fastaInput) "0" else "1") //sanger format is more similar to modern illumina!

      logger.info("running LAST " + command)
      import scala.sys.process._
      command.!
    }
  }

  override def build(loadingManager: LoadingManager): Last = {
    logger.info("downloading LAST")
    val last = ObjectAddress("metapasta", "lastal")
    val f = new File("lastal")
    loadingManager.download(last, f)
    f.setExecutable(true)
    new _Last()
  }
}