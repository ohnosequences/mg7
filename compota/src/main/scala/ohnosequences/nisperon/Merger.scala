package ohnosequences.nisperon

import ohnosequences.awstools.s3.{ObjectAddress, S3}
import java.io.{PrintWriter, File}
import ohnosequences.nisperon.logging.Logger
import scala.collection.mutable


trait Merger[T] {
  def merge(destination: ObjectAddress, parts: Traversable[T], total: Option[Int])
}


class MonoidInMemoryMerger[T](s3: S3, monoid: Monoid[T], serializer: Serializer[T], logger: Logger) extends Merger[T] {
  override def merge(destination: ObjectAddress, parts: Traversable[T], total: Option[Int]) {
    var result = monoid.unit
    var counter = 1
    parts.foreach { part =>
      if (counter % 100 == 0) {
        logger.info(counter + " parts merged"
          + total.map{ total => " " + (total - counter) + " left"}.getOrElse("")
        )
      }
      counter += 1
      result = monoid.mult(result, part)
    }

    logger.info("merged. writing result")
    s3.putWholeObject(destination, serializer.toString(result))
  }
}

//for monoids like String, lists
//we should traverse them either two times to calculate content length or store all parts into a file
class IncrementalMerger[T](s3: S3, incrementalSerializer: IncrementalSerializer[T], logger: Logger) extends Merger[T] {
  override def merge(destination: ObjectAddress, parts: Traversable[T], total: Option[Int]) {
  //  s3.s3.put

    val rawMerger = new IncrementalRawMerger(s3, destination, incrementalSerializer, logger, new File("merge-buffer.txt"))
    var counter = 1

    rawMerger.header()

    parts.foreach { part =>

      rawMerger.addPart(part)
      if (counter % 100 == 0) {
        logger.info(counter + " parts merged"
          + total.map{ total => " " + (total - counter) + " left"}.getOrElse("")
        )
      }
      counter += 1
    }
    rawMerger.footer()
  }
}


class IncrementalRawMerger[T](s3: S3, destination: ObjectAddress, incrementalSerializer: IncrementalSerializer[T], logger: Logger, bufferFile: File) {
  val printWriter = new PrintWriter(bufferFile)

  var first = true

  def header() {
    printWriter.print(incrementalSerializer.header)
  }

  def addPart(part: T) {
    if (first) {
      first = false
      printWriter.print(incrementalSerializer.firstPart(part))
    } else {
      printWriter.print(incrementalSerializer.part(part))
    }
  }

  def footer() {
    printWriter.close()
    s3.putObject(destination, bufferFile)
  }
}

class MapIncrementalMerger[K, T](s3: S3,  kSerializer: Serializer[K], incrementalSerializer: IncrementalSerializer[T], logger: Logger) extends Merger[(K, T)] {
  override def merge(destination: ObjectAddress, parts: Traversable[(K, T)], total: Option[Int]) {
    val rawMergers = new mutable.HashMap[K, IncrementalRawMerger[T]]()
    parts.foreach { case (key, part) =>
      rawMergers.get(key) match {
        case None => {
          val bufferFile = new File("buffer-" + kSerializer.toString(key) + ".txt")
          val m = new IncrementalRawMerger(s3, destination, incrementalSerializer, logger, bufferFile)
          rawMergers.put(key, m)
          m.header()
          m.addPart(part)
        }
        case Some(m) => {
          m.addPart(part)
        }
      }
    }
    rawMergers.values.foreach(_.footer())
  }
}


trait IncrementalSerializer[T] {
  def header: String
  def firstPart(t: T): String
  def part(t: T): String
  def footer: String
}

