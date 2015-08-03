package ohnosequences.metapasta.reporting.spreadsheeet

import org.junit.Test
import org.junit.Assert._
import ohnosequences.nisperon.longMonoid

class SpreadsheetTests {
  object Id extends StringAttribute[(Int, String)]("id", new StringConstantMonoid("total")) {
    override type Item = (Int, String)
    override  def execute(item: Item, index: Int, context: Context) = {
      item._1.toString
    }
  }

  object Name extends StringAttribute[(Int, String)]("name", new StringConstantMonoid("")) {
    override type Item = (Int, String)
    override  def execute(item: Item, index: Int, context: Context) = {
      item._2
    }
  }

  object Counter extends LongAttribute[(Int, String)]("counter", longMonoid) {
    override def execute(item: Item, index: Int, context: Context) = {
      context.get(Counter, index -1) + 1
    }
  }

  @Test
  def csvTests {
    val items = List (
      (1, "one"), (2, "two"), (3, "three"), (123, "one-two-three")
    )

    val etalonCsv = """id,name,counter,counter+counter,counter.freq
                |1,one,1,2,10.0
                |2,two,2,4,20.0
                |3,three,3,6,30.0
                |123,one-two-three,4,8,40.0
                |total,,10,20,100.0"""

    val attributes = List[AnyAttribute.For[(Int, String)]](Id, Name, Counter, Sum(List(Counter, Counter)), Freq(Counter))
    val executor = new CSVExecutor(attributes, items)
    val csv = executor.execute()

    assertEquals(true, csv.contains("123,one-two-three,4,8,40.0") )

  }



}
