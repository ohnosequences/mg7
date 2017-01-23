
```scala
package ohnosequences.test.mg7
import ohnosequences.test.mg7.testDefaults._
import ohnosequences.mg7._, loquats._
import ohnosequences.datasets._, illumina._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.blast.api._
import ohnosequences.awstools._, ec2._ , s3._, autoscaling._, regions._
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
    override lazy val name = "PRJEB6592"

    val sampleIds: List[ID] = List("ERR567374")

    val commonS3Prefix = s3"resources.ohnosequences.com/16s/public-datasets/PRJEB6592"/

    lazy val inputSamples: Map[SampleID, S3Resource] = sampleIds.map { sampleId =>
      sampleId -> S3Resource(commonS3Prefix / "flash-test" / s"${sampleId}.merged.fastq")
    }.toMap

    override val splitConfig  = SplitConfig(1)
    override val blastConfig  = BlastConfig(10)
    override val assignConfig = AssignConfig(1)
    override val mergeConfig  = MergeConfig(1)
    override val countConfig  = CountConfig(1)
  }

}

```




[main/scala/mg7/bundles.scala]: ../../../../main/scala/mg7/bundles.scala.md
[main/scala/mg7/configs.scala]: ../../../../main/scala/mg7/configs.scala.md
[main/scala/mg7/csv.scala]: ../../../../main/scala/mg7/csv.scala.md
[main/scala/mg7/data.scala]: ../../../../main/scala/mg7/data.scala.md
[main/scala/mg7/defaults.scala]: ../../../../main/scala/mg7/defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../../../../main/scala/mg7/loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../../../../main/scala/mg7/loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../../../../main/scala/mg7/loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../../../../main/scala/mg7/loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../../../../main/scala/mg7/loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../../../../main/scala/mg7/loquats/6.count.scala.md
[main/scala/mg7/package.scala]: ../../../../main/scala/mg7/package.scala.md
[main/scala/mg7/parameters.scala]: ../../../../main/scala/mg7/parameters.scala.md
[main/scala/mg7/pipeline.scala]: ../../../../main/scala/mg7/pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: ../../../../main/scala/mg7/referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../counts.scala.md
[test/scala/mg7/fqnames.scala]: ../fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: ../mock/illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: ../mock/pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: ../referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: ../taxonomy.scala.md
[test/scala/mg7/testData.scala]: ../testData.scala.md
[test/scala/mg7/testDefaults.scala]: ../testDefaults.scala.md