import java.io.File
import ohnosequences.awstools.s3.{S3, ObjectAddress}

import ohnosequences.nisperon.AWS
import org.junit.Test
import org.junit.Assert._

class S3Splitter(s3: S3, address: ObjectAddress, chunksSize: Long) {

  def objectSize(): Long = {
    s3.s3.getObjectMetadata(address.bucket, address.key).getContentLength
  }

  def chunks(): List[(Long, Long)] = {

    val size = objectSize()

    val starts = 0L until size by chunksSize
    val ends = starts.map { start =>
      math.min(start + chunksSize - 1, size - 1)
    }

    starts.zip(ends).toList
  }
}

class OldTestCase {



//  @Test
  def test {
  val aws = new AWS(new File(System.getProperty("user.home"), "nispero.credentials"))
    val s3 = aws.s3

    val file = ObjectAddress("metapasta-test", "microtest.fastq")
    val chunks = new S3Splitter(s3, file, 100000).chunks()
    import ohnosequences.formats._
    import ohnosequences.parsers._
    val reader = S3ChunksReader(s3, file)
    var left = chunks.size
    var size = 0
    for (chunk <- chunks) {
      println(left + " chunks left")
      left -= 1
      val parsed: List[FASTQ[RawHeader]] = reader.parseChunk[RawHeader](chunk._1, chunk._2)._1
      size += parsed.size
    }
    //print 10001 instead 10000 ( = amount of reads in microtest.fastq)
    assertEquals(10000, size)
    println(size)
  }
}