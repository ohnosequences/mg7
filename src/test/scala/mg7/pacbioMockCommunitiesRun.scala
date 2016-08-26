package ohnosequences.test.mg7

import ohnosequences.test.mg7.testDefaults._
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

case object Pacbio {

  case object pipeline extends MG7Pipeline(testDefaults.PacBioParameters) with MG7PipelineDefaults {

    // TODO: update this to the current conventions
    val commonS3Prefix = S3Folder("era7p", "pacbio/data/")

    // val sampleIds: List[ID] = List(
    //   "BEI_even",
    //   "BEI_staggered",
    //   "CAMI",
    //   "SakinawLake"
    // )
    //
    // val inputSamples: Map[ID, S3Resource] = sampleIds.map { id =>
    //   id -> S3Resource(commonS3Prefix / "in" / s"${id}_16S.fastq")
    // }.toMap

    val cleanSampleIds: List[ID] = List(
      //"stagg",
      //"even"
      //this sample name corresponds to the blast results @rtobes filtered manually
      "even-filtered"
    )

    val inputSamples: Map[ID, S3Resource] = cleanSampleIds.map { id =>
      id -> S3Resource(commonS3Prefix / "in" / s"${id}.subreads_ccs_99.fastq.filter.fastq")
    }.toMap

    val outputS3Folder = testDefaults.outputS3FolderFor("Pacbio")
  }
}
