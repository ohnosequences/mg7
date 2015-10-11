package ohnosequences.metagenomica.bundles

import ohnosequencesBundles.statika.Blast

// TODO: it should be in era7bio/bundles
case object blast extends Blast("2.2.31")


case object blastBundleTesting {
  import ohnosequences.statika.bundles._
  import ohnosequences.statika.instructions._
  import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._
  import ohnosequences.awstools.regions.Region._

  case object blastCompat extends Compatible(
    amzn_ami_64bit(Ireland, Virtualization.HVM)(1),
    blast,
    generated.metadata.Metagenomica
  )
}
