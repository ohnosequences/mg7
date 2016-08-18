package ohnosequences.mg7

import ohnosequences.mg7.loquats._
import ohnosequences.loquat._
import ohnosequences.datasets._
import ohnosequences.awstools.s3._

trait AnyDataflow {

  type Params <: AnyMG7Parameters
  val  params: Params

  /* The essential steps of any MG7 dataflow are */
  val splitDataMappings : List[DataMapping[splitDataProcessing]]
  /* - BLAST */
  val blastDataMappings : List[DataMapping[blastDataProcessing[Params]]]
  /* - Assignment */
  val assignDataMappings: List[DataMapping[assignDataProcessing[Params]]]
  /* - Merge */
  val mergeDataMappings : List[DataMapping[mergeDataProcessing.type]]
  /* - Counting */
  lazy val countDataMappings: List[DataMapping[countDataProcessing.type]] =
    mergeDataMappings.map { case mergeDM =>

      val sampleId = mergeDM.label

      def outputFor(d: FileData): (FileData, S3Resource) = {
        d -> S3Resource(params.outputS3Folder(sampleId, "count") / s"${sampleId}.${d.baseName}.csv")
      }

      DataMapping(sampleId, countDataProcessing)(

        remoteInput = Map(
          lookup(data.lcaCSV, mergeDM.remoteOutput),
          lookup(data.bbhCSV, mergeDM.remoteOutput)
        ),

        remoteOutput = Map(
          outputFor(data.lca.direct.absolute),
          outputFor(data.lca.accum.absolute),
          outputFor(data.lca.direct.relative),
          outputFor(data.lca.accum.relative),
          outputFor(data.bbh.direct.absolute),
          outputFor(data.bbh.accum.absolute),
          outputFor(data.bbh.direct.relative),
          outputFor(data.bbh.accum.relative)
        )
      )
    }
}
