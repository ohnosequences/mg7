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
