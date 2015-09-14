package ohnosequences.metagenomica.tests

import ohnosequences.metagenomica.bundles._

import ohnosequences.statika._, aws._, bundles._

import ohnosequences.awstools.ec2._
import ohnosequences.awstools.ec2.{Tag => Ec2Tag}
import java.io._
import org.scalatest._
import scala.annotation.tailrec
import com.amazonaws.auth._, profile._
import ohnosequences.awstools.regions.Region._
import ohnosequences.awstools.ec2.InstanceType._


class BundlesTest extends FunSuite with ParallelTestExecution {

  val ec2 = EC2.create(new ProfileCredentialsProvider("default"))
  val user = era7.project.users.aalekhin

  def launchAndWait(ec2: EC2, name: String, specs: InstanceSpecs): List[ec2.Instance] = {
    ec2.runInstances(1, specs) flatMap { inst =>
      def checkStatus: String = inst.getTagValue("statika-status").getOrElse("...")

      val id = inst.getInstanceId()
      def printStatus(s: String) = println(name+" ("+id+"): "+s)

      inst.createTag(Ec2Tag("Name", name))
      printStatus("launched")

      while(checkStatus != "preparing") { Thread sleep 2000 }
      printStatus("url: "+inst.getPublicDNS().getOrElse("..."))

      @tailrec def waitForSuccess(previous: String): String = {
        val current = checkStatus
        if(current == "failure" || current == "success") {
          printStatus(current)
          current
        } else {
          if (current != previous) printStatus(current)
          Thread sleep 3000
          waitForSuccess(current)
        }
      }

      if (waitForSuccess(checkStatus) == "success") Some(inst) else None
    }
  }

  ignore("testing blast16s bundle") {
    import blast16sTest._

    val specs = blast16sCompat.instanceSpecs(
      instanceType = m3_medium,
      user.awsAccount.keypair.name,
      Some(era7.aws.roles.projects.name)
    )

    val instances = launchAndWait(ec2, blast16sCompat.name, specs)
    assert{ instances.length == 1 }
  }

  ignore("testing gis filtering bundle") {
    import filtering._

    val specs = filterGIsCompat.instanceSpecs(
      instanceType = m3_large,
      user.awsAccount.keypair.name,
      Some(era7.aws.roles.projects.name)
    )

    val instances = launchAndWait(ec2, filterGIsCompat.name, specs)
    assert{ instances.length == 1 }
  }

  ignore("testing blast bundle") {
    import ohnosequences.metagenomica.loquats.blast.blastDataProcessing._

    val specs = blastCompat.instanceSpecs(
      instanceType = m3_medium,
      user.awsAccount.keypair.name,
      Some(era7.aws.roles.projects.name)
    )

    val instances = launchAndWait(ec2, blastCompat.name, specs)
    // instances.foreach{ _.terminate }
    assert{ instances.length == 1 }
  }

}
