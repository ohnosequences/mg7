package ohnosequences.mg7

import ohnosequences.mg7.loquats._
import ohnosequences.loquat._
import ohnosequences.datasets._
import ohnosequences.awstools.s3._

trait AnyDataflow {

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
          data.lcaDirectCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.direct.absolute.counts.csv"),
          data.bbhDirectCountsCSV -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.direct.absolute.counts.csv"),
          data.lcaAccumCountsCSV  -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.lca.accum.absolute.counts.csv"),
          data.bbhAccumCountsCSV  -> S3Resource(outputS3Folder(sampleId, "counting") / s"${sampleId}.bbh.accum.absolute.counts.csv")
        )
      )
    }

}
