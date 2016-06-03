
```scala
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

```




[main/scala/mg7/bio4j/bundle.scala]: bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: ../csv.scala.md
[main/scala/mg7/data.scala]: ../data.scala.md
[main/scala/mg7/dataflow.scala]: ../dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: ../dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: ../dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: ../loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: ../loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: ../package.scala.md
[main/scala/mg7/parameters.scala]: ../parameters.scala.md
[main/scala/mg7/referenceDB.scala]: ../referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/lca.scala]: ../../../../test/scala/mg7/lca.scala.md
[test/scala/mg7/pipeline.scala]: ../../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../../test/scala/mg7/taxonomy.scala.md