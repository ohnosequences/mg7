package ohnosequences.mg7.bio4j

/* This piece of code is just a generic implementation of the advanced LCA algorithm */
case object taxonomyTree {

  /* This is just an abstract representation of the taxonomy tree nodes */
  trait AnyTaxonNode extends Any {

    def id: String
    def name: String
    def rankName: String

    // root doesn't have parent
    def parent: Option[AnyTaxonNode]

    /* The sequence of ancestors from the root to this node (it is never empty) */
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

  /* Path in the tree stores a sequence of nodes from bottom to top */
  type Path = Seq[AnyTaxonNode]

  /* Find the "solution" of the algorithm for a set of nodes */
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
}
