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

  val splitDataMappings: List[DataMapping[splitDataProcessing]]

  /* - BLAST */
  val blastDataMappings: List[DataMapping[blastDataProcessing[Params]]]

  /* - Assignment */
  val assignDataMappings: List[DataMapping[assignDataProcessing[Params]]]

  /* - Merge */
  val mergeDataMappings: List[DataMapping[mergeDataProcessing.type]]

  /* - Counting */
  lazy val countDataMappings: List[DataMapping[countDataProcessing.type]] =
    mergeDataMappings.map { case mergeDM =>
      val sampleId = mergeDM.label

      DataMapping(sampleId, countDataProcessing)(
        remoteInput = Map(
          lookup(data.lcaCSV, mergeDM.remoteOutput),
          lookup(data.bbhCSV, mergeDM.remoteOutput)
        ),
        remoteOutput = Map(
          data.lcaDirectCountsCSV     -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.direct.absolute.counts.csv"),
          data.lcaAccumCountsCSV      -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.accum.absolute.counts.csv"),
          data.lcaDirectFreqCountsCSV -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.direct.frequency.counts.csv"),
          data.lcaAccumFreqCountsCSV  -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.lca.accum.frequency.counts.csv"),
          data.bbhDirectCountsCSV     -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.direct.absolute.counts.csv"),
          data.bbhAccumCountsCSV      -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.accum.absolute.counts.csv"),
          data.bbhDirectFreqCountsCSV -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.direct.frequency.counts.csv"),
          data.bbhAccumFreqCountsCSV  -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.bbh.accum.frequency.counts.csv")
        )
      )
    }

}
