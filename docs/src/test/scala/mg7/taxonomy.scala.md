
```scala
package ohnosequences.mg7.tests

import ohnosequences.mg7.bio4j.taxonomyTree._


case object taxonomy {

  sealed abstract class AnyNode(val parent: Option[AnyNode]) extends AnyTaxonNode {

    val id = this.toString
    val name = id
    val rankName = ""
  }

  abstract class Node(p: AnyNode) extends AnyNode(Some(p))

  case object root extends AnyNode(None)
  // common part
  case object c1 extends Node(root)
  case object c2 extends Node(c1)
  // left branch
  case object l1 extends Node(c2)
  case object l2 extends Node(l1)
  // right branch
  case object r1 extends Node(c2)
  case object r2 extends Node(r1)
  case object r3 extends Node(r2)

  val common = Seq(root, c1, c2)

  val allNodes: Set[AnyNode] = Set(root, c1, c2, l1, l2, r1, r2, r3)

  val id2node: Map[String, AnyNode] = allNodes.map{ n => (n.id -> n) }.toMap
}

```




[main/scala/mg7/bio4j/bundle.scala]: ../../../main/scala/mg7/bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: ../../../main/scala/mg7/bio4j/taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: ../../../main/scala/mg7/bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: ../../../main/scala/mg7/csv.scala.md
[main/scala/mg7/data.scala]: ../../../main/scala/mg7/data.scala.md
[main/scala/mg7/dataflow.scala]: ../../../main/scala/mg7/dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: ../../../main/scala/mg7/dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: ../../../main/scala/mg7/dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../../../main/scala/mg7/loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../../../main/scala/mg7/loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../../../main/scala/mg7/loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../../../main/scala/mg7/loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../../../main/scala/mg7/loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../../../main/scala/mg7/loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: ../../../main/scala/mg7/loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: ../../../main/scala/mg7/loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: ../../../main/scala/mg7/package.scala.md
[main/scala/mg7/parameters.scala]: ../../../main/scala/mg7/parameters.scala.md
[main/scala/mg7/referenceDB.scala]: ../../../main/scala/mg7/referenceDB.scala.md
[test/scala/mg7/counts.scala]: counts.scala.md
[test/scala/mg7/lca.scala]: lca.scala.md
[test/scala/mg7/pipeline.scala]: pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: taxonomy.scala.md