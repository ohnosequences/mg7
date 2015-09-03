package ohnosequences.metagenomica.bio4j


case object taxonomyTree {
  trait TaxonNode {

    // val id: String
    // TODO: any other useful information (i.e. name)

    // root doesn't have parent
    val parent: Option[TaxonNode]
  }

  type Path = Seq[TaxonNode]

  /* Just goes up until the root. The resulting path start from the root */
  @scala.annotation.tailrec
  def pathToTheRoot(node: TaxonNode, acc: Path): Path =
    node.parent match {
      case None => node +: acc
      case Some(p) => pathToTheRoot(p, node +: acc)
    }


  /* This takes a path and some node and tries to go up from the node
     until it encounters intersection with the path (plus returns the accumulated branch)
  */
  @scala.annotation.tailrec
  def intersect(path: Path)(node: TaxonNode, acc: Path): (Path, Path) = {

    val (intersection, rest) = path.span(_ != node)

    // if node is not on the path, we continue searching
    if (rest.isEmpty) node.parent match {
      case Some(p) => intersect(path)(p, node +: acc)
      // we came to the root, but the intersection is empty, then path was empty
      case None => (intersection, node +: acc)
    }
    // if node lies on the path, we found the intersection
    else (intersection :+ node, acc)
  }


  sealed trait Solution {
    val path: Path
    lazy val node: Option[TaxonNode] = path.lastOption
  }
  case class MSN(path: Path) extends Solution
  case class LCA(path: Path) extends Solution

  /* Finds either LCA or the most specific for thos on the same branch */
  def solution(nodes: List[TaxonNode]): Solution = {

    def step(candidate: Solution)(node: TaxonNode, acc: Path): Solution = {
      candidate match {
        // here we assume that nodes are on the same line
        case MSN(path) =>
          val (intersection, branch) = intersect(path)(node, acc)

          if (branch.headOption.flatMap(_.parent) == candidate.node) MSN(intersection ++ branch)
          // if node just lies on the path, we return the same left reference
          else if (branch.isEmpty) candidate
          // if we found intersection and it was two different branches, it's the LCA
          else LCA(intersection)
          // }
        // here we're searching only for intersection
        case LCA(path) => {
          val (intersection, _) = intersect(path)(node, acc)
          LCA(intersection)
        }
      }
    }

    nodes.foldLeft[Solution]( MSN(Seq()) ) { (candidate, next) =>
      step(candidate)(next, Seq())
    }
  }


  ///////////////////////////////////////////////////////////////////////
  // import com.bio4j.model.ncbiTaxonomy._
  // import com.bio4j.titan.model.ncbiTaxonomy._
  import com.bio4j.titan.util.DefaultTitanGraph
  import com.thinkaurelius.titan.core._, schema._

  type TitanTaxon = com.bio4j.model.ncbiTaxonomy.vertices.NCBITaxon[
    DefaultTitanGraph,
    TitanVertex, VertexLabelMaker,
    TitanEdge, EdgeLabelMaker
  ]


  def toOption[T](optional: java.util.Optional[T]): Option[T] = {
    if (optional.isPresent) Some(optional.get) else None
  }

  case class TitanTaxonNode(titanTaxon: TitanTaxon) extends TaxonNode {

    lazy val id: String = titanTaxon.id()

    lazy val parent: Option[TitanTaxonNode] =
      toOption(titanTaxon.ncbiTaxonParent_inV) map { p =>
        TitanTaxonNode(p)
      }
  }
}
