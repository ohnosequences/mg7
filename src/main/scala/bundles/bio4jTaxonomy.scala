package ohnosequences.metagenomica.bundles

import ohnosequences.statika._, bundles._, instructions._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._, profile._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.transfer._

import java.io.File

import com.thinkaurelius.titan.core._
import com.bio4j.titan.model.ncbiTaxonomy._
import com.bio4j.titan.util.DefaultTitanGraph
import org.apache.commons.configuration.BaseConfiguration


case object bio4jTaxonomy extends Bundle() {

  // val region = "eu-west-1"
  val bucket = "resources.ohnosequences.com"
  val key = "16s/bio4j"

  lazy val destination: File = new File(".")
  lazy val location: File = new File(destination, key)

  def instructions: AnyInstructions = {

    LazyTry {
      println(s"""Dowloading
        |from: s3://${bucket}/${key}
        |to: ${destination.getCanonicalPath}
        |""".stripMargin)

      // val transferManager = new TransferManager(new ProfileCredentialsProvider("default"))
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      val transfer = transferManager.downloadDirectory(bucket, key, destination)
      transfer.waitForCompletion
    } -&-
    say(s"Taxonomy database was dowloaded to ${location.getCanonicalPath}")
  }

  lazy val conf: BaseConfiguration = {
    val base = new BaseConfiguration()
    base.setProperty("storage.directory", location)
    base.setProperty("storage.backend", "berkeleyje")
    base
  }

  // the graph; its only (direct) use is for indexes
  lazy val graph: TitanNCBITaxonomyGraph =
    new TitanNCBITaxonomyGraph(
      new DefaultTitanGraph(TitanFactory.open(conf))
    )
  // lazy val byId = graph.nCBITaxonIdIndex
}


case object bio4jBundleTest {

  import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._
  import ohnosequences.awstools.regions.Region._

  case object bio4jTaxonomyCompat extends Compatible(
    amzn_ami_64bit(Ireland, Virtualization.HVM)(1),
    bio4jTaxonomy,
    generated.metadata.Metagenomica
  )

}
