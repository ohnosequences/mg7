package ohnosequences.mg7.bio4j


/* This piece of code is just a generic implementation of the advanced LCA algorithm */
case object taxonomyTree {

  /* This is just an abstract representation of the taxonomy tree nodes */
  trait AnyTaxonNode extends Any {

    def id: String
    def name: String
    def rank: String

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

    def rankNumber: Int = this.rank.trim.toLowerCase match {
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
