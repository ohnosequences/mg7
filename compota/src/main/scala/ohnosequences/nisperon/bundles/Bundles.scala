package ohnosequences.nisperon.bundles

import ohnosequences.statika._
import ohnosequences.statika.aws._
import ohnosequences.typesets._
import shapeless._


import ohnosequences.nisperon._
import org.clapper.avsl.Logger
import ohnosequences.nisperon.queues.SQSQueue


trait InstructionsBundleAux extends AnyBundle {
  override def install[Dist <: AnyDistribution](distribution: Dist): InstallResults = {
    success("instructions finished")
  }

}

abstract class InstructionsBundle[D <: TypeSet : ofBundles, T <: HList : towerFor[D]#is](val deps: D = ∅) extends InstructionsBundleAux {
  type Deps = D
  val  depsTower = deps.tower
  type DepsTower = T
}


trait WorkerBundleAux extends AnyBundle {
  type IA <: InstructionsBundleAux
  val instructions: IA

  type Deps = IA :~: ∅
  val deps =  instructions :~: ∅


}

abstract class WorkerBundle[I <: InstructionsBundleAux, T <: HList : towerFor[I :~: ∅]#is](val instructions: I) extends WorkerBundleAux {
  type IA = I
  val  depsTower = deps.tower
  type DepsTower = T
}


trait ManagerDistributionAux extends AnyAWSDistribution {
  type WA <: WorkerBundleAux
  val worker: WA

  type Deps = ∅
  val deps = ∅

  type Metadata = NisperonMetadata

  type AMI = NisperonAMI.type
  val ami = NisperonAMI

  type Members = WA :~: ∅
  val members = worker :~: ∅

}

abstract class ManagerDistribution[W <: WorkerBundleAux, T <: HList : towerFor[∅]#is](val worker: W) extends ManagerDistributionAux {

  type WA = W

  type DepsTower = T
  val  depsTower = deps.tower
}


trait NisperoDistributionAux extends AnyAWSDistribution {
  type MA <: ManagerDistributionAux
  val manager: MA

  type Deps = ∅
  val deps = ∅

  type Members = MA :~: ∅
  val members = manager :~: ∅

  type Metadata = NisperonMetadata

  type AMI = NisperonAMI.type
  val ami = NisperonAMI
}






abstract class NisperoDistribution[M <: ManagerDistributionAux, T <: HList : towerFor[∅]#is](val manager: M) extends NisperoDistributionAux {
  type MA =  M

  type DepsTower = T
  val  depsTower = deps.tower


  override def install[Dist <: AnyDistribution](distribution: Dist): InstallResults = {
    success("NisperoDistribution finished")
  }
}



class WhateverBundle[T <: HList : towerFor[∅]#is](nisperon: Nisperon, component: String, name: String) extends AnyAWSDistribution {

  type Deps = ∅
  val deps = ∅

  type DepsTower = T
  val  depsTower = deps.tower


  type Members = WhateverBundle[T] :~: ∅
  val members = this :~: ∅

  type Metadata = NisperonMetadata

  type AMI = NisperonAMI.type
  val ami = NisperonAMI

  val metadata = nisperon.nisperonConfiguration.metadataBuilder.build(component, name, nisperon.nisperonConfiguration.workingDir)

  override def install[Dist <: AnyDistribution](distribution: Dist): InstallResults = {
    success("WhateverBundle finished")

  }
}


