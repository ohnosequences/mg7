package ohnosequences.mg7

import ohnosequences.loquat._
import ohnosequences.statika._, aws._
import ohnosequences.awstools.ec2._, InstanceType._
import ohnosequences.awstools.autoscaling._


trait AnyAssignConfig extends AnyMG7LoquatConfig {

  /* Data processing parameters */

  /* Loquat parameters */

  // NOTE: we don't want to check input objects here because they are too many and
  //   checking them one by one will take too long and likely fail
  override val checkInputObjects = false

  override val size = 10

  override lazy val amiEnv = amznAMIEnv(ami, javaHeap = 10)
  override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    instanceSpecs = InstanceSpecs(defaultAMI, m3.xlarge),
    purchaseModel = Spot(maxPrice = Some(0.05)),
    groupSize = AutoScalingGroupSize(0, size, size)
  )
}
