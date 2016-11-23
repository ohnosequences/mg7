package ohnosequences.test.mg7.mock

import ohnosequences.test.mg7._, testDefaults._
import ohnosequences.mg7._, loquats._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._
import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import com.amazonaws.auth._, profile._

case object pacbio {

  case object pipeline extends MG7Pipeline(PacBioParameters) with MG7PipelineDefaults {

    val sampleIDs: List[ID] = List(
      "stagg",
      "even"
      // NOTE: this sample name corresponds to the blast results @rtobes filtered manually
      // "even-filtered"
    )

    val inputSamples: Map[ID, S3Resource] = sampleIDs.map { id =>
      id -> S3Resource(testData.s3 / "pacbio" / s"${id}.subreads_ccs_99.fastq.filter.fastq")
    }.toMap

    val outputS3Folder = testDefaults.outputS3FolderFor("pacbio")
  }
}
