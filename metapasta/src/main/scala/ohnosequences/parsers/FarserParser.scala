package ohnosequences.parsers

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object

import ohnosequences.awstools._
import ohnosequences.awstools.s3._

import java.io._
import scala.io._

import ohnosequences.formats._

/* Type class which
   - requires to define header parser
   - provides parsers for reads
*/
trait ParsableHeaders[H <: Header] {
  def headerParser(line: String): Option[H]

  // "parses" one read/item/record
  def itemParser(ls: Seq[String]): Option[FASTQ[H]] = {
    headerParser(ls(0)) match {
      case Some(h) if (
          ls.length == 4 &&             // 4 lines
          ls(2).startsWith("+") &&      // third is opt-header
          ls(1).length == ls(3).length  // sequence length == quality lienght
        ) => Some(FASTQ(h, ls(1), ls(2), ls(3)))
      case _ => None
    }
  }
}

/* Implicit instances for know headers */
object ParsableHeaders {
  implicit object RawHeaderParser extends ParsableHeaders[RawHeader] {
    def headerParser(line: String) = if (line.startsWith("@")) Some(RawHeader(line)) else None
  }

  implicit object CasavaHeaderParser extends ParsableHeaders[CasavaHeader] {
    def headerParser(line: String) = {
      // @<instrument>:<run number>:<flowcell ID>:<lane>:<tile>:<x-pos>:<y-pos> <read>:<is filtered>:<control number>:<index sequence>
      // @EAS139:136:FC706VJ:2:2104:15343:197393 1:Y:18:ATCACG
      val casabaPattern = """@([\w\-]+):(\d+):(\w+):(\d+):(\d+):(\d+):(\d+)\s+(\d+):(\w):(\d+):(\w+)""".r
      line match {
        case casabaPattern(instrument, runNumber, flowcellID, lane, tile, xPos, yPos, read, isFiltered, controlNumber, indexSequence) =>
          Some(CasavaHeader(line)(instrument, runNumber.toLong, flowcellID, lane.toLong, tile.toLong, xPos.toLong, yPos.toLong, read.toLong, isFiltered, controlNumber.toLong, indexSequence))
        case _ => None
      }
    }
  }
}

/* Class which works reads S3 by chunks and parses them */
case class S3ChunksReader(s3: S3, address: ObjectAddress) {

  /* This function reads part/chunk of an S3 object and returns it as `S3Object` */
  def s3ObjectChunk(start: Long, end: Long): S3Object = {
    val objSize = s3.s3.getObjectMetadata(address.bucket, address.key).getContentLength
    val left = List(start, end, objSize).min // real left end of range
    val right = List(start, List(end, objSize).min).max // real right end, but not further than the end of file
    val request = new GetObjectRequest(address.bucket, address.key).withRange(left, right)
    // TODO: deal somehow with `InvalidRange` exception
    // try {
    s3.s3.getObject(request)
    // } catch {
    //   case e: com.amazonaws.AmazonServiceException
    //     if e.getErrorCode == "InvalidRange" => ???
    // }
  }

  implicit class extS3Object(obj: S3Object) {
    /* This adds a method to `S3Object` which returns an iterator over the object's content lines */
    def lines: Iterator[String] = Source.fromInputStream(obj.getObjectContent).getLines()

    /* This is a colvenience method, which helps not to forget to close the `S3Object` after using it */
    def closeAfter[R](clos: S3Object => R): R = {
      val r = clos(obj)
      obj.close
      r
    }
  }

  /* Just an alias */
  type ParsedNumber = Long

  /* Takes an iterator over lines and returns:
     (parsed reads, the rest of unparsed lines, number of parsed reads) */
  def readsParser[H <: Header : ParsableHeaders](strm: Iterator[String]):
  (List[FASTQ[H]], Seq[String], ParsedNumber) = {
    // picking the right parser
    val parser = implicitly[ParsableHeaders[H]].itemParser _

    // sliding, parsing, accumulating
    strm.sliding(4).foldLeft(List[FASTQ[H]](), Seq[String](), 0L) {
      case ((parsed, unparsed, parsedNumber), r) =>
        parser(r) match {
          case Some(read) => (read :: parsed, Seq[String](), parsedNumber + 1)
          // TODO: this unparsed thing is a bit tricky, should write it more clear
          case _          => (parsed, unparsed ++ r.drop(3), parsedNumber)
        }
    }
  }

  /*  */
  def parseChunk[H <: Header : ParsableHeaders](start: Long, end: Long):
  (List[FASTQ[H]], ParsedNumber) = {
    s3ObjectChunk(start, end).closeAfter { chunk =>
      val (reads, unparsed, n) = readsParser(chunk.lines)

      /* Perfect match! */
      if(unparsed.isEmpty) {
        // println("Perfect match!!!")
        (reads, n)
      } else {
        /* Reading the next chunk */
        s3ObjectChunk(end + 1, end + 1 + (end-start)).closeAfter { chunk =>
          /* but we want only to complete the `unparsed` part */
          val lines = chunk.lines.take(4).toSeq
          /* first we try just to add the new lines and parse */
          val concatenated = unparsed ++ lines
          val (psd, u, _) = readsParser(concatenated.toIterator)
          /* maybe it's something... */
          val mbLast = if (psd.nonEmpty) psd.take(1)
          /* but if it didn't work, it's likely that the last line of `unparsed` was cut */
            else {
              /* so we glue it with the first of the new chunk*/
              val glued = unparsed.init ++ ((unparsed.last + lines.head) +: lines.tail)
              val (rest, u, _) = readsParser(glued.toIterator)
              if (rest.nonEmpty) rest.take(1)
              /* this can happen **only** if it was the end of file and there were no enough lines */
              else if (concatenated.length < 4 || glued.length < 4) Seq()
              /* otherwise there is probably a bug in the parser */
              else throw new RuntimeException(s"Reads parser failed to parse additional read:\n ${glued}")
            }
          (reads ++ mbLast, n + mbLast.length)
        }
      }
    }
  }
}