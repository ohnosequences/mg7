
```scala
// package ohnosequences.mg7.tests
//
// import ohnosequences.mg7._
// import ohnosequences.ncbitaxonomy._, api.{ Taxa => TaxaOps, Taxon => _, _ }
// import ohnosequences.mg7.loquats.countDataProcessing._
// import ohnosequences.mg7.tests.taxonomy._
//
//
// case object countsCtx {
//
//   val realCounts = Map[AnyNode, Int](
//     c2 -> 4,
//     l2 -> 2,
//     r1 -> 3,
//     r3 -> 5
//   )
//
//   val ids: Taxa =
//     realCounts.flatMap { case (node, count) =>
//       List.fill(count)(node.id)
//     }.toList
//
//   def getLineage(id: Taxon): Taxa = id2node(id).lineage.map(_.id)
//
//   val direct: Map[Taxon, (Int, Taxa)] = directCounts(ids, getLineage)
//
//   val accumulated: Map[Taxon, (Int, Taxa)] = accumulatedCounts(direct, getLineage)
// }
//
//
// class CountsTest extends org.scalatest.FunSuite {
//   import countsCtx._
//
//   def assertMapsDiff[A, B](m1: Map[A, B], m2: Map[A, B]): Unit = {
//     assertResult( List() ) {
//       m1.toList diff m2.toList
//     }
//   }
//
//
//   test("direct counts") {
//
//     assertMapsDiff(
//       realCounts.map { case (n, c) => n.id -> c },
//       direct.map { case (id, (c, _)) => id -> c }
//     )
//   }
//
//   test("accumulated counts") {
//
//     // accumulated.foreach{ case (id, i) => info(s"${id}\t-> ${i}") }
//
//     assertMapsDiff(
//       accumulated.map { case (id, (c, _)) => id -> c },
//       Map(
//           root.id -> 14,
//             c1.id -> 14,
//             c2.id -> 14,
//         l1.id -> 2, r1.id -> 8,
//         l2.id -> 2, r2.id -> 5,
//                     r3.id -> 5
//       )
//     )
//   }
//
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