package ohnosequences.metapasta.tests

import org.junit.Test
import org.junit.Assert._
import ohnosequences.awstools.s3._
import ohnosequences.formats._
import ohnosequences.parsers._

class ParserTest {

  import TestReads._

  //@Test
  def duplicatesTest() = {
    def noDuplicates[T](reads: List[T]): Boolean = {
      val readsNumber = reads.length
      val distinctNumber = reads.distinct.length
      if (readsNumber != distinctNumber)
        println(s"Of ${readsNumber} reads only ${distinctNumber} are distinct")
      readsNumber == distinctNumber
    }
   // assert(noDuplicates(reads1000))
   // assert(noDuplicates(reads10000))
    assert(noDuplicates(reads100000))
  }

  //@Test // checking that its independent from the chunk size
  def sameReadsNumberTest() = {
    assert(reads1000.length == reads10000.length)
    assert(reads100000.length == reads100000.length)
  }

}