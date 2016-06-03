package ohnosequences.mg7.tests

import ohnosequences.mg7.bio4j.taxonomyTree._


case object taxonomy {

  sealed abstract class AnyNode(val parent: Option[AnyNode]) extends AnyTaxonNode {

    val id = this.toString
    val name = id
    val rankName = ""
  }

  abstract class Node(p: AnyNode) extends AnyNode(Some(p))

  case object root extends AnyNode(None)
  // common part
  case object c1 extends Node(root)
  case object c2 extends Node(c1)
  // left branch
  case object l1 extends Node(c2)
  case object l2 extends Node(l1)
  // right branch
  case object r1 extends Node(c2)
  case object r2 extends Node(r1)
  case object r3 extends Node(r2)

  val common = Seq(root, c1, c2)

  val allNodes: Set[AnyNode] = Set(root, c1, c2, l1, l2, r1, r2, r3)

  val id2node: Map[String, AnyNode] = allNodes.map{ n => (n.id -> n) }.toMap
}
