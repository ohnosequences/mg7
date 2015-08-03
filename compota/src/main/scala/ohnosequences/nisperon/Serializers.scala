package ohnosequences.nisperon

import scala.util.parsing.json.JSONObject

trait Serializer[T] {
  def fromString(s: String): T

  def toString(t: T): String
}


class JsonSerializer[T](implicit mf: scala.reflect.Manifest[T]) extends Serializer[T] {

  def fromString(s: String): T = {
    JSON.extract[T](s)
  }

  def toString(t: T): String = {
    JSON.toJSON(t.asInstanceOf[AnyRef])
  }

}

object unitSerializer extends Serializer[Unit] {
  def fromString(s: String) = ()
  def toString(t: Unit) = ""
}

object intSerializer extends Serializer[Int] {

  def fromString(s: String): Int = s.toInt

  def toString(t: Int): String = t.toString
}

object stringSerializer extends Serializer[String] {
  def fromString(s: String): String = s

  def toString(t: String): String = t
}

class MapSerializer[K, V](kSerializer: Serializer[K], vSerializer: Serializer[V]) extends Serializer[Map[K, V]] {
  override def toString(t: Map[K, V]): String = {
    val rawMap: Map[String, String] = t.map { case (key, value) =>
      (kSerializer.toString(key), vSerializer.toString(value))
    }
    JSONObject(rawMap).toString()
  }


  override def fromString(s: String): Map[K, V] = {
    scala.util.parsing.json.JSON.parseFull(s) match {
      case Some(map: Map[String, String]) => {
        map.map {
          case (key, value) =>
            (kSerializer.fromString(key), vSerializer.fromString(value))
        }
      }
    }
  }
}
