package ohnosequences.metapasta.reporting.spreadsheeet

import ohnosequences.nisperon.{longMonoid, doubleMonoid, intMonoid, Monoid}
import scala.collection.mutable


trait Context {
  def get[A <: AnyAttribute](attribute: A, index: Int): attribute.Type
  def getTotal[A <: AnyAttribute](attribute: A): attribute.Type
  def set[A <: AnyAttribute](attribute: A, index: Int)(value: attribute.Type)
}

class ListContext(val attributes: List[AnyAttribute]) extends Context {

  val map = new mutable.HashMap[(String, Int), Any]()

  val totals = new mutable.HashMap[String, Any]()

  for (attribute <- attributes) {
    totals.put(attribute.name, attribute.monoid.unit)
  }

  override def set[A <: AnyAttribute](attribute: A, index: Int)(value: attribute.Type): Unit = {
    val cur = totals.getOrElse(attribute.name, attribute.monoid.unit).asInstanceOf[attribute.Type]
    totals.put(attribute.name, attribute.monoid.mult(cur, value))
    map.put((attribute.name, index), value)
  }

  override def getTotal[A <: AnyAttribute](attribute: A): attribute.Type = {
    totals.getOrElse(attribute.name, attribute.monoid.unit).asInstanceOf[attribute.Type]
  }

  override def get[A <: AnyAttribute](attribute: A, index: Int): attribute.Type = {
    map.getOrElse((attribute.name, index), attribute.monoid.unit).asInstanceOf[attribute.Type]
  }
}

case class StringConstantMonoid(c: String) extends Monoid[String] {
  override def mult(x: String, y: String): String = c

  override def unit: String = c
}

trait AnyAttribute {
  type Type

  val monoid: Monoid[Type]
  val name: String
  type Item

  val hidden: Boolean
  def execute(item: Item, index: Int, context: Context): Type

  def printTotal(total: Type): String = total.toString

}

object AnyAttribute {
  type For[T] = AnyAttribute { type Item = T }

}

abstract class LongAttribute[I](val name: String, val monoid: Monoid[Long], val hidden: Boolean = false) extends AnyAttribute {
  override type Type = Long
  override type Item = I
}

abstract class DoubleAttribute[I](val name: String, val monoid: Monoid[Double], val hidden: Boolean = false) extends AnyAttribute {
  override type Type = Double
  override type Item = I
}

abstract class StringAttribute[I](val name: String, val monoid: Monoid[String], val hidden: Boolean = false) extends AnyAttribute {
  override type Type = String
  override type Item = I
}



case class Freq[I](a: LongAttribute[I]) extends DoubleAttribute[I](a.name + ".freq", doubleMonoid) {
  override def execute(item: Item, index: Int, context: Context) = {

    if(context.getTotal(a) == 0 ) {
      if (context.get(a, index) == 0) {
        0D
      } else {
        println("error: " + a.name + " of " + item + " < " + " total!")
        0D
      }
    } else {
      (context.get(a, index).toDouble / context.getTotal(a)) * 100
    }
  }
}

case class  Normalize[I](a: LongAttribute[I], d: LongAttribute[I], oname: String, percentage: Boolean, override val hidden: Boolean = false) extends DoubleAttribute[I](oname, doubleMonoid, hidden) {
  override def execute(item: Item, index: Int, context: Context) = {
    if(context.getTotal(d) == 0 ) {
      if (context.get(a, index) == 0) {
        0D
      } else {
        println("error " + d.name +".total == 0")
        0D
      }
    } else {
      val r = (context.get(a, index).toDouble / context.getTotal(d))
      if (percentage) r * 100 else r
    }
  }
}

case class Sum[I](a: List[LongAttribute[I]], override val hidden: Boolean = false) extends LongAttribute[I](a.map(_.name).reduce { _ + "+" + _}, longMonoid, hidden) {
  override def execute(item: Item, index: Int, context: Context) = {
    a.map {context.get(_, index)}.reduce{_ + _}
  }
}



case class Average[I](a: List[DoubleAttribute[I]], override val hidden: Boolean = false, override val monoid: Monoid[Double] = doubleMonoid) extends DoubleAttribute[I]("mean(" + a.map(_.name).reduce { _ + "," + _} + ")", monoid, hidden) {
  override def execute(item: Item, index: Int, context: Context) = {
    (a.map {
      context.get(_, index)
    }.reduce {
      _ + _
    } + 0.0) / a.size
  }
}




class Executor[Item](attributes: List[AnyAttribute.For[Item]], items: Iterable[Item]) {
  def execute() {
    val context = new ListContext(attributes)

    for (attribute <- attributes) {
      var index = 0
      for (item <- items) {
        val res = attribute.execute(item, index, context)
        context.set(attribute, index)(res)
        println(attribute.name + "[" + index + "] = " + res)
        index += 1
      }
      println(attribute.name + "[*] = " + context.getTotal(attribute))
    }
  }
}

class CSVExecutor[Item](
                         attributes: List[AnyAttribute.For[Item]],
                         items: Iterable[Item],
                         val separator: String = ",",
                         val headers: Boolean = true
                         ) {
 def quote(s: String): String = {
   if(s.contains(" ") || s.contains("\t")) {
     '"' + s + '"'
   } else {
     s
   }
 }

  def execute(): String  = {
    val context = new ListContext(attributes)



   // println("executing")
    for (attribute <- attributes) {
      var index = 0
      for (item <- items) {
        val res = attribute.execute(item, index, context)
        context.set(attribute, index)(res)
       // println(attribute.name + "[" + index + "] = " + res)
        index += 1
      }
     // println(attribute.name + "[*] = " + context.getTotal(attribute))
    }

    val lines = new mutable.StringBuilder()

    var index = 0

    val line = new mutable.StringBuilder()

    if(headers) {


      for (attribute <- attributes if !attribute.hidden) {
        if (!line.isEmpty) {
          line.append(separator)
        }
        line.append(quote(attribute.name))
      }

      lines.append(line.toString() + System.lineSeparator())
      line.clear()
    }
    for (item <- items) {

      for (attribute <- attributes if !attribute.hidden) {
        if(!line.isEmpty) {
          line.append(separator)
        }
        line.append(quote(context.get(attribute, index).toString))
      }
      lines.append(line.toString() + System.lineSeparator())
      line.clear()
      // println(attribute.name + "[" + index + "] = " + res)
      index += 1
    }
    for (attribute <- attributes if !attribute.hidden) {
      if(!line.isEmpty) {
        line.append(separator)
      }
      val total = attribute.printTotal(context.getTotal(attribute))
      line.append(quote(total))
    }
    lines.append(line.toString() + System.lineSeparator())
    lines.toString()
  }
}


