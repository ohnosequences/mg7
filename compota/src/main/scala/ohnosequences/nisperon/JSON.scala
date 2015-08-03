package ohnosequences.nisperon

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{writePretty}

object JSON {
  implicit val formats = DefaultFormats

  def extract[T](json: String)(implicit mf: scala.reflect.Manifest[T]): T = {
    parse(json).extract[T]
  }

  def toJSON(a: AnyRef): String = {
    writePretty(a)
  }

}
