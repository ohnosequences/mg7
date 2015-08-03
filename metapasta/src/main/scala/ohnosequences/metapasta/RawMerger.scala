package ohnosequences.metapasta

import ohnosequences.awstools.s3.{LoadingManager, ObjectAddress}
import java.io.{PrintWriter, File}
import ohnosequences.nisperon.AWS
import org.clapper.avsl.Logger


class FastaMerger(aws: AWS, readObject: ObjectAddress, resultsObject: ObjectAddress, samples: List[String]) {

  val logger = Logger(this.getClass)
  val lm = aws.s3.createLoadingManager()


  def merge() {
    for (sample <- samples) {
      logger.info("merging noHits fastas for sample " + sample)
      rawMerge(S3Paths.noHitFastas(readObject, sample), S3Paths.mergedNoHitFasta(resultsObject, sample), lm)



      for (asType <- List(LCA, BBH)) {

        logger.info("merging noTaxIds fastas for sample " + sample + " for " + asType)
        rawMerge(S3Paths.noTaxIdFastas(readObject, sample, asType), S3Paths.mergedNoTaxIdFasta(resultsObject, sample, asType), lm)

        logger.info("merging notAssigned fastas for sample " + sample + " for " + asType)
        rawMerge(S3Paths.notAssignedFastas(readObject, sample, asType), S3Paths.mergedNotAssignedFasta(resultsObject, sample, asType), lm)

        logger.info("merging assigned fastas for sample " + sample + " for " + asType)
        rawMerge(S3Paths.assignedFastas(readObject, sample, asType), S3Paths.mergedAssignedFasta(resultsObject, sample, asType), lm)
      }

    }
  }

  def rawMerge(address: ObjectAddress, dst: ObjectAddress, lm: LoadingManager) {
    val objects = aws.s3.listObjects(address.bucket, address.key)
    var c = objects.size
    val res = new File("res")
    val pw = new PrintWriter(res)

    if (objects.isEmpty) {
      logger.warn("couldn't find files in: " + address)
    }
    for (obj <- objects) {
      if (c % 100 == 0) {
        logger.info("merger: " + c + " objects left")
      }
      val s = try {
        aws.s3.readWholeObject(obj)
      } catch {
        case t: Throwable => logger.error("couldn't read from object: " + obj + " skipping"); ""
      }
      pw.println(s)
      c -= 1
    }
    pw.close()
    logger.info("uploading results to: "  + dst)
    lm.upload(dst, res)

  }

}
