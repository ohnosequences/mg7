package ohnosequences.test.mg7.mock

import ohnosequences.test.mg7._, testDefaults._
import ohnosequences.mg7._
import ohnosequences.datasets._
import ohnosequences.datasets.illumina._

case object illumina {

  case object pipeline extends FlashMG7Pipeline(defaults.Illumina(rna16sRefDB)) with MG7PipelineDefaults {

    override lazy val name = "illumina"

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

    val flashParameters = FlashParameters(bp250)

    val flashConfig: AnyFlashConfig = FlashConfig(1)
  }
}
