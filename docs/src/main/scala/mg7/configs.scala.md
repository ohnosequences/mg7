
```scala
package ohnosequences.mg7

import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.awstools._, ec2._, autoscaling._, regions._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api.{ outputFields => out, _ }

trait AnyMG7LoquatConfig extends AnyLoquatConfig {

  val pipelineName: String
  val stepName: String
  lazy val loquatName: String = s"${pipelineName}-${stepName}"

  lazy val defaultAMI = AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  lazy val managerConfig = ManagerConfig(
    defaultAMI,
    m3.medium,
    PurchaseModel.spot(0.1)
  )

  val size: Int //= 1

  lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    defaultAMI,
    m3.medium,
    PurchaseModel.spot(0.1),
    AutoScalingGroupSize(0, size, size)
  )

  lazy val terminationConfig = TerminationConfig(
    terminateAfterInitialDataMappings = true
  )
}

abstract class MG7LoquatConfig(val stepName: String) extends AnyMG7LoquatConfig


abstract class AnyFlashConfig extends MG7LoquatConfig("flash")
abstract class AnySplitConfig extends MG7LoquatConfig("split")
abstract class AnyCountConfig extends MG7LoquatConfig("count")

abstract class AnyBlastConfig extends MG7LoquatConfig("blast") {

  // NOTE: we don't want to check input objects here because they are too many and
  //   checking them one by one will take too long and likely fail
  override val checkInputObjects = false

  override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    defaultAMI,
    c3.large,
    PurchaseModel.spot(0.025),
    AutoScalingGroupSize(0, size, size)
  )
}

abstract class AnyAssignConfig extends MG7LoquatConfig("assign") {

  // NOTE: we don't want to check input objects here because they are too many and
  //   checking them one by one will take too long and likely fail
  override val checkInputObjects = false

  override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 10)
  override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    defaultAMI,
    m3.xlarge,
    PurchaseModel.spot(0.05),
    AutoScalingGroupSize(0, size, size)
  )
}

abstract class AnyMergeConfig extends MG7LoquatConfig("merge") {

  // NOTE: if all blast results are empty, we still want to get the "merged" empty file
  override val skipEmptyResults = false

  override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    defaultAMI,
    m3.xlarge,
    PurchaseModel.spot(0.05),
    AutoScalingGroupSize(0, size, size)
  )
}

```




[main/scala/mg7/bundles.scala]: bundles.scala.md
[main/scala/mg7/configs.scala]: configs.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/defaults.scala]: defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[main/scala/mg7/pipeline.scala]: pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/fqnames.scala]: ../../../test/scala/mg7/fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: ../../../test/scala/mg7/mock/illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: ../../../test/scala/mg7/mock/pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: ../../../test/scala/mg7/PRJEB6592/PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: ../../../test/scala/mg7/referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../test/scala/mg7/taxonomy.scala.md
[test/scala/mg7/testData.scala]: ../../../test/scala/mg7/testData.scala.md
[test/scala/mg7/testDefaults.scala]: ../../../test/scala/mg7/testDefaults.scala.md