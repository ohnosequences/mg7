package ohnosequences.mg7

import ohnosequences.mg7.loquats._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.datasets._
import ohnosequences.awstools._, s3._
import com.amazonaws.auth._, profile._


trait AnyMG7Pipeline { pipeline =>

  type Parameters <: AnyMG7Parameters
  val  parameters: Parameters

  val inputSamples: Map[SampleID, S3Resource]
  val outputS3Folder: (SampleID, StepName) => S3Folder

  val metadata: AnyArtifactMetadata
  val iamRoleName: String
  val logsS3Prefix: S3Folder

  /* This trait helps to set these common values */
  trait CommonConfigDefaults {

    val metadata: AnyArtifactMetadata = pipeline.metadata
    val iamRoleName: String           = pipeline.iamRoleName
    val logsS3Prefix: S3Folder        = pipeline.logsS3Prefix
  }

  case class  SplitConfig(val size: Int) extends AnySplitConfig  with CommonConfigDefaults
  case class  BlastConfig(val size: Int) extends AnyBlastConfig  with CommonConfigDefaults
  case class AssignConfig(val size: Int) extends AnyAssignConfig with CommonConfigDefaults
  case class  MergeConfig(val size: Int) extends AnyMergeConfig  with CommonConfigDefaults
  case class  CountConfig(val size: Int) extends AnyCountConfig  with CommonConfigDefaults

  /* You have to set these values to customize configuration for each step */
  val splitConfig:  AnySplitConfig
  val blastConfig:  AnyBlastConfig
  val assignConfig: AnyAssignConfig
  val mergeConfig:  AnyMergeConfig
  val countConfig:  AnyCountConfig

  // Boilerplate definitions that are derived from the ones above:

  lazy val splitDataMappings: DataMappings[splitDataProcessing] = inputSamples.toList.map { case (sampleId, readsS3Resource) =>

    DataMapping(sampleId, splitDataProcessing(parameters))(
      remoteInput = Map(
        data.mergedReads -> readsS3Resource
      ),
      remoteOutput = Map(
        data.fastaChunks -> S3Resource(outputS3Folder(sampleId, "split"))
      )
    )
  }

  private lazy val instanceS3client = S3Client()

  private def listChunks(s3prefix: AnyS3Address): List[(S3Object, Int)] = {
    instanceS3client
      .listObjects(S3Folder(s3prefix.toURI))
      .getOrElse(List())
      .zipWithIndex
  }

  // These prefixes will be used several times, so they factored in methods:
  private def blastChunksS3Prefix(sampleId: String): S3Folder = outputS3Folder(sampleId, "blast") / "chunks" /
  private def blastNoHitsS3Prefix(sampleId: String): S3Folder = outputS3Folder(sampleId, "blast") / "no-hits" /

  // Here we generate tasks/data mappings for the blast loquat one per each S3 object generated by the split loquat
  lazy val blastDataMappings: DataMappings[blastDataProcessing[Parameters]] = splitDataMappings.flatMap { splitDM =>
    val sampleId = splitDM.label

    listChunks( splitDM.remoteOutput(data.fastaChunks).resource )
      .map { case (chunkS3Obj, n) =>

        DataMapping(s"${sampleId}.${n}", blastDataProcessing(parameters))(
          remoteInput = Map(
            data.fastaChunk -> S3Resource(chunkS3Obj)
          ),
          remoteOutput = Map(
            data.blastChunk  -> S3Resource(blastChunksS3Prefix(sampleId) / s"blast.${n}.csv"),
            data.noHitsChunk -> S3Resource(blastNoHitsS3Prefix(sampleId) / s"no-hits.${n}.fa")
          )
        )
      }
  }

  // These prefixes will be used several times, so they factored in methods:
  private def lcaAssignS3Prefix(sampleId: String): S3Folder = outputS3Folder(sampleId, "assign") / "lca" /
  private def bbhAssignS3Prefix(sampleId: String): S3Folder = outputS3Folder(sampleId, "assign") / "bbh" /

  lazy val assignDataMappings: DataMappings[assignDataProcessing[Parameters]] = inputSamples.keys.toList.flatMap { case sampleId =>

    listChunks( blastChunksS3Prefix(sampleId) )
      .map { case (chunkS3Obj, n) =>

        DataMapping(sampleId, assignDataProcessing(parameters))(
          remoteInput = Map(
            data.blastChunk -> S3Resource(chunkS3Obj)
          ),
          remoteOutput = Map(
            data.lcaChunk -> S3Resource(lcaAssignS3Prefix(sampleId) / s"${sampleId}.lca.${n}.csv"),
            data.bbhChunk -> S3Resource(bbhAssignS3Prefix(sampleId) / s"${sampleId}.bbh.${n}.csv")
          )
        )
      }
  }

