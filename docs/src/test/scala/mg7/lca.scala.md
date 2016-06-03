
```scala
package ohnosequences.mg7.tests

import ohnosequences.mg7.bio4j.taxonomyTree._
import ohnosequences.mg7.tests.taxonomy._

class LCATest extends org.scalatest.FunSuite {

  test("lineage") {

    assert{ l2.lineage == Seq(root, c1, c2, l1, l2) }
    assert{ c1.lineage == Seq(root, c1) }
    assert{ root.lineage == Seq(root) }
  }

  test("lowest common ancestor") {

    // Just a shortcut:
    def lca(nodes: Seq[AnyTaxonNode]): Option[AnyTaxonNode] = lowestCommonAncestor(nodes)

    assertResult( None ) { lca(Seq()) }

    assertResult( Some(r3) ) {
      lca(Seq(r3))
    }

    assertResult( Some(root) ) {
      lca(Seq(root, l1))
    }

    assertResult( Some(c2) ) {
      lca(Seq(l1, r3))
    }

    assertResult( Some(c1) ) {
      lca(Seq(l1, c1))
    }

    assertResult( Some(l1) ) {
      lca(Seq(l1, l2))
    }

    assertResult( Some(c2) ) {
      lca(Seq(c2, r2))
    }

    assertResult( Some(c2) ) {
      lca(Seq(l1, r1))
    }

    assertResult( Some(c2) ) {
      lca(Seq(c2, c2))
    }

    // Multiple nodes:

    // left, common, right
    assertResult( Some(c2) ) {
      lca(Seq(l1, c2, r2))
    }

    // all on the same line
    assertResult( Some(c1) ) {
      lca(Seq(c1, l2, l1, c2))
    }

    // some from one branch and some from another
    assertResult( Some(c1) ) {
      lca(Seq(c1, l2, r3, c2, r1))
    }
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
[main/scala/mg7/referenceDB.scala]: ../../../main/scala/mg7/referenceDB.scala.md
[test/scala/mg7/counts.scala]: counts.scala.md
[test/scala/mg7/lca.scala]: lca.scala.md
[test/scala/mg7/pipeline.scala]: pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: taxonomy.scala.md