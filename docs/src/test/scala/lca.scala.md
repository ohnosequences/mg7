
```scala
package ohnosequences.mg7.tests

import ohnosequences.mg7.bio4j.taxonomyTree._

class LCATest extends org.scalatest.FunSuite {

  object defs {

    case object root extends AnyTaxonNode {
      val id = "root"
      val parent = None
    }

    class node(p: AnyTaxonNode) extends AnyTaxonNode {
      val id = this.toString
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

  }
  import defs._

  test("path to the root") {

    assert{ pathToTheRoot(l2, Seq()) == Seq(root, c1, c2, l1, l2) }
    assert{ pathToTheRoot(c1, Seq()) == Seq(root, c1) }
    assert{ pathToTheRoot(root, Seq()) == Seq(root) }
  }

  test("intersection") {

    val dPath = pathToTheRoot(l2, Seq())

    assertResult( (List(root, c1, c2), List(r1, r2, r3)) ) {
      intersect(dPath)(r3, Seq())
    }

    assertResult( (List(root, c1, c2, l1, l2), List()) ){
      intersect(dPath)(l2, Seq())
    }

    assertResult( (List(root, c1, c2), List()) ){
      intersect(dPath)(c2, Seq())
    }

    assertResult( (List(root), List()) ){
      intersect(dPath)(root, Seq())
    }

    assertResult( (List(), List(root, c1, c2, r1)) ){
      intersect(Seq())(r1, Seq())
    }

    assertResult( (List(root, c1, c2), List(r1, r2)) ) {
      intersect(dPath)(r2, Seq())
    }
  }

  test("most specific node or lowest common ancestor") {

    def ref(n: AnyTaxonNode): Either[Path, Path] = Left(pathToTheRoot(l1, Seq()))

    assertResult( LCA(List(root, c1, c2)) ) {
      solution(List(l1, r3))
    }

    assertResult( MSN(List(root, c1, c2, l1)) ) {
      solution(List(l1, c1))
    }

    assertResult( MSN(List(root, c1, c2, l1, l2)) ) {
      solution(List(l1, l2))
    }

    assertResult( MSN(List(root, c1, c2, r1, r2)) ) {
      solution(List(c2, r2))
    }

    assertResult( LCA(List(root, c1, c2)) ) {
      solution(List(l1, r1))
    }

    assertResult( MSN(List(root, c1, c2)) ) {
      solution(List(c2, c2))
    }

    // Multiple nodes:

    // left, common, right -> LCA
    assertResult( LCA(List(root, c1, c2)) ) {
      solution(List(l1, c2, r2))
    }

    // all on the same line
    assertResult( MSN(List(root, c1, c2, l1, l2)) ) {
      solution(List(c1, l2, l1, c2))
    }

    // some from one branch and some from another
    assertResult( LCA(List(root, c1, c2)) ) {
      solution(List(c1, l2, r3, c2, r1))
    }
  }

}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: ../../main/scala/metagenomica/bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: ../../main/scala/metagenomica/bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: ../../main/scala/metagenomica/bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: ../../main/scala/metagenomica/bundles/blast.scala.md
[main/scala/metagenomica/bundles/blast16s.scala]: ../../main/scala/metagenomica/bundles/blast16s.scala.md
[main/scala/metagenomica/bundles/flash.scala]: ../../main/scala/metagenomica/bundles/flash.scala.md
[main/scala/metagenomica/bundles/gis.scala]: ../../main/scala/metagenomica/bundles/gis.scala.md
[main/scala/metagenomica/data.scala]: ../../main/scala/metagenomica/data.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: ../../main/scala/metagenomica/dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../../main/scala/metagenomica/loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../../main/scala/metagenomica/loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../../main/scala/metagenomica/loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../../main/scala/metagenomica/loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../../main/scala/metagenomica/loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../../main/scala/metagenomica/loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../../main/scala/metagenomica/package.scala.md
[main/scala/metagenomica/parameters.scala]: ../../main/scala/metagenomica/parameters.scala.md
[test/scala/bundles.scala]: bundles.scala.md
[test/scala/lca.scala]: lca.scala.md
[test/scala/metagenomica/pipeline.scala]: metagenomica/pipeline.scala.md