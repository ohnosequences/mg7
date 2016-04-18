
```scala
// package ohnosequences.mg7.tests
//
// import ohnosequences.mg7.bundles._
//
// import ohnosequences.statika._, aws._
//
// import ohnosequences.awstools.ec2._
// import ohnosequences.awstools.ec2.{Tag => Ec2Tag}
// import java.io._
// import org.scalatest._
// import scala.annotation.tailrec
// import com.amazonaws.auth._, profile._
// import ohnosequences.awstools.regions.Region._
// import ohnosequences.awstools.ec2.InstanceType._
//
//
// class BundlesTest extends FunSuite with ParallelTestExecution {
//
//   val ec2 = EC2.create(new ProfileCredentialsProvider("default"))
//   val user = era7.project.users.aalekhin
//
//   def launchAndWait(ec2: EC2, name: String, specs: InstanceSpecs): List[ec2.Instance] = {
//     ec2.runInstances(1, specs) flatMap { inst =>
//       def checkStatus: String = inst.getTagValue("statika-status").getOrElse("...")
//
//       val id = inst.getInstanceId()
//       def printStatus(s: String) = println(name+" ("+id+"): "+s)
//
//       inst.createTag(Ec2Tag("Name", name))
//       printStatus("launched")
//
//       while(checkStatus != "preparing") { Thread sleep 2000 }
//       printStatus("url: "+inst.getPublicDNS().getOrElse("..."))
//
//       @tailrec def waitForSuccess(previous: String): String = {
//         val current = checkStatus
//         if(current == "failure" || current == "success") {
//           printStatus(current)
//           current
//         } else {
//           if (current != previous) printStatus(current)
//           Thread sleep 3000
//           waitForSuccess(current)
//         }
//       }
//
//       if (waitForSuccess(checkStatus) == "success") Some(inst) else None
//     }
//   }
//
//   ignore("testing rna16s bundle") {
//     import rna16sTest._
//
//     val specs = rna16sCompat.instanceSpecs(
//       instanceType = m3.medium,
//       user.awsAccount.keypair.name,
//       Some(era7.aws.roles.projects.name)
//     )
//
//     val instances = launchAndWait(ec2, rna16sCompat.name, specs)
//     assert{ instances.length == 1 }
//   }
//
//   ignore("testing gis filtering bundle") {
//     import filtering._
//
//     val specs = filterGIsCompat.instanceSpecs(
//       instanceType = m3_large,
//       user.awsAccount.keypair.name,
//       Some(era7.aws.roles.projects.name)
//     )
//
//     val instances = launchAndWait(ec2, filterGIsCompat.name, specs)
//     assert{ instances.length == 1 }
//   }
//
//   ignore("testing blast bundle") {
//     import ohnosequences.mg7.loquats.blast.blastDataProcessing._
//
//     val specs = blastCompat.instanceSpecs(
//       instanceType = m3.medium,
//       user.awsAccount.keypair.name,
//       Some(era7.aws.roles.projects.name)
//     )
//
//     val instances = launchAndWait(ec2, blastCompat.name, specs)
//     // instances.foreach{ _.terminate }
//     assert{ instances.length == 1 }
//   }
//
// }

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: ../../main/scala/metagenomica/bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: ../../main/scala/metagenomica/bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: ../../main/scala/metagenomica/bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: ../../main/scala/metagenomica/bundles/blast.scala.md
[main/scala/metagenomica/bundles/filterGIs.scala]: ../../main/scala/metagenomica/bundles/filterGIs.scala.md
[main/scala/metagenomica/bundles/flash.scala]: ../../main/scala/metagenomica/bundles/flash.scala.md
[main/scala/metagenomica/bundles/referenceDB.scala]: ../../main/scala/metagenomica/bundles/referenceDB.scala.md
[main/scala/metagenomica/bundles/referenceMap.scala]: ../../main/scala/metagenomica/bundles/referenceMap.scala.md
[main/scala/metagenomica/data.scala]: ../../main/scala/metagenomica/data.scala.md
[main/scala/metagenomica/dataflow.scala]: ../../main/scala/metagenomica/dataflow.scala.md
[main/scala/metagenomica/dataflows/noFlash.scala]: ../../main/scala/metagenomica/dataflows/noFlash.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: ../../main/scala/metagenomica/dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../../main/scala/metagenomica/loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../../main/scala/metagenomica/loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../../main/scala/metagenomica/loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../../main/scala/metagenomica/loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../../main/scala/metagenomica/loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../../main/scala/metagenomica/loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../../main/scala/metagenomica/package.scala.md
[main/scala/metagenomica/parameters.scala]: ../../main/scala/metagenomica/parameters.scala.md
[test/scala/bundles.scala]: bundles.scala.md
[test/scala/lca.scala]: lca.scala.md
[test/scala/metagenomica/pipeline.scala]: metagenomica/pipeline.scala.md