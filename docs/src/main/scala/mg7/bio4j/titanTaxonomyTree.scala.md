
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
```

you can get one (or none) from a titan graph by id

```scala
    def getNode(id: String): Option[TitanTaxonNode] =
      optional(graph.nCBITaxonIdIndex.getVertex(id)).map(TitanTaxonNode)
  }
}

```




[main/scala/mg7/bio4j/bundle.scala]: bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: ../csv.scala.md
[main/scala/mg7/data.scala]: ../data.scala.md
[main/scala/mg7/dataflow.scala]: ../dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: ../dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: ../dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: ../loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: ../loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: ../package.scala.md
[main/scala/mg7/parameters.scala]: ../parameters.scala.md
[test/scala/mg7/counts.scala]: ../../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/lca.scala]: ../../../../test/scala/mg7/lca.scala.md
[test/scala/mg7/pipeline.scala]: ../../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../../test/scala/mg7/taxonomy.scala.md