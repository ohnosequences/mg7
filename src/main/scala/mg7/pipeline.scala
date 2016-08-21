package ohnosequences.mg7

import ohnosequences.mg7.loquats._
import ohnosequences.loquat._


trait AnyPipeline {

  type Parameters <: AnyMG7Parameters
  val parameters: Parameters

  val dataflow: AnyDataflow { type Params = Parameters }

  val splitConfig:  AnySplitConfig
  val blastConfig:  AnyBlastConfig
  val assignConfig: AnyAssignConfig
  val mergeConfig:  AnyMergeConfig
  val countConfig:  AnyCountConfig

  case object splitLoquat  extends Loquat(splitConfig,  splitDataProcessing(parameters))(dataflow.splitDataMappings)
  case object blastLoquat  extends Loquat(blastConfig,  blastDataProcessing(parameters))(dataflow.blastDataMappings)
  case object assignLoquat extends Loquat(assignConfig, assignDataProcessing(parameters))(dataflow.assignDataMappings)
  case object mergeLoquat  extends Loquat(mergeConfig,  mergeDataProcessing)(dataflow.mergeDataMappings)
  case object countLoquat  extends Loquat(countConfig,  countDataProcessing)(dataflow.countDataMappings)
}


// class NoFlashPipeline(val dataflow: AnyDataflow) extends AnyPipeline

// class FullPipeline(val dataflow: AnyFullDataflow) extends AnyPipeline {
//
//   val flashConfig: AnyFlashConfig
//   case object flashLoquat extends Loquat(flashConfig, flashDataProcessing(parameters))
// }
