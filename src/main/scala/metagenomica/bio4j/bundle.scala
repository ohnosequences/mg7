package ohnosequences.mg7.bio4j

import ohnosequences.statika._
import ohnosequences.awstools.s3._
import ohnosequencesBundles.statika._

import com.thinkaurelius.titan.core._
import com.bio4j.titan.model.ncbiTaxonomy._
import com.bio4j.titan.util.DefaultTitanGraph
import org.apache.commons.configuration.Configuration


case object taxonomyBundle extends AnyBio4jDist {

  lazy val s3folder: S3Folder = S3Folder("resources.ohnosequences.com", "16s/bio4j-taxonomy/")

  lazy val configuration: Configuration = DefaultBio4jTitanConfig(dbLocation)

  // the graph; its only (direct) use is for indexes
  // FIXME: this works but still with errors, should be fixed (something about transactions)
  lazy val graph: TitanNCBITaxonomyGraph =
    new TitanNCBITaxonomyGraph(
      new DefaultTitanGraph(TitanFactory.open(configuration))
    )
}


// case object bio4jBundleTest {
//
//   import ohnosequences.statika.aws._
//   import ohnosequences.awstools.ec2._
//   import ohnosequences.awstools.regions.Region._
//
//   case object bio4jTaxonomyCompat extends Compatible(
//     amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
//     bio4jNCBITaxonomy,
//     generated.metadata.mg7
//   )
//
// }
