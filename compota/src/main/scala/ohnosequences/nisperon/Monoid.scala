package ohnosequences.nisperon
import collection.mutable

trait Monoid[M] {

  def unit: M
  def mult(x: M, y: M): M
}

class ProductMonoid[X, Y](xMonoid: Monoid[X], yMonoid: Monoid[Y]) extends Monoid[(X, Y)] {
  def unit: (X, Y) = (xMonoid.unit, yMonoid.unit)

  def mult(x: (X, Y), y: (X, Y)): (X, Y) = (xMonoid.mult(x._1, y._1), yMonoid.mult(x._2, y._2))
}

class ListMonoid[T] extends Monoid[List[T]] {
  def unit: List[T] = List[T]()
  def mult(x: List[T], y: List[T]): List[T] = x ++ y
}

object intMonoid extends Monoid[Int] {
  def unit: Int = 0
  def mult(x: Int, y: Int): Int = x + y
}

object longMonoid extends Monoid[Long] {
  def unit: Long = 0L
  def mult(x: Long, y: Long): Long = x + y
}

object maxLongMonoid extends Monoid[Long] {
  def unit: Long = 0L
  def mult(x: Long, y: Long): Long = math.max(x, y)
}

object doubleMonoid extends Monoid[Double] {
  def unit: Double = 0
  def mult(x: Double, y: Double): Double = x + y
}

object maxDoubleMonoid extends Monoid[Double] {
  def unit: Double = 0D
  def mult(x: Double, y: Double): Double = math.max(x, y)
}

object stringMonoid extends Monoid[String] {
  def unit = ""
  def mult(x: String, y: String): String = x + y
}



object unitMonoid extends Monoid[Unit] {
  def unit: Unit = ()
  def mult(x: Unit, y: Unit): Unit = ()
}

//todo fix it!
class MapMonoid[K, V](vMonoid: Monoid[V]) extends Monoid[Map[K, V]] {

  override def unit: Map[K, V] = Map[K, V]()

  override def mult(x: Map[K, V], y: Map[K, V]): Map[K, V] = {
    val res = new mutable.HashMap[K, V]()
    res ++= x
    for ((k, v) <- y) {
      x.get(k) match {
        case None => res.put(k, v)
        case Some(v0) => res.put(k, vMonoid.mult(v0, v))
      }
    }
    res.toMap
  }


}





