
```scala
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
```

Particular instance of AnyTaxonNode

```scala
  case class TitanTaxonNode(titanTaxon: TitanNCBITaxon) extends AnyVal with AnyTaxonNode {

    def id: String = titanTaxon.id()

    def parent: Option[TitanTaxonNode] =
      optional(titanTaxon.ncbiTaxonParent_inV) map TitanTaxonNode
  }
```

you can get one (or none) from a titan graph by id

```scala
  def titanTaxonNode(graph: TitanNCBITaxonomyGraph, id: String): Option[TitanTaxonNode] =
    optional(graph.nCBITaxonIdIndex getVertex id) map TitanTaxonNode
```

of by several ids, here non-existring ids are just filtered out

```scala
  def titanTaxonNodes(graph: TitanNCBITaxonomyGraph, ids: List[String]): List[TitanTaxonNode] =
    ids flatMap { titanTaxonNode(graph, _) }

}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: ../bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: ../bundles/blast.scala.md
[main/scala/metagenomica/bundles/blast16s.scala]: ../bundles/blast16s.scala.md
[main/scala/metagenomica/bundles/flash.scala]: ../bundles/flash.scala.md
[main/scala/metagenomica/bundles/gis.scala]: ../bundles/gis.scala.md
[main/scala/metagenomica/data.scala]: ../data.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: ../dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../package.scala.md
[main/scala/metagenomica/parameters.scala]: ../parameters.scala.md
[test/scala/bundles.scala]: ../../../../test/scala/bundles.scala.md
[test/scala/lca.scala]: ../../../../test/scala/lca.scala.md
[test/scala/metagenomica/pipeline.scala]: ../../../../test/scala/metagenomica/pipeline.scala.md