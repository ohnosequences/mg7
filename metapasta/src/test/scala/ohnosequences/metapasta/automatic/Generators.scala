package ohnosequences.metapasta.automatic

import ohnosequences.formats.{RawHeader, FASTQ}
import ohnosequences.metapasta._
import org.scalacheck.{Arbitrary, Gen}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random


object Generators {

  @tailrec
  def repeat[T](gen: Gen[T], attempt: Int = 10): Option[T] = {
    gen.sample match {
      case Some(s) => Some(s)
      case None => {
        if (attempt == 0) {
          None
        } else {
          repeat(gen, attempt - 1)
        }
      }
    }
  }

  @tailrec
  def genListAux[U, T](list: List[U], res: Gen[List[T]], gen: U => Gen[T]): Gen[List[T]] = list match {
    case Nil => res.map(_.reverse)
    case head :: tail => {
      val newRes = for {
        headN <- gen(head)
        resR <- res
      } yield headN :: resR
      genListAux(tail, newRes, gen)
    }
  }

  def genList[U, T](list: List[U], gen: U => Gen[T]): Gen[List[T]] = genListAux(list, Gen.const(List[T]()), gen)


  @tailrec
  def genMapAux[U, T](list: List[U], res: Gen[Map[U, T]], gen: U => Gen[T]): Gen[Map[U, T]] = list match {
    case Nil => res
    case head :: tail => {
      val newRes = for {
        headN <- gen(head)
        resR <- res
      } yield  resR + (head -> headN)

      genMapAux(tail, newRes, gen)
    }
  }

  def genMap[U, T](list: List[U], gen: U => Gen[T]): Gen[Map[U, T]] = genMapAux[U, T](list, Gen.const(Map[U, T]()), gen)

  def genPair[T, S](gen1: Gen[T], gen2: Gen[S]): Gen[(T, S)] = for {
    v1 <- gen1
    v2 <- gen2
  } yield (v1, v2)

  def genTriple[T, S, U](gen1: Gen[T], gen2: Gen[S], gen3: Gen[U]): Gen[(T, S, U)] = for {
    v1 <- gen1
    v2 <- gen2
    v3 <- gen3
  } yield (v1, v2, v3)


  val random = new Random()

  @tailrec
  def treeRawAux[T](size: Int, map: mutable.HashMap[T, T], labeling: Int => T): Gen[MapTree[T]] = {
    if (size == 1) {
      Gen.const(new MapTree[T](map.toMap, labeling(1)))
    } else {
      map.put(labeling(size), labeling(random.nextInt(size - 1) + 1))
      treeRawAux[T](size - 1, map, labeling)
    }
  }


  def sizedTree[T](labeling: Int => T, size: Int): Gen[Tree[T]] = {

    val r = treeRawAux(size, new mutable.HashMap[T, T](), labeling)
    ///println("generated tree " + r + )
    r
  }

  def intLabeling(i: Int) = i

  def stringLabeling(i: Int) = i.toString


  def boundedTree[T](labeling: Int => T, bound: Int): Gen[(Tree[T], Int)] = {
    Gen.choose(1, bound).flatMap { size => sizedTree(labeling, size).map { tree => (tree, size)}}
  }



  def partitions(size: Int): Gen[List[Int]] = {

    Gen.listOfN(size, Arbitrary.arbBool).map {
      cuts =>
        var res = List[Int]()
        var lastCut = 0
        var pos = 0

        for (cut <- cuts) {
          cut.arbitrary.map {
            cutVal =>
              if (cutVal) {
                res = (pos - lastCut) :: res
                lastCut = pos
              }
          }
          pos += 1
        }
        res = (pos - lastCut) :: res
        lastCut = pos
        res
    }
  }

  def randomNode[T](sizedTree: Gen[(Tree[T], Int)], labeling: Int => T): Gen[(Tree[T], T)] = sizedTree.flatMap{ case (tree, size) =>
    Gen.choose(1, size).map { num => (tree, labeling(num))}
  }

  def randomNodePair[T](sizedTree: Gen[(Tree[T], Int)], labeling: Int => T): Gen[(Tree[T], T, T)] = sizedTree.flatMap{ case (tree, size) =>
    genPair(Gen.choose(1, size), Gen.choose(1, size)).map { case (num1, num2) => (tree, labeling(num1), labeling(num2))}
  }

  def randomNodeTriple[T](sizedTree: Gen[(Tree[T], Int)], labeling: Int => T): Gen[(Tree[T], T, T, T)] = sizedTree.flatMap{ case (tree, size) =>
    genTriple(Gen.choose(1, size), Gen.choose(1, size), Gen.choose(1, size)).map { case (num1, num2, num3) => (tree, labeling(num1), labeling(num2), labeling(num3))}
  }

  def randomNodeSetAux[T](tree: Tree[T], size: Int, labeling: Int => T): Gen[Set[T]] = {
    Gen.choose(1, size).flatMap { n => Gen.listOfN(n, Gen.choose(1, size).map(labeling))}.map { list => list.toSet }
  }

  def randomNodeSet[T](sizedTree: Gen[(Tree[T], Int)], labeling: Int => T): Gen[(Tree[T], Set[T])] = sizedTree.flatMap{ case (tree, size) =>
    randomNodeSetAux(tree, size, labeling).map { set => (tree, set)}
  }

  def randomNodeSets[T](sizedTree: Gen[(Tree[T], Int)], labeling: Int => T, setsBount: Int): Gen[(Tree[T], List[Set[T]])] = sizedTree.flatMap{ case (tree, size) =>
    Gen.choose(1, setsBount).flatMap{ n => Gen.listOfN(n, randomNodeSetAux(tree, size, labeling))}.map { sets => (tree, sets)}
  }

  def randomNodeList[T](sizedTree: Gen[(Tree[T], Int)], labeling: Int => T): Gen[(Tree[T], List[T])] = sizedTree.flatMap { case (tree, size) =>
    Gen.choose(1, size).flatMap { n => Gen.listOfN(n, Gen.choose(1, size).map(labeling))}.map { list => (tree, list)}
  }

  def hitsPerReadId(readId: Int, treeSize: Int, labeling: Int => String): Gen[List[Hit]] = {
    Gen.listOf(Gen.choose(1, treeSize).map(labeling).flatMap(hit(readId, _)))
  }

  def hit(readId: Int, node: String): Gen[Hit] = {
    for {
      quality <- Gen.chooseNum(300, 400)
    } yield Hit(ReadId(readId.toString), refId(Taxon(node)), quality)
  }

  def refId(taxon: Taxon) = RefId("gi|" + taxon.taxId + "|gb|000|")

  def groupedHits(readsAmount: Int, treeSize: Int, labeling: Int => String): Gen[Map[Int, List[Hit]]] = {
    genMap((1 to readsAmount).toList, { id => hitsPerReadId(id, treeSize, labeling)})
  }

  def read(readId: Int) = FASTQ(RawHeader(readId.toString), "ATG", "+", "quality")

}