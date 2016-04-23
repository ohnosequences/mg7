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
