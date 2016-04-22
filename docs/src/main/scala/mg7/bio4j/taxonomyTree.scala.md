
```scala
package ohnosequences.mg7.bio4j
```

This piece of code is just a generic implementation of the advanced LCA algorithm

```scala
case object taxonomyTree {
```

This is just an abstract representation of the taxonomy tree nodes

```scala
  trait AnyTaxonNode {

    def id: String
    def name: String
    def rank: String

    // root doesn't have parent
    def parent: Option[AnyTaxonNode]
```

The sequence of ancestors from the root to this node (it is never empty)

```scala
    lazy val lineage: Path = {
      @scala.annotation.tailrec
      def ancestors_rec(n: AnyTaxonNode, acc: Path): Path = n.parent match {
        case None => n +: acc
        case Some(p) => ancestors_rec(p, n +: acc)
      }

      ancestors_rec(this, Seq())
    }
  }
```

Path in the tree stores a sequence of nodes from bottom to top

```scala
  type Path = Seq[AnyTaxonNode]
```

Find the "solution" of the algorithm for a set of nodes

```scala
  def lowestCommonAncestor(default: AnyTaxonNode, nodes: Seq[AnyTaxonNode]): AnyTaxonNode = {

    def longestCommonPrefix(path1: Path, path2: Path): Path = {
      (path1 zip path2)
        .takeWhile { case (n1, n2) =>
          n1.id == n2.id
        }.map { _._1 }
    }

    nodes
      .map(_.lineage)
      .reduceOption(longestCommonPrefix)
      .flatMap(_.lastOption)
      .getOrElse(default)
  }

}

```




[test/scala/mg7/pipeline.scala]: ../../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/lca.scala]: ../../../../test/scala/mg7/lca.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: ../dataflows/noFlash.scala.md
[main/scala/mg7/dataflows/full.scala]: ../dataflows/full.scala.md
[main/scala/mg7/package.scala]: ../package.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: titanTaxonomyTree.scala.md
[main/scala/mg7/bio4j/bundle.scala]: bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: taxonomyTree.scala.md
[main/scala/mg7/dataflow.scala]: ../dataflow.scala.md
[main/scala/mg7/csv.scala]: ../csv.scala.md
[main/scala/mg7/parameters.scala]: ../parameters.scala.md
[main/scala/mg7/data.scala]: ../data.scala.md
[main/scala/mg7/loquats/7.stats.scala]: ../loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: ../loquats/8.summary.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../loquats/6.count.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../loquats/4.assign.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../loquats/5.merge.scala.md