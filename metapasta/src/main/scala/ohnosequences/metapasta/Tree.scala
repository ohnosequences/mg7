package ohnosequences.metapasta

import scala.annotation.tailrec


object Tree {
  def relabel[T, S](tree: Tree[T], f: T => S, g: S => T): Tree[S] = new Tree[S] {

    override def getParent(node: S): Option[S] = tree.getParent(g(node)).map(f)

    override def isNode(node: S): Boolean = tree.isNode(g(node))

    override val root: S = f(tree.root)
  }
}

/**
 * a layer for bio4j taxonomy tree
 */
trait Tree[N] {
  def getParent(node: N): Option[N]
  val root: N
  def isNode(node: N): Boolean
}

class MapTree[N](val map: Map[N, N], val root: N) extends Tree[N] {

  override def getParent(node: N): Option[N] = map.get(node)

  override def isNode(node: N): Boolean = {
    root.equals(node) || map.contains(node)
  }

  override def toString: String = map.toString() + "|" + root
}

case class Taxon(taxId: String)

class Bio4JTaxonomyTree(nodeRetriever: NodeRetriever) extends Tree[Taxon] {
  override def getParent(taxon: Taxon): Option[Taxon] = {
    val node = nodeRetriever.nodeRetriever.getNCBITaxonByTaxId(taxon.taxId)
    val parent = node.getParent()
    if (parent == null) {
      None
    } else {
      Some(Taxon(parent.getTaxId()))
    }
  }

  override val root: Taxon = Taxon("1")

  override def isNode(node0: Taxon): Boolean = {
    val node = nodeRetriever.nodeRetriever.getNCBITaxonByTaxId(node0.taxId)
    if (node == null) {
      false
    } else {
      val taxId = node.getTaxId()
      if (taxId == null || taxId.isEmpty) {
        false
      } else {
        true
      }
    }
  }
}


object TreeUtils {


  @tailrec
  def getLineageAux[N](tree: Tree[N], node: N, acc: List[N] = List[N]()): List[N] = {
    tree.getParent(node) match {
      case None => node :: acc
      case Some(p) => getLineageAux(tree, p, node :: acc)
    }
  }

  def getLineage[N](tree: Tree[N], node: N): List[N] = getLineageAux(tree, node)

  @tailrec
  def getLineageExclusiveAux[N](tree: Tree[N], node: N, acc: List[N] = List[N]()): List[N] = {
    tree.getParent(node) match {
      case None => acc
      case Some(p) => getLineageExclusiveAux(tree, p, p :: acc)
    }
  }

  def getLineageExclusive[N](tree: Tree[N], node: N): List[N] = getLineageExclusiveAux(tree, node)



  /** Tests if the set of nodes form a line in the tree
    *  @return ``Some(node)` if there they are, node is most specific node `None` otherwise.
    */
  def isInLine[N](tree: Tree[N], nodes: Set[N]): Option[N] = {
    if (nodes.isEmpty) {
      None
    } else {
      var maxLineageSize = 0
      var maxLineage = List[N]()

      for (node <- nodes) {
        val c = getLineage(tree, node)
        if (c.size > maxLineageSize) {
          maxLineageSize = c.size
          maxLineage = c
        }
      }

      //first taxIds.size elements should be taxIds
      val maxLineageTail = maxLineage.takeRight(nodes.size)
      if (maxLineageTail.size.equals(nodes.size) && maxLineageTail.forall(nodes.contains)) {
       // println("maxlin: " + maxLineageTail + "nodes: " + nodes)
        maxLineage.lastOption
      } else {
        None
      }
    }
  }

//  todo: linear algorithm, with map and stacks
//  def isInLine[N](tree: Tree[N], nodes: Seq[N]): Option[N] = {
//    val distances = new mutable.HashMap[N, Int]()
//
//
//    nodes.find { node =>
//      var curent = node
//      val visited = new mutable.Stack[N]()
//
//      whi
//
//      true
//    }
//  }


  def lca[N](tree: Tree[N], n1: N, n2: N): N = {
    if (n1.equals(n2)) {
      n1
    } else {
      val lineage1 = getLineage(tree, n1)
      val lineage2 = getLineage(tree, n2)

      //should be not empty because both lineages contain root
      val coincidePrefix = lineage1.zip(lineage2).takeWhile{
        case (nn1, nn2) => nn1.equals(nn2)
      }

      coincidePrefix.last._1
    }
  }

  def lcaAux[N](tree: Tree[N], nodes: List[N]): N = nodes match {
    case Nil => tree.root
    case h :: t => t.foldLeft(h) {
      case (nn1, nn2) => val r = lca(tree, nn1, nn2); r
    }
  }

  def lca[N](tree: Tree[N], nodes: Set[N]): N = lcaAux(tree, nodes.toList)
}



