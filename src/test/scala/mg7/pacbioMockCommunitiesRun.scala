package ohnosequences.test.mg7

import ohnosequences.test.mg7.testDefaults._
import ohnosequences.mg7._, loquats._, dataflows._
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

case object pacbioTestPipeline {

  // TODO update this to the current conventions
  val commonS3Prefix = S3Folder("era7p", "pacbio/data/")
  //
  // val sampleIds: List[ID] = List(
  //   "BEI_even",
  //   "BEI_staggered",
  //   "CAMI",
  //   "SakinawLake"
  // )
  //
  // val splitInputs: Map[ID, S3Resource] = sampleIds.map { id =>
  //   id -> S3Resource(commonS3Prefix / "in" / s"${id}_16S.fastq")
  // }.toMap
  //
  val cleanSampleIds: List[ID] = List(
    //"stagg",
    //"even"
    //this sample name corresponds to the blast results @rtobes filtered manually
    "even-filtered"
  )

  val cleanInputs: Map[ID, S3Resource] = cleanSampleIds.map { id =>
    id -> S3Resource(commonS3Prefix / "in" / s"${id}.subreads_ccs_99.fastq.filter.fastq")
  }.toMap

  // val dataflow = NoFlashDataflow(testDefaults.PacBio.parameters)(cleanInputs)

  // case object splitConfig extends TestLoquatConfig("split", dataflow.splitDataMappings)
  // case object splitLoquat extends Loquat(splitConfig, splitDataProcessing(testDefaults.PacBio.parameters))
  //
  // case object blastConfig extends TestLoquatConfig("blast", dataflow.blastDataMappings) {
  //   // NOTE: we don't want to check input objects here because they are too many and
  //   //   checking them one by one will take too long and likely fail
  //   override val checkInputObjects = false
  //   override lazy val workersConfig = testDefaults.PacBio.blastWorkers
  // }
  // case object blastLoquat extends Loquat(blastConfig, blastDataProcessing(testDefaults.PacBio.parameters))
  //
  // case object assignConfig extends TestLoquatConfig("assign", dataflow.assignDataMappings) {
  //
  //   override val checkInputObjects = false
  //   override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 10)
  //   override lazy val workersConfig = testDefaults.PacBio.assignWorkers
  // }
  // case object assignLoquat extends Loquat(assignConfig, assignDataProcessing(testDefaults.PacBio.parameters))
  //
  // case object mergeConfig extends TestLoquatConfig("merge", dataflow.mergeDataMappings) {
  //
  //   override val skipEmptyResults = false
  //   override lazy val workersConfig: AnyWorkersConfig = testDefaults.PacBio.mergeWorkers
  // }
  // case object mergeLoquat extends Loquat(mergeConfig, mergeDataProcessing)
  //
  // case object countConfig extends TestLoquatConfig("count", dataflow.countDataMappings)
  // case object countLoquat extends Loquat(countConfig, countDataProcessing)
}
