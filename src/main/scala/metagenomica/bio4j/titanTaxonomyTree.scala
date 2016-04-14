package ohnosequences.mg7.bio4j

import taxonomyTree._

case object titanTaxonomyTree {

  import com.bio4j.model.ncbiTaxonomy.NCBITaxonomyGraph._
  import com.bio4j.titan.model.ncbiTaxonomy.TitanNCBITaxonomyGraph
  import com.bio4j.titan.util.DefaultTitanGraph
  import com.thinkaurelius.titan.core._, schema._

  type TitanNCBITaxon = com.bio4j.model.ncbiTaxonomy.vertices.NCBITaxon[
    DefaultTitanGraph,
    TitanVertex, VertexLabelMaker,
    TitanEdge, EdgeLabelMaker
  ]

  // Java to Scala
  final def optional[T](jopt: java.util.Optional[T]): Option[T] = {
    if (jopt.isPresent) Some(jopt.get) else None
  }

  /* Particular instance of AnyTaxonNode */
  case class TitanTaxonNode(titanTaxon: TitanNCBITaxon) extends AnyTaxonNode {

    def id: String = titanTaxon.id()
    // These methods may return null
    def name: String = Option( titanTaxon.name() ).getOrElse("")
    def rank: String = Option( titanTaxon.taxonomicRank() ).getOrElse("")

    def parent: Option[TitanTaxonNode] =
      optional(titanTaxon.ncbiTaxonParent_inV) map TitanTaxonNode
  }


  implicit def titanNCBITaxonomyGraphOps(graph: TitanNCBITaxonomyGraph):
    TitanNCBITaxonomyGraphOps =
    TitanNCBITaxonomyGraphOps(graph)

  case class TitanNCBITaxonomyGraphOps(graph: TitanNCBITaxonomyGraph) extends AnyVal {

    /* you can get one (or none) from a titan graph by id */
    def getNode(id: String): Option[TitanTaxonNode] =
      optional(graph.nCBITaxonIdIndex.getVertex(id)).map(TitanTaxonNode)

    /* of by several ids, here non-existring ids are just filtered out */
    def getNodes(ids: Seq[String]): Seq[TitanTaxonNode] =
      ids.flatMap(getNode)

    // // NOTE: this is kind of unsafe, but we know that there is a root, otherwise nothing makes sense
    // def root(): TitanTaxonNode = getNode("1").get
  }
}
