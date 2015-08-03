package ohnosequences.metapasta.automatic

import ohnosequences.metapasta.automatic.Assigner._
import ohnosequences.metapasta.automatic.Generators._
import ohnosequences.metapasta.reporting.spreadsheeet._
import ohnosequences.nisperon.{maxDoubleMonoid, doubleMonoid, longMonoid}
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}


object Spreadsheeet extends Properties("Spreadsheeet") {

  property("counter test") = forAll (Gen.listOf(Gen.choose(1, 100))) { list =>
    type Item = Int

    object ascendingCounter extends LongAttribute[Item]("ascendingCounter", longMonoid, hidden = true) {
      override def execute(item: Item, index: Int, context: Context): Long = {
        index
      }
    }

    object descendingCounter extends LongAttribute[Item]("descendingCounter", longMonoid, hidden = true) {
      override def execute(item: Item, index: Int, context: Context): Long = {
        (list.size -1) - index
      }
    }

    object constAttribute extends LongAttribute[Item]("coefficient", longMonoid, hidden = true) {
      override def execute(item: Item, index: Int, context: Context): Long = {
        if (index == 0) 1 else 0
      }
    }

    object n1 extends Normalize[Item](ascendingCounter, constAttribute, "n1", percentage = false, hidden = true)

    object n2 extends Normalize[Item](descendingCounter, constAttribute, "n2", percentage = false, hidden = true)

    object avg extends Average(List(n1, n2), monoid = maxDoubleMonoid)



    val result = new CSVExecutor[Item](List[AnyAttribute.For[Item]](ascendingCounter, descendingCounter, constAttribute, n1, n2, avg), list, headers = false).execute()

    (list.isEmpty || {result.lines.forall { s => s.toDouble.equals((list.size - 1.0) / 2)}}) &&
      (!list.isEmpty || {result.lines.forall { s => s.toDouble.equals(0D)}})
  }

}
