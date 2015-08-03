package ohnosequences.metapasta.automatic

import org.scalacheck.{Gen, Arbitrary, Properties}
import org.scalacheck.Prop._
import ohnosequences.metapasta.{Tree, TreeUtils, MapTree}




object TreeTests extends Properties("Tree") {
  import Generators._

  property("root") = forAll (boundedTree(stringLabeling, 1000)) { case (tree, size) =>
    tree.isNode(tree.root)

  }

  property("is node") = forAll (randomNode(boundedTree(stringLabeling, 1000), stringLabeling)) { case (tree, node) =>
  // println(">> tree=" + tree + " size=" + node)
    tree.isNode(node)
  }

  property("parent") = forAll (randomNode(boundedTree(stringLabeling, 1000), stringLabeling)) {  case (tree, node) =>
    if (tree.root.equals(node)) {
      tree.getParent(node).isEmpty
    } else {
      tree.getParent(node).isDefined
    }
  }

  property("lca idem") = forAll (randomNode(boundedTree(stringLabeling, 1000), stringLabeling)) { case (tree, node) =>
    val lca = TreeUtils.lca(tree, node, node)
    node.equals(lca)
  }

  property("lca shuffle") = forAll (randomNodeList(boundedTree(stringLabeling, 10000), stringLabeling)) { case (tree, nodes) =>
    val lca1 = TreeUtils.lca(tree, nodes.toSet)
    val lca2 = TreeUtils.lca(tree, random.shuffle(nodes.toSet))
    lca1.equals(lca2)
  }

  property("lca root") = forAll (randomNodeList(boundedTree(stringLabeling, 10000), stringLabeling)) { case (tree, nodes) =>
    val lca = TreeUtils.lca(tree, (tree.root :: nodes).toSet)
    tree.root.equals(lca)
  }

  property("lca order") = forAll (randomNodePair(boundedTree(stringLabeling, 10000), stringLabeling)) { case (tree, node1, node2) =>
    val lca = TreeUtils.lca(tree, node1, node2)
    (!(lca.equals(node1)) || TreeUtils.getLineage(tree, node2).contains(node1)) &&
      (!(lca.equals(node2)) || TreeUtils.getLineage(tree, node1).contains(node2))
  }


  property("lca associativity 1") = forAll (randomNodeTriple(boundedTree(stringLabeling, 10000), stringLabeling)) { case (tree, node1, node2, node3) =>
    val lca1 = TreeUtils.lca(tree, TreeUtils.lca(tree, node1, node2), node3)
    val lca2 = TreeUtils.lca(tree, node1, TreeUtils.lca(tree, node2, node3))
    lca2.equals(lca1)
  }

  property("lca associativity 2") = forAll (randomNodeSets(boundedTree(stringLabeling, 10000), stringLabeling, 100)) { case (tree, sets) =>
    val allNodes = sets.flatMap { set => set}.toSet
    val lca1 = TreeUtils.lca(tree, allNodes)
    val lca2 = TreeUtils.lca(tree, sets.map { set => TreeUtils.lca(tree, set)}.toSet)
    lca2.equals(lca1)
  }

  property("in line") = forAll (randomNodeSet(boundedTree(stringLabeling, 10000), stringLabeling)) { case (tree, nodes) =>
    TreeUtils.isInLine(tree, nodes) match {
      case None => {
        nodes.forall { node =>
          !TreeUtils.getLineage(tree, node).take(nodes.size).toSet.equals(nodes)
        }
      }
      case Some(node) => {
        //println("are in line, tree: " + tree.toString + " nodes:" + nodes)
        TreeUtils.getLineage(tree, node).takeRight(nodes.size).toSet.equals(nodes)
      }
    }
  }

  property("sum") = forAll (Generators.partitions(10)) { l: List[Int] =>
    l.sum == 10
  }


}
