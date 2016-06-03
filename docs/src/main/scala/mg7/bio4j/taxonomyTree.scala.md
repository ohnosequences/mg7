
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
    def name: String
    def rankName: String

    // root doesn't have parent
    def parent: Option[AnyTaxonNode]
```

The sequence of ancestors from the root to this node (it is never empty)

```scala
    def lineage: Path = {
      @scala.annotation.tailrec
      def ancestors_rec(n: AnyTaxonNode, acc: Path): Path = n.parent match {
        case None => n +: acc
        case Some(p) => ancestors_rec(p, n +: acc)
      }

      ancestors_rec(this, Seq())
    }

    def rankNumber: Int = this.rankName.trim.toLowerCase match {
      case "superkingdom"     => 1
      case "kingdom"          => 2
      case "superphylum"      => 3
      case "phylum"           => 4
      case "subphylum"        => 5
      case "class"            => 6
      case "subclass"         => 7
      case "order"            => 8
      case "suborder"         => 9
      case "family"           => 10
      case "subfamily"        => 11
      case "tribe"            => 12
      case "subtribe"         => 13
      case "genus"            => 14
      case "subgenus"         => 15
      case "species group"    => 16
      case "species subgroup" => 17
      case "species"          => 18
      case "subspecies"       => 19
      // "no rank"
      case _ => this.parent.map(_.rankNumber).getOrElse(0) + 1
    }

    def rank: String = s"${this.rankNumber}: ${this.rankName}"
  }
```

Path in the tree stores a sequence of nodes from bottom to top

```scala
  type Path = Seq[AnyTaxonNode]
```

Find the "solution" of the algorithm for a set of nodes

```scala
  def lowestCommonAncestor(nodes: Seq[AnyTaxonNode]): Option[AnyTaxonNode] = {

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
  }

  // // TODO: move it all to the Bio4j taxonomy dist
  // sealed trait AnyTaxonomicRank
  //
  // case object NoRank extends AnyTaxonomicRank
  // sealed class TaxonomicRank(toInt: Int)
  //
  // case object Superkingdom    extends TaxonomicRank(1)
  // case object Kingdom         extends TaxonomicRank(2)
  // case object Superphylum     extends TaxonomicRank(3)
  // case object Phylum          extends TaxonomicRank(4)
  // case object Subphylum       extends TaxonomicRank(5)
  // case object Class           extends TaxonomicRank(6)
  // case object Subclass        extends TaxonomicRank(7)
  // case object Order           extends TaxonomicRank(8)
  // case object Suborder        extends TaxonomicRank(9)
  // case object Family          extends TaxonomicRank(10)
  // case object Subfamily       extends TaxonomicRank(11)
  // case object Tribe           extends TaxonomicRank(12)
  // case object Subtribe        extends TaxonomicRank(13)
  // case object Genus           extends TaxonomicRank(14)
  // case object Subgenus        extends TaxonomicRank(15)
  // case object SpeciesGroup    extends TaxonomicRank(16)
  // case object SpeciesSubgroup extends TaxonomicRank(17)
  // case object Species         extends TaxonomicRank(18)
  // case object Subspecies      extends TaxonomicRank(19)

}

```




[main/scala/mg7/bio4j/bundle.scala]: bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: ../csv.scala.md
[main/scala/mg7/data.scala]: ../data.scala.md
[main/scala/mg7/dataflow.scala]: ../dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: ../dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: ../dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: ../loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: ../loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: ../package.scala.md
[main/scala/mg7/parameters.scala]: ../parameters.scala.md
[main/scala/mg7/referenceDB.scala]: ../referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/lca.scala]: ../../../../test/scala/mg7/lca.scala.md
[test/scala/mg7/pipeline.scala]: ../../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../../test/scala/mg7/taxonomy.scala.md