
```scala
package ohnosequences.mg7.tests

import ohnosequences.mg7._
import ohnosequences.mg7.bio4j.taxonomyTree._
import ohnosequences.mg7.loquats.countDataProcessing._

import ohnosequences.mg7.tests.taxonomy._


case object countsCtx {

  val realCounts = Map[AnyNode, Int](
    c2 -> 4,
    l2 -> 2,
    r1 -> 3,
    r3 -> 5
  )

  val ids: List[TaxID] =
    realCounts.flatMap { case (node, count) =>
      List.fill(count)(node.id)
    }.toList

  def getLineage(id: TaxID): Seq[TaxID] = id2node(id).lineage.map(_.id)

  val direct: Map[TaxID, (Int, Seq[TaxID])] = directCounts(ids, getLineage)

  val accumulated: Map[TaxID, (Int, Seq[TaxID])] = accumulatedCounts(direct, getLineage)
}


class CountsTest extends org.scalatest.FunSuite {
  import countsCtx._

  def assertMapsDiff[A, B](m1: Map[A, B], m2: Map[A, B]): Unit = {
    assertResult( List() ) {
      m1.toList diff m2.toList
    }
  }


  test("direct counts") {

    assertMapsDiff(
      realCounts.map { case (n, c) => n.id -> c },
      direct.map { case (id, (c, _)) => id -> c }
    )
  }

  test("accumulated counts") {

    // accumulated.foreach{ case (id, i) => info(s"${id}\t-> ${i}") }

    assertMapsDiff(
      accumulated.map { case (id, (c, _)) => id -> c },
      Map(
          root.id -> 14,
            c1.id -> 14,
            c2.id -> 14,
        l1.id -> 2, r1.id -> 8,
        l2.id -> 2, r2.id -> 5,
                    r3.id -> 5
      )
    )
  }

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
[test/scala/mg7/counts.scala]: counts.scala.md
[test/scala/mg7/lca.scala]: lca.scala.md
[test/scala/mg7/pipeline.scala]: pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: taxonomy.scala.md