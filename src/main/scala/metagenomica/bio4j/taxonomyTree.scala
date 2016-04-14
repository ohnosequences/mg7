package ohnosequences.mg7.bio4j


/* This piece of code is just a generic implementation of the advanced LCA algorithm */
case object taxonomyTree {

  /* This is just an abstract representation of the taxonomy tree nodes */
  trait AnyTaxonNode {

    def id: String
    def name: String
    def rank: String

    // root doesn't have parent
    def parent: Option[AnyTaxonNode]

    /* The sequence of ancestors from the root to this node (it is never empty) */
    lazy val lineage: Path = {
      @scala.annotation.tailrec
      def ancestors_rec(n: AnyTaxonNode, acc: Path): Path = n.parent match {
        case None => n +: acc
        case Some(p) => ancestors_rec(p, n +: acc)
      }

      ancestors_rec(this, Seq())
    }
  }

  /* Path in the tree stores a sequence of nodes from bottom to top */
  type Path = Seq[AnyTaxonNode]

  private def longestCommonPrefix[T](seq1: Seq[T], seq2: Seq[T]): Seq[T] = {
    (seq1 zip seq2)
      .takeWhile { case (s1, s2) =>
        s1 == s2
      }.map {
        _._1
      }
  }

  /* Find the "solution" of the algorithm for a set of nodes */
  def lowestCommonAncestor(nodes: Seq[AnyTaxonNode]): Option[AnyTaxonNode] = {
    if (nodes.isEmpty) None
    else Some(
      nodes.reduce { (node1, node2) =>
        longestCommonPrefix(node1.lineage, node2.lineage).last
      }
    )
  }

}
