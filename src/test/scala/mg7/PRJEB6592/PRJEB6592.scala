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

// TODO this is a gut sample after all, see what we are going to do with this
case object PRJEB6592 {

  val commonS3Prefix = S3Folder("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592")

  val sampleIds: List[ID] = List("ERR567374")

  def testOutS3Folder(sampleId: SampleID, step: StepName): S3Folder =
    commonS3Prefix / s"${step}-test" / sampleId /

  lazy val inputSamples: Map[ID, (S3Resource, S3Resource)] = sampleIds.map { id =>
    id -> ((
      S3Resource(commonS3Prefix / "reads" / s"${id}_1.fastq.gz"),
      S3Resource(commonS3Prefix / "reads" / s"${id}_2.fastq.gz")
    ))
  }.toMap

  lazy val splitInputs: Map[ID, S3Resource] = sampleIds.map { sampleId =>
    sampleId -> S3Resource(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq")
  }.toMap

  case object testParameters extends MG7Parameters(
    outputS3Folder  = testOutS3Folder,
    readsLength     = bp300,
    blastCommand    = blastn,
    blastOutRec     = defaults.blastnOutputRecord,
    blastOptions    = defaults.blastnOptions.value,
    referenceDBs    = Set(rna16sRefDB)
  )
  {

    // an example of how you can add some conditions to the filter predicate
    override def blastFilter(row: csv.Row[BlastOutRecKeys]): Boolean = {
      defaultBlastFilter(row) ||
      parseInt(row.select(outputFields.bitscore)).map{ _ > 42 }.getOrElse(false)
    }
  }

  val dataflow = NoFlashDataflow(testParameters)(splitInputs)

  case object splitConfig extends TestLoquatConfig("split", dataflow.splitDataMappings)
  case object splitLoquat extends Loquat(splitConfig, splitDataProcessing(testParameters))

  case object blastConfig extends TestLoquatConfig("blast", dataflow.blastDataMappings) {
    // NOTE: we don't want to check input objects here because they are too many and
    //   checking them one by one will take too long and likely fail
    override val checkInputObjects = false
  }
  case object blastLoquat extends Loquat(blastConfig, blastDataProcessing(testParameters))

  case object mergeConfig extends TestLoquatConfig("merge", dataflow.mergeDataMappings)
  case object mergeLoquat extends Loquat(mergeConfig, mergeDataProcessing)

  case object assignConfig extends TestLoquatConfig("assign", dataflow.assignDataMappings) {

    override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 3)

    override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
      instanceSpecs = InstanceSpecs(defaultAMI, r3.large),
      purchaseModel = Spot(maxPrice = Some(0.4)),
      groupSize = AutoScalingGroupSize(0, 1, 10)
    )
  }
  case object assignLoquat extends Loquat(assignConfig, assignDataProcessing(testParameters))

  case object countConfig extends TestLoquatConfig("count", dataflow.countDataMappings)
  case object countLoquat extends Loquat(countConfig, countDataProcessing)
}
