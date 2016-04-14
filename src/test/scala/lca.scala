package ohnosequences.mg7.tests

import ohnosequences.mg7.bio4j.taxonomyTree._

class LCATest extends org.scalatest.FunSuite {

  object defs {

    case object root extends AnyTaxonNode {
      val id = "root"
      val name = "root"
      val rank = ""
      val parent = None
    }

    class node(p: AnyTaxonNode) extends AnyTaxonNode {
      val id = this.toString
      val name = id
      val rank = ""
      val parent = Some(p)
    }

    // common part
    case object c1 extends node(root)
    case object c2 extends node(c1)
    // left branch
    case object l1 extends node(c2)
    case object l2 extends node(l1)
    // right branch
    case object r1 extends node(c2)
    case object r2 extends node(r1)
    case object r3 extends node(r2)

    val common = Seq(root, c1, c2)
  }
  import defs._

  test("path to the root") {

    assert{ l2.lineage == Seq(root, c1, c2, l1, l2) }
    assert{ c1.lineage == Seq(root, c1) }
    assert{ root.lineage == Seq(root) }
  }

  test("most specific node or lowest common ancestor") {

    // def ref(n: AnyTaxonNode): Either[Path, Path] = Left(pathToTheRoot(l1, Seq()))

    assertResult( Some(c2) ) {
      lowestCommonAncestor(Seq(l1, r3))
    }

    assertResult( Some(c1) ) {
      lowestCommonAncestor(Seq(l1, c1))
    }

    assertResult( Some(l1) ) {
      lowestCommonAncestor(Seq(l1, l2))
    }

    assertResult( Some(c2) ) {
      lowestCommonAncestor(Seq(c2, r2))
    }

    assertResult( Some(c2) ) {
      lowestCommonAncestor(Seq(l1, r1))
    }

    assertResult( Some(c2) ) {
      lowestCommonAncestor(Seq(c2, c2))
    }

    // Multiple nodes:

    // left, common, right
    assertResult( Some(c2) ) {
      lowestCommonAncestor(Seq(l1, c2, r2))
    }

    // all on the same line
    assertResult( Some(c1) ) {
      lowestCommonAncestor(Seq(c1, l2, l1, c2))
    }

    // some from one branch and some from another
    assertResult( Some(c1) ) {
      lowestCommonAncestor(Seq(c1, l2, r3, c2, r1))
    }
  }

}
