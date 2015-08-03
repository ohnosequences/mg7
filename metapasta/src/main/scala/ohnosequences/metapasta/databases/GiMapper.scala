package ohnosequences.metapasta.databases

import java.io.File
import ohnosequences.awstools.s3.{LoadingManager, ObjectAddress}
import scala.collection.mutable
import org.clapper.avsl.Logger
import ohnosequences.metapasta.{Taxon, Factory}

/**
 * todo it will moved to bio4j
 */
trait GIMapper {
  def getTaxIdByGi(gi: String): Option[Taxon]
}

class InMemoryGIMapperFactory() extends DatabaseFactory[GIMapper] {

  val logger = Logger(this.getClass)

  class InMemoryGIMapper(map: mutable.HashMap[String, Taxon]) extends GIMapper {
    override def getTaxIdByGi(gi: String): Option[Taxon] = map.get(gi)
  }


  override def build(loadingManager: LoadingManager): GIMapper = {
    val mapping = new mutable.HashMap[String, Taxon]()
    val mappingFile = new File("gi.map")
    loadingManager.download(ObjectAddress("metapasta", "gi.map"), mappingFile)
    val giP = """(\d+)\s+(\d+).*""".r
    for(line <- io.Source.fromFile(mappingFile).getLines()) {
      line match {
        case giP(gi, tax) => mapping.put(gi, Taxon(tax))
        case l => logger.error("can't parse " + l)
      }
    }

    new InMemoryGIMapper(mapping)
  }

}
