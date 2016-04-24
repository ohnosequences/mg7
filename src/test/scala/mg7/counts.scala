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

  val ids: List[TaxID] =
    realCounts.flatMap { case (node, count) =>
      List.fill(count)(node.id)
    }.toList

  val direct = directCounts(ids)

  val accumulated = accumulatedCounts(
    direct,
    id2node(_).lineage.map(_.id)
  )
}


class CountsTest extends org.scalatest.FunSuite {
  import countsCtx._

  test("direct counts") {

    assertResult( direct ) {
      realCounts.map { case (n, i) => n.id -> i }
    }
  }

  test("accumulated counts") {

    // accumulated.foreach{ case (id, i) => info(s"${id}\t-> ${i}") }

    assertResult( List() ) {
      accumulated.toList.diff(
        Map(
            root.id -> 14,
              c1.id -> 14,
              c2.id -> 14,
          l1.id -> 2, r1.id -> 8,
          l2.id -> 2, r2.id -> 5,
                      r3.id -> 5
        ).toList
      )
    }
  }

}
