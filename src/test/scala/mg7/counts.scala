package ohnosequences.mg7.tests

import ohnosequences.mg7._
import ohnosequences.mg7.bio4j.taxonomyTree._
import ohnosequences.mg7.loquats.countDataProcessing._

import ohnosequences.mg7.tests.taxonomy._


case object countsCtx {

  val realCounts = Map[AnyNode, Int](
    c2 -> 4,
    l2 -> 2,
    r1 -> 3,
    r3 -> 5
  )

  val ids: List[Taxa] =
    realCounts.flatMap { case (node, count) =>
      List.fill(count)(node.id)
    }.toList

  def getLineage(id: Taxa): Seq[Taxa] = id2node(id).lineage.map(_.id)

  val direct: Map[Taxa, (Int, Seq[Taxa])] = directCounts(ids, getLineage)

  val accumulated: Map[Taxa, (Int, Seq[Taxa])] = accumulatedCounts(direct, getLineage)
}


class CountsTest extends org.scalatest.FunSuite {
  import countsCtx._

  def assertMapsDiff[A, B](m1: Map[A, B], m2: Map[A, B]): Unit = {
    assertResult( List() ) {
      m1.toList diff m2.toList
    }
  }


  test("direct counts") {

    assertMapsDiff(
      realCounts.map { case (n, c) => n.id -> c },
      direct.map { case (id, (c, _)) => id -> c }
    )
  }

  test("accumulated counts") {

    // accumulated.foreach{ case (id, i) => info(s"${id}\t-> ${i}") }

    assertMapsDiff(
      accumulated.map { case (id, (c, _)) => id -> c },
      Map(
          root.id -> 14,
            c1.id -> 14,
            c2.id -> 14,
        l1.id -> 2, r1.id -> 8,
        l2.id -> 2, r2.id -> 5,
                    r3.id -> 5
      )
    )
  }

}
