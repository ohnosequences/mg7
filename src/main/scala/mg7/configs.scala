package ohnosequences.mg7

import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.autoscaling._
import ohnosequences.awstools.regions.Region._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api.{ outputFields => out, _ }

trait AnyMG7LoquatConfig extends AnyLoquatConfig {
  // lazy val metadata: AnyArtifactMetadata = ???
  // val iamRoleName = ???
  // val logsBucketName = ???

  lazy val defaultAMI = AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  lazy val  managerConfig = ManagerConfig(
    InstanceSpecs(defaultAMI, m3.medium),
    purchaseModel = Spot(maxPrice = Some(0.1))
  )

  val size: Int = 1

  lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    instanceSpecs = InstanceSpecs(defaultAMI, m3.medium),
    purchaseModel = Spot(maxPrice = Some(0.1)),
    groupSize = AutoScalingGroupSize(0, size, size)
  )

  lazy val terminationConfig = TerminationConfig(
    terminateAfterInitialDataMappings = true
  )
}
