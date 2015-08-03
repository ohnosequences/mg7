package ohnosequences.nipsperon

import org.junit.Test
import org.junit.Assert._
import ohnosequences.nisperon.{MapSerializer, JsonSerializer}

class SerializerTests {

  @Test
  def mapSerializerTest() {
    val kSerializer = new JsonSerializer[(String, Int)]()
    val vSerializer = new JsonSerializer[List[String]]()

    val map = Map (
      ("one", 1) -> List("test test test"),
      ("two", 2) -> List("test\ntest"),
      ("tree", 3) -> List()
    )

    val mapSerializer = new MapSerializer(kSerializer, vSerializer)
    val json = mapSerializer.toString(map)
    println(json)
    assertEquals(map, mapSerializer.fromString(json))
  }

}
