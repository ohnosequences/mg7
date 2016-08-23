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


trait AnyMergeConfig extends AnyMG7LoquatConfig {

  /* Data processing parameters */

  /* Loquat parameters */

  // NOTE: if all blast results are empty, we still want to get the "merged" empty file
  override val skipEmptyResults = false

  override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    instanceSpecs = InstanceSpecs(defaultAMI, m3.xlarge),
    purchaseModel = Spot(maxPrice = Some(0.05)),
    groupSize = AutoScalingGroupSize(0, size, size)
  )
}
