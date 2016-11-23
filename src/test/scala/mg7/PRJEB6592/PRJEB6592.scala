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

// TODO this is a gut sample after all, see what we are going to do with this
case object PRJEB6592 {

  case object parameters extends MG7Parameters(
    splitInputFormat = FastQInput,
    splitChunkSize   = 10,
    blastCommand     = blastn,
    blastOutRec      = defaults.blastnOutputRecord,
    blastOptions     = defaults.blastnOptions.value,
    referenceDBs     = Set(rna16sRefDB)
  ) {

    // an example of how you can add some conditions to the filter predicate
    override def blastFilter(row: csv.Row[BlastOutRecKeys]): Boolean = {
      defaultBlastFilter(row) ||
      parseInt(row.select(outputFields.bitscore)).map{ _ > 42 }.getOrElse(false)
    }
  }

  case object pipeline extends MG7Pipeline(parameters) with MG7PipelineDefaults {

    val sampleIds: List[ID] = List("ERR567374")

    val commonS3Prefix = S3Folder("resources.ohnosequences.com", "16s/public-datasets/PRJEB6592")

    lazy val inputSamples: Map[SampleID, S3Resource] = sampleIds.map { sampleId =>
      sampleId -> S3Resource(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq")
    }.toMap

    lazy val outputS3Folder = testDefaults.outputS3FolderFor("PRJEB6592")

    override val splitConfig  = SplitConfig(1)
    override val blastConfig  = BlastConfig(10)
    override val assignConfig = AssignConfig(1)
    override val mergeConfig  = MergeConfig(1)
    override val countConfig  = CountConfig(1)
  }

}
