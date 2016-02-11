package ohnosequences.metagenomica.bundles

import ohnosequencesBundles.statika.Blast

// TODO: it should be in era7bio/bundles
case object blast extends Blast("2.2.31")


case object blastBundleTesting {
  import ohnosequences.statika._
  import ohnosequences.statika.aws._
  import ohnosequences.awstools.ec2._
  import ohnosequences.awstools.regions.Region._

  case object blastCompat extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
    blast,
    generated.metadata.Metagenomica
  )
}
