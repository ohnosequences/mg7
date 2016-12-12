package ohnosequences.test.mg7.mock

import ohnosequences.test.mg7._, testDefaults._
import ohnosequences.mg7._, loquats._
import ohnosequences.datasets._
import ohnosequences.datasets.illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._
import ohnosequences.awstools._, ec2._, s3._, autoscaling._, regions._
import com.amazonaws.auth._, profile._

case object illumina {

  case object pipeline extends FlashMG7Pipeline(IlluminaParameters) with MG7PipelineDefaults {

    // TODO move all this to the testData object
    /* For now we are only testing one sample */
    val sampleIDs: List[SampleID] = List(
      "ERR1049996"
      // "ERR1049997",
      // "ERR1049998",
      // "ERR1049999",
      // "ERR1050000",
      // "ERR1050001"
    )

    val inputPairedReads: Map[SampleID, (S3Resource, S3Resource)] = sampleIDs.map { id =>
      id -> ((
        S3Resource(testData.s3 / "illumina" / s"${id}_1_val_1.fq.gz"),
        S3Resource(testData.s3 / "illumina" / s"${id}_2_val_2.fq.gz")
      ))
    }.toMap

    val outputS3Folder = testDefaults.outputS3FolderFor("illumina")

    val flashParameters = FlashParameters(bp250)

    val flashConfig: AnyFlashConfig = FlashConfig(1)
  }
}