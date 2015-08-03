package ohnosequences.nisperon

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import ohnosequences.nisperon.queues.{ProductQueue, MonoidQueueAux}

case class Node[N](label: N)

case class Edge[E, N](label: E, source: Node[N], target: Node[N])


class Graph[N, E](val edges: List[Edge[E, N]]) {

  override def toString() = {
    "nodes: " + nodes + System.lineSeparator() + "edges: " + edges
  }

  val nodes: Set[Node[N]] = {
    edges.toSet.flatMap { edge: Edge[E, N] =>
      Set(edge.source, edge.target)
    }
  }

  def remove(edge: Edge[E, N]): Graph[N, E] = {
    new Graph(edges.filterNot(_.equals(edge)))
  }

  def out(node: Node[N]): Set[Edge[E, N]] = {
    edges.filter(_.source.equals(node)).toSet
  }

  def in(node: Node[N]): Set[Edge[E, N]] = {
    edges.filter(_.target.equals(node)).toSet
  }

  def sort: List[Node[N]] =  {
    var arcFree = new Graph(edges.filterNot { edge =>
      edge.source.equals(edge.target)
    })

    val result = ListBuffer[Node[N]]()
    var s: List[Node[N]] = nodes.toList.filter(arcFree.in(_).isEmpty)
    //println(s)
    while(!s.isEmpty) {
      val n = s.head
      s = s.tail
      result += n

     // println("analyzing node: " + n + " in: " + arcFree.out(n))
      arcFree.out(n).foreach { e =>
        arcFree = arcFree.remove(e)
        if(!arcFree.nodes.contains(e.target) || arcFree.in(e.target).isEmpty) {
          s = e.target :: s

        }
      }
    }

    if(!arcFree.edges.isEmpty) {
     // println("oioioi")
    }
    result.toList
  }

}


class NisperoGraph(nisperos: HashMap[String, NisperoAux]) {



  val queues = {
    val r = new HashMap[String, MonoidQueueAux]()

    nisperos.values.foreach { nispero =>
      r ++= ProductQueue.flatQueue(nispero.inputQueue).map{ q=>
        q.name -> q
      }
      r ++= ProductQueue.flatQueue(nispero.outputQueue).map{ q=>
        q.name -> q
      }
    }
    r
  }

  val edges = nisperos.values.toList.flatMap { nispero =>
    for {
      i <- ProductQueue.flatQueue(nispero.inputQueue)
      o <- ProductQueue.flatQueue(nispero.outputQueue)
    } yield Edge(
      label = nispero.nisperoConfiguration.name,
      source = Node(i.name),
      target = Node(o.name)
    )
  }

  val graph: Graph[String, String] = new Graph(edges)


  //return either not leafs queues all (to delete them
  def checkQueues(): Either[MonoidQueueAux, List[MonoidQueueAux]] = {
    val sorted = graph.sort
    println(sorted)


    val notLeafsQueues = sorted.filterNot(graph.out(_).isEmpty).map { node =>
      queues(node.label)
    }

    notLeafsQueues.find { queue =>
      !queue.isEmpty
    } match {
      case None => println("all queues are empty"); Right(notLeafsQueues)
      case Some(queue) => println("queue " + queue.name + " isn't empty"); Left(queue)
    }


  }

}


