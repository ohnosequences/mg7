
```scala
package ohnosequences.mg7.bio4j
```

This piece of code is just a generic implementation of the advanced LCA algorithm

```scala
case object taxonomyTree {
```

This is just an abstract representation of the taxonomy tree nodes

```scala
  trait AnyTaxonNode extends Any {

    def id: String
    // TODO: any other useful information (i.e. name)

    // root doesn't have parent
    def parent: Option[AnyTaxonNode]
  }
```

Path in the tree stores a sequence of nodes from top to bottom

```scala
  type Path = Seq[AnyTaxonNode]
```

Just goes up until the root. The resulting path starts from the root

```scala
  @scala.annotation.tailrec
  def pathToTheRoot(node: AnyTaxonNode, acc: Path): Path =
    node.parent match {
      case None => node +: acc
      case Some(p) => pathToTheRoot(p, node +: acc)
    }
```

This takes a path and some node and tries to go up from the node
until it encounters intersection with the path (plus returns the accumulated branch)


```scala
  @scala.annotation.tailrec
  def intersect(path: Path)(node: AnyTaxonNode, acc: Path): (Path, Path) = {

    val (intersection, rest) = path.span(_ != node)

    // if node is not on the path, we continue searching
    if (rest.isEmpty) node.parent match {
      case Some(p) => intersect(path)(p, node +: acc)
      // we came to the root, but the intersection is empty, then the path was empty
      case None => (intersection, node +: acc)
    }
    // if node lies on the path, we found the intersection
    else (intersection :+ node, acc)
  }
```

Solution is either
- the _most specific node (MSN)_ if they are all on the same branch
- or fair _lowest common ancestor (LCA)_


```scala
  // TODO: rename it something more sensible
  sealed trait Solution {
    val path: Path
    lazy val node: Option[AnyTaxonNode] = path.lastOption
  }
  case class MSN(path: Path) extends Solution
  case class LCA(path: Path) extends Solution
```

Find the "solution" of the algorithmfor a set of nodes

```scala
  def solution(nodes: List[AnyTaxonNode]): Solution = {

    def step(candidate: Solution, next: AnyTaxonNode): Solution = {
      candidate match {
        // here we assume that nodes are on the same line
        case MSN(path) =>
          val (intersection, branch) = intersect(path)(next, Seq())

          // if the path from the node leads to the candidate, it's the new MSN
          if (branch.headOption.flatMap(_.parent) == candidate.node) MSN(intersection ++ branch)
          // if node just lies on the path, we return the same left reference
          else if (branch.isEmpty) candidate
          // if we found intersection and it was two different branches, it's the LCA
          else LCA(intersection)

        // here we're searching only for intersection
        case LCA(path) => {
          val (intersection, _) = intersect(path)(next, Seq())
          LCA(intersection)
        }
      }
    }

    // just folding over the nodes list with the inital assumtion that they are on the same branch
    nodes.foldLeft[Solution]( MSN(Seq()) ) { step(_, _) }
  }

}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: ../bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: ../bundles/blast.scala.md
[main/scala/metagenomica/bundles/blast16s.scala]: ../bundles/blast16s.scala.md
[main/scala/metagenomica/bundles/flash.scala]: ../bundles/flash.scala.md
[main/scala/metagenomica/bundles/gis.scala]: ../bundles/gis.scala.md
[main/scala/metagenomica/data.scala]: ../data.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: ../dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../package.scala.md
[main/scala/metagenomica/parameters.scala]: ../parameters.scala.md
[test/scala/bundles.scala]: ../../../../test/scala/bundles.scala.md
[test/scala/lca.scala]: ../../../../test/scala/lca.scala.md
[test/scala/metagenomica/pipeline.scala]: ../../../../test/scala/metagenomica/pipeline.scala.md