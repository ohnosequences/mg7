package ohnosequences.mg7.bundles

import ohnosequences.statika._
import ohnosequences.awstools.s3._
import ohnosequencesBundles.statika._

// import com.amazonaws.auth._, profile._
// import com.amazonaws.services.s3._
// import com.amazonaws.services.s3.model._
// import com.amazonaws.services.s3.transfer._

import com.thinkaurelius.titan.core._
import com.bio4j.titan.model.ncbiTaxonomy._
import com.bio4j.titan.util.DefaultTitanGraph
import org.apache.commons.configuration.Configuration


case object bio4jNCBITaxonomy extends AnyBio4jDist {

  lazy val s3folder: S3Folder = S3Folder("resources.ohnosequences.com", "16s/bio4j")

  lazy val configuration: Configuration = DefaultBio4jTitanConfig(dbLocation)

  // the graph; its only (direct) use is for indexes
  // FIXME: this works but still with errors, should be fixed (something about transactions)
  lazy val graph: TitanNCBITaxonomyGraph =
    new TitanNCBITaxonomyGraph(
      new DefaultTitanGraph(TitanFactory.open(configuration))
    )
}

// case object bio4jTaxonomy extends Bundle() {
//   val bucket = "resources.ohnosequences.com"
//   val key = "16s/bio4j"
//
//   lazy val destination: File = new File(".")
//   lazy val location: File = new File(destination, key)
//
//   def instructions: AnyInstructions = {
//
//     LazyTry {
//       println(s"""Dowloading
//         |from: s3://${bucket}/${key}
//         |to: ${destination.getCanonicalPath}
//         |""".stripMargin)
//
//       // val transferManager = new TransferManager(new ProfileCredentialsProvider("default"))
//       val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
//       val transfer = transferManager.downloadDirectory(bucket, key, destination)
//       transfer.waitForCompletion
//     } -&-
//     say(s"Taxonomy database was dowloaded to ${location.getCanonicalPath}")
//   }
//
//   lazy val conf: BaseConfiguration = {
//     val base = new BaseConfiguration()
//     base.setProperty("storage.directory", location.getCanonicalPath)
//     base.setProperty("storage.backend", "berkeleyje")
//     base.setProperty("storage.batch-loading", "false")
//     base.setProperty("storage.transactions", "true")
//     base.setProperty("query.fast-property", "false")
//     base.setProperty("schema.default", "none")
//     base
//   }
//
//   // the graph; its only (direct) use is for indexes
//   // FIXME: this works but still with errors, should be fixed (something about transactions)
//   lazy val graph: TitanNCBITaxonomyGraph =
//     new TitanNCBITaxonomyGraph(
//       new DefaultTitanGraph(TitanFactory.open(conf))
//     )
// }


case object bio4jBundleTest {

  import ohnosequences.statika.aws._
  import ohnosequences.awstools.ec2._
  import ohnosequences.awstools.regions.Region._

  case object bio4jTaxonomyCompat extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
    bio4jNCBITaxonomy,
    generated.metadata.mg7
  )

}
