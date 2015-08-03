package metapastatest

import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.nisperon.bundles.NisperonMetadataBuilder
import ohnosequences.metapasta._
import ohnosequences.awstools.autoscaling._
import ohnosequences.nisperon._
import ohnosequences.awstools.ec2._

object mockSamples {
  val testBucket = "metapasta-test"

  val ss2 = "supermock3"
  val s2 = PairedSample(ss2, ObjectAddress(testBucket, "mock/" + ss2 + ".fastq"), ObjectAddress(testBucket, "mock/" + ss2 + ".fastq"))

  val samples = List(s2)
}

object configuration extends BlastConfiguration (
  metadataBuilder = new NisperonMetadataBuilder(new generated.metadata.metapastatest()),
  email = "museeer@gmail.com",
  mappingWorkers = Group(size = 1, max = 20, instanceType = InstanceType.T1Micro, purchaseModel = OnDemand),
  uploadWorkers = None,
  samples = mockSamples.samples,
  logging = true,
  database = NTDatabase,
  xmlOutput = true
)

object metapastatest extends Metapasta(configuration) {

  def check(test: Boolean, description: String) {
    if (!test) {
      throw new Error(description)
    }
  }

  override def undeployActions(solved: Boolean): Option[String] = {
    super.undeployActions(solved)
    //test xmls

    try {
      val xml1 = ObjectAddress(nisperonConfiguration.bucket, "map/" + mockSamples.ss2 + ".1.1/out.blast")
      val xml2 = ObjectAddress(nisperonConfiguration.bucket, "map/" + mockSamples.ss2 + ".1.2/out.blast")

      val query5 = "Query_5"
      val query6 = "Query_6"
      val query25 = "Query_25"
      val query26 = "Query_26"

      println("checking " + xml1)
      check(aws.s3.readWholeObject(xml1).contains(query25), "BLAST xml " + xml1 + " should contain " + query25)
      check(!aws.s3.readWholeObject(xml1).contains(query26), "BLAST xml " + xml1 + " shouldn't contain " + query26)
      check(aws.s3.readWholeObject(xml2).contains(query5), "BLAST xml " + xml2 + " should contain " + query5)
      check(!aws.s3.readWholeObject(xml2).contains(query6), "BLAST xml " + xml2 + " shouldn't contain " + query6)

      None

    } catch {
      case t: Throwable => {
        Some(t.getMessage)
        //handle error
      }
    }

  }

  override def additionalHandler(args: List[String]) {
    println(undeployActions(false))
  }
}