  lazy val mergeDataMappings: DataMappings[mergeDataProcessing] = inputSamples.keys.toList.map { case sampleId =>

    def outputFor(d: FileData): (FileData, S3Resource) = {
      d -> S3Resource(outputS3Folder(sampleId, "merge") / s"${sampleId}.${d.label}")
    }

    DataMapping(sampleId, mergeDataProcessing())(
      remoteInput = Map(
        data.blastChunksFolder -> S3Resource(blastChunksS3Prefix(sampleId)),
        data.blastNoHitsFolder -> S3Resource(blastNoHitsS3Prefix(sampleId)),
        data.lcaChunksFolder   -> S3Resource(lcaAssignS3Prefix(sampleId)),
        data.bbhChunksFolder   -> S3Resource(bbhAssignS3Prefix(sampleId))
      ),
      remoteOutput = Map(
        outputFor(data.blastResult),
        outputFor(data.blastNoHits),
        outputFor(data.lcaCSV),
        outputFor(data.bbhCSV)
      )
    )
  }

  lazy val countDataMappings: DataMappings[countDataProcessing] = mergeDataMappings.map { case mergeDM =>
    val sampleId = mergeDM.label

    def outputFor(d: FileData): (FileData, S3Resource) = {
      d -> S3Resource(outputS3Folder(sampleId, "count") / s"${sampleId}.${d.baseName}.csv")
    }

    DataMapping(sampleId, countDataProcessing())(
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

  lazy val fullName: String = this.getClass.getName.split("\\$").mkString(".")
  trait FixedName extends AnyLoquat {

    override lazy val fullName: String = s"${pipeline.fullName}.${this.toString}"
  }

  case object split  extends Loquat(splitConfig,   splitDataProcessing(parameters))(splitDataMappings)  with FixedName
  case object blast  extends Loquat(blastConfig,   blastDataProcessing(parameters))(blastDataMappings)  with FixedName
  case object assign extends Loquat(assignConfig, assignDataProcessing(parameters))(assignDataMappings) with FixedName
  case object merge  extends Loquat(mergeConfig,   mergeDataProcessing())(mergeDataMappings)            with FixedName
  case object count  extends Loquat(countConfig,   countDataProcessing())(countDataMappings)            with FixedName
}


/* This kind of pipeline adds the Flash data preprocessing step */
trait AnyFlashMG7Pipeline extends AnyMG7Pipeline {

  val flashParameters: AnyFlashParameters
  val inputPairedReads: Map[SampleID, (S3Resource, S3Resource)]

  case class FlashConfig(val size: Int) extends AnyFlashConfig  with CommonConfigDefaults

  val flashConfig: AnyFlashConfig


  lazy val flashDataMappings: DataMappings[flashDataProcessing] = inputPairedReads.toList.map { case (sampleId, (reads1S3Resource, reads2S3Resource)) =>

    def outputFor(d: FileData): (FileData, S3Resource) = {
      d -> S3Resource(outputS3Folder(sampleId, "flash") / s"${sampleId}.${d.label}")
    }

    DataMapping(sampleId, flashDataProcessing(flashParameters))(
      remoteInput = Map(
        data.pairedReads1 -> reads1S3Resource,
        data.pairedReads2 -> reads2S3Resource
      ),
      remoteOutput = Map(
        outputFor(data.mergedReads),
        outputFor(data.pair1NotMerged),
        outputFor(data.pair2NotMerged),
        outputFor(data.flashHistogram)
      )
    )
  }

  /* This is the input of the base pipeline derived from the output of Flash */
  final lazy val inputSamples: Map[SampleID, S3Resource] = flashDataMappings.map { flashDM =>
    flashDM.label -> flashDM.remoteOutput(data.mergedReads)
  }.toMap

  case object flash extends Loquat(flashConfig, flashDataProcessing(flashParameters))(flashDataMappings) with FixedName
}

/* With the constructor it is just easier to bind the Parameters type member. The rest of the members can be set inside */
abstract class MG7Pipeline[P <: AnyMG7Parameters](val parameters: P)
  extends AnyMG7Pipeline { type Parameters = P }

abstract class FlashMG7Pipeline[P <: AnyMG7Parameters](val parameters: P)
  extends AnyFlashMG7Pipeline { type Parameters = P }
