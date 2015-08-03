package ohnosequences.metapasta

import ohnosequences.typesets._
import ohnosequences.statika._
import ohnosequences.statika.aws._
import ohnosequences.bio4j.bundles._
import ohnosequences.nisperon.bundles._
import org.clapper.avsl.Logger

class Bio4jDistributionDist(metadataBuilder: NisperonMetadataBuilder) extends AWSDistribution (
  metadata = metadataBuilder.build("bio", "bio", "."),
  ami = NisperonAMI,
  members = NCBITaxonomyDistribution :~: ∅
)

trait NodeRetriever {
  var nodeRetriever: com.ohnosequences.bio4j.titan.model.util.NodeRetrieverTitan
}

class BundleNodeRetrieverFactory extends Factory[NisperonMetadataBuilder, NodeRetriever] {

  val logger = Logger(this.getClass)


  class BundleNodeRetriever extends NodeRetriever {
    var nodeRetriever = ohnosequences.bio4j.bundles.NCBITaxonomyDistribution.nodeRetriever
  }
  override def build(context: NisperonMetadataBuilder): NodeRetriever = {
    logger.info("installing bio4j")
    println(new Bio4jDistributionDist(context).installWithDeps(ohnosequences.bio4j.bundles.NCBITaxonomyDistribution))
    new BundleNodeRetriever()
  }
}

