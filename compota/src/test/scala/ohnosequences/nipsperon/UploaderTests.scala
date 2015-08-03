package ohnosequences.nipsperon

import org.junit.Test
import org.junit.Assert._
import ohnosequences.nisperon.{MultiPartUploadingContext, MultiPartUploader, AWS}
import java.io.File
import scala.util.Random
import ohnosequences.awstools.s3.ObjectAddress
import scala.annotation.tailrec

class UploaderTests {

  @Test
  def multiPart() {
    val partsDirectory = ObjectAddress("compota", "test") / "multipart"
    val merged = partsDirectory / "merged"

    def partAddress(part: Int) = {
      partsDirectory / (part + ".part")
    }

    val aws = new AWS(new File(System.getProperty("user.home"), "nispero.credentials"))

    println("uploading objects")
    val parts = 1
    val buffer = new StringBuilder()
    //preparing data
    for (i <- 1 to parts) {
      val s = new Random().alphanumeric.take(100).mkString
      buffer.append(s)
      aws.s3.putWholeObject(partAddress(i), s)
    }

    val uploader = new MultiPartUploader(aws.s3, merged)
    println("uploading parts")
    val context = uploader.start()

    @tailrec
    def uploadParts(context: MultiPartUploadingContext, partNumber: Int): MultiPartUploadingContext = {
      if (partNumber <= parts) {
        val stream = aws.s3.getObjectStream(partAddress(partNumber))

        val p = aws.s3.s3.getObject(partAddress(partNumber).bucket, partAddress(partNumber).key)
        uploadParts(uploader.addPart(context, p.getObjectContent, p.getObjectMetadata.getContentLength, partNumber), partNumber + 1)
      } else {
        context
      }
    }



    uploader.finish(uploadParts(context, 1))

    assertEquals(buffer.toString(), aws.s3.readWholeObject(merged))
    //uploading


  }
}
