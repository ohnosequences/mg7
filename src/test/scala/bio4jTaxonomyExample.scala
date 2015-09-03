package ohnosequences.metagenomica.tests

case object bio4jTaxonomyExample {

  import com.bio4j.titan.util.DefaultTitanGraph
  import org.apache.commons.configuration.BaseConfiguration
  import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph
  import com.thinkaurelius.titan.core._, schema._


  import com.bio4j.model.ncbiTaxonomy._

  val dbFolder: String = "some/where/beyond/the/sea"

  type Taxon = vertices.NCBITaxon[DefaultTitanGraph,TitanVertex,VertexLabelMaker,TitanEdge,EdgeLabelMaker]

  lazy val conf = {

    val c = new BaseConfiguration
    c.setProperty("storage.directory", dbFolder)
    c.setProperty("storage.backend", "berkeleyje")
    c
  }

  // the graph; its only (direct) use is for indexes and types
  lazy val g = new TitanNCBITaxonomyGraph( new DefaultTitanGraph(TitanFactory.open(conf)) )
  lazy val byId = g.nCBITaxonIdIndex

  import g._

  import scala.compat.java8.OptionConverters._
  // get a taxon by id; returns optional
  lazy val myTaxon: Option[Taxon] = (byId getVertex "ACF12431CC|ADFDA").asScala

  val u: Option[String] = myTaxon map { _.id }

  val parent = myTaxon flatMap { _.ncbiTaxonParent_inV.asScala }
}
