package ohnosequences.metapasta.databases

import ohnosequences.nisperon.AWS
import ohnosequences.metapasta.Factory
import org.clapper.avsl.Logger
import ohnosequences.awstools.s3.{LoadingManager, ObjectAddress}
import java.io.File

object Blast16SFactory extends DatabaseFactory[BlastDatabase16S] {


  val logger = Logger(this.getClass)

  class BlastDatabase extends BlastDatabase16S {
    val name: String = "nt.march.14.blast/nt.march.14.fasta"

    //gi|313494140|gb|GU939576.1|
    val re = """\Qgi|\E(\d+)[^\d]+.*""".r
    def parseGI(refId: String) : Option[String] = {
      refId match {
        case re(id) => Some(id)
        case s => None
      }
    }
  }

  override def build(loadingManager: LoadingManager): BlastDatabase = {
    logger.info("downloading database")
    loadingManager.downloadDirectory(ObjectAddress("metapasta", "nt.march.14.blast"), new File("."))
    new BlastDatabase()
  }
}
