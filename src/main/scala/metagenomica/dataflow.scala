package ohnosequences.mg7

import ohnosequences.mg7.loquats._
import ohnosequences.loquat._
import ohnosequences.datasets._
import ohnosequences.awstools.s3._

trait AnyDataflow {

  val outputS3Folder: (SampleID, StepName) => S3Folder

  // param-pam-pam
  type Params <: AnyMG7Parameters
  val  params: Params

  /* The essential steps of any MG7 dataflow are */

  /* - BLAST */
  val blastDataMappings: List[DataMapping[blastDataProcessing[Params]]]

  /* - Assignment */
  val assignmentDataMappings: List[DataMapping[assignmentDataProcessing[Params]]]

  /* - Counting */
  lazy val countingDataMappings: List[DataMapping[countingDataProcessing.type]] =
    assignmentDataMappings.map { assignmentDM =>
      val sampleId = assignmentDM.label

      DataMapping(sampleId, countingDataProcessing)(
        remoteInput = assignmentDM.remoteOutput,
        remoteOutput = Map(
          data.lcaCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.counts.csv"),
          data.bbhCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.counts.csv")
        )
      )
    }

}
