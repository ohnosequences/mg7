package ohnosequences.test.mg7.mock

import ohnosequences.test.mg7._, testDefaults._
import ohnosequences.mg7._
import ohnosequences.datasets._

case object pacbio {

  case object pipeline extends MG7Pipeline(defaults.PacBio(rna16sRefDB)) with MG7PipelineDefaults {

    override lazy val name = "pacbio"

    val sampleIDs: List[ID] = List(
      "stagg",
      "even"
      // NOTE: this sample name corresponds to the blast results @rtobes filtered manually
      // "even-filtered"
    )

    val inputSamples: Map[ID, S3Resource] = sampleIDs.map { id =>
      id -> S3Resource(testData.s3 / "pacbio" / s"${id}.subreads_ccs_99.fastq.filter.fastq")
    }.toMap
  }
}
