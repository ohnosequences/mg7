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


trait AnyBlastConfig extends AnyMG7LoquatConfig {

  /* Data processing parameters */

  type BlastCommand <: AnyBlastCommand {
    type ArgumentsVals = BlastArgumentsVals
  }
  val  blastCommand: BlastCommand

  type BlastOutRecKeys <: AnyBlastOutputFields{
    type Types <: AnyKList {
      type Bound <: AnyOutputField
      type Union <: BlastCommand#ValidOutputFields#Types#Union
    }
  }
  val  blastOutRec: BlastOutputRecord[BlastOutRecKeys]

  val blastOptions: BlastCommand#OptionsVals

  /* Loquat parameters */

  // NOTE: we don't want to check input objects here because they are too many and
  //   checking them one by one will take too long and likely fail
  override val checkInputObjects = false

  override val size = 100

  override lazy val workersConfig: AnyWorkersConfig = WorkersConfig(
    instanceSpecs = InstanceSpecs(defaultAMI, c3.large),
    purchaseModel = Spot(maxPrice = Some(0.025)),
    groupSize = AutoScalingGroupSize(0, size, size)
  )
}
