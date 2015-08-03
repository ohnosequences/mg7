package ohnosequences.metapasta.tests

import org.junit.Test
import org.junit.Assert._
import ohnosequences.metapasta.{TreeUtils, MapTree}

class TreeTests {
  @Test
  def lineage() {
    val tree = new MapTree(Map(
      "2" -> "1",
      "3" -> "2",
      "4" -> "3",
      "4l" -> "3"
    ), "1")

    assertEquals(List("1", "2", "3", "4"), TreeUtils.getLineage(tree, "4"))
    assertEquals(List("1"), TreeUtils.getLineage(tree, "1"))
    assertEquals(List(), TreeUtils.getLineageExclusive(tree, "1"))
    assertEquals(List("1", "2", "3"), TreeUtils.getLineageExclusive(tree, "4l"))

  }

  @Test
  def isInLineTest() {
    val tree = new MapTree(Map(
      "2" -> "1",
      "3" -> "2",
      "4" -> "3",
      "4l" -> "3",
      "5" -> "4"
    ), "1")

    assertEquals(Some("4"), TreeUtils.isInLine(tree, Set("4")))
    assertEquals(None, TreeUtils.isInLine(tree, Set("4", "4l")))
    assertEquals(Some("4"), TreeUtils.isInLine(tree, Set("1", "2", "3", "4")))
    assertEquals(None, TreeUtils.isInLine(tree, Set("1", "3", "5")))
  }

  @Test
  def lca() {
    val tree = new MapTree(Map(
      "2" -> "1",
      "2l" -> "1",
      "3" -> "2",
      "4" -> "3",
      "4l" -> "3",
      "5" -> "4"
    ), "1")

    assertEquals("3", TreeUtils.lca(tree, "4", "4l"))
    assertEquals("3", TreeUtils.lca(tree, Set("4", "4l", "5")))
    assertEquals("1", TreeUtils.lca(tree, Set[String]()))
    assertEquals("2l", TreeUtils.lca(tree, Set("2l")))


  }

}
