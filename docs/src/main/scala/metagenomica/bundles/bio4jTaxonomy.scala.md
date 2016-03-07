
```scala
package ohnosequences.mg7.bundles

import ohnosequences.statika._
import ohnosequences.awstools.s3._
import ohnosequencesBundles.statika._

import com.thinkaurelius.titan.core._
import com.bio4j.titan.model.ncbiTaxonomy._
import com.bio4j.titan.util.DefaultTitanGraph
import org.apache.commons.configuration.Configuration


case object bio4jNCBITaxonomy extends AnyBio4jDist {

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

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: ../bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: ../bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: blast.scala.md
[main/scala/metagenomica/bundles/filterGIs.scala]: filterGIs.scala.md
[main/scala/metagenomica/bundles/flash.scala]: flash.scala.md
[main/scala/metagenomica/bundles/referenceDB.scala]: referenceDB.scala.md
[main/scala/metagenomica/bundles/referenceMap.scala]: referenceMap.scala.md
[main/scala/metagenomica/data.scala]: ../data.scala.md
[main/scala/metagenomica/dataflow.scala]: ../dataflow.scala.md
[main/scala/metagenomica/dataflows/noFlash.scala]: ../dataflows/noFlash.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: ../dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../package.scala.md
[main/scala/metagenomica/parameters.scala]: ../parameters.scala.md
[test/scala/bundles.scala]: ../../../../test/scala/bundles.scala.md
[test/scala/lca.scala]: ../../../../test/scala/lca.scala.md
[test/scala/metagenomica/pipeline.scala]: ../../../../test/scala/metagenomica/pipeline.scala.md