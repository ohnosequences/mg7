
```scala
// package ohnosequences.mg7.tests
//
// import ohnosequences.ncbitaxonomy._, api._
//
// case object taxonomy {
//
//   sealed abstract class AnyNode(val parent: Option[AnyNode]) extends AnyTaxonNode {
//
//     val id = this.toString
//     val name = id
//     val rankName = ""
//   }
//
//   abstract class Node(p: AnyNode) extends AnyNode(Some(p))
//
//   case object root extends AnyNode(None)
//   // common part
//   case object c1 extends Node(root)
//   case object c2 extends Node(c1)
//   // left branch
//   case object l1 extends Node(c2)
//   case object l2 extends Node(l1)
//   // right branch
//   case object r1 extends Node(c2)
//   case object r2 extends Node(r1)
//   case object r3 extends Node(r2)
//
//   val common = Seq(root, c1, c2)
//
//   val allNodes: Set[AnyNode] = Set(root, c1, c2, l1, l2, r1, r2, r3)
//
//   val id2node: Map[String, AnyNode] = allNodes.map{ n => (n.id -> n) }.toMap
// }

```




[main/scala/mg7/bundles.scala]: ../../../main/scala/mg7/bundles.scala.md
[main/scala/mg7/configs.scala]: ../../../main/scala/mg7/configs.scala.md
[main/scala/mg7/csv.scala]: ../../../main/scala/mg7/csv.scala.md
[main/scala/mg7/data.scala]: ../../../main/scala/mg7/data.scala.md
[main/scala/mg7/defaults.scala]: ../../../main/scala/mg7/defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../../../main/scala/mg7/loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../../../main/scala/mg7/loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../../../main/scala/mg7/loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../../../main/scala/mg7/loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../../../main/scala/mg7/loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../../../main/scala/mg7/loquats/6.count.scala.md
[main/scala/mg7/package.scala]: ../../../main/scala/mg7/package.scala.md
[main/scala/mg7/parameters.scala]: ../../../main/scala/mg7/parameters.scala.md
[main/scala/mg7/pipeline.scala]: ../../../main/scala/mg7/pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: ../../../main/scala/mg7/referenceDB.scala.md
[test/scala/mg7/counts.scala]: counts.scala.md
[test/scala/mg7/fqnames.scala]: fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: mock/illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: mock/pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: PRJEB6592/PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: taxonomy.scala.md
[test/scala/mg7/testData.scala]: testData.scala.md
[test/scala/mg7/testDefaults.scala]: testDefaults.scala.md