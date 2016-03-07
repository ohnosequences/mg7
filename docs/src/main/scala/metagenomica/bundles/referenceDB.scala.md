
```scala
package ohnosequences.mg7.bundles

import ohnosequences.statika._
import ohnosequences.loquat.utils._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


// TODO: the non-bundle part of the trait could be put in the blast-api lib
trait AnyReferenceDB extends AnyBundle {
  // Depending on the DB you use, you have to provide a Map of it's IDs to TaxID
  val idsMap: AnyReferenceIDsMap

  val name: String
  val s3Address: AnyS3Address

  val dbLocation: File
  val dbName: File
}


// Here's the default one:
case object rna16s extends Bundle() with AnyReferenceDB {
  val idsMap = filteredGIs

  val name = "era7.16S.reference.sequences.0.1.0"
  val s3Address = S3Object("resources.ohnosequences.com", s"16s/${name}.tgz")

  val archive: File  = file"${name}.tgz"
  val dbLocation: File = file"${name}"
  val dbName: File   = dbLocation / s"${name}.fasta"

  def instructions: AnyInstructions = {

    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      transferManager.download(s3Address, archive)
    } -&-
    cmd("tar")("-xvzf", archive.path.toString) -&-
    say(s"Reference database ${name} was dowloaded to ${dbLocation.path}")
  }
}

case object rnaCentral extends Bundle() with AnyReferenceDB {
  val idsMap = rnaCentralIDsMap

  val name = "rnacentral_active"

  val s3key = "rnacentral/4.0/blastdb"
  val s3Address = S3Folder("resources.ohnosequences.com", s3key)

  val dbLocation: File = s3key.toFile
  val dbName: File = dbLocation / s"${name}.fasta"

  def instructions: AnyInstructions = {

    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      transferManager.download(s3Address, file".")
    } -&-
    say(s"Reference database ${name} was dowloaded to ${dbLocation.path}")
  }
}


// case object rna16sTest {
//
//   import ohnosequences.statika.aws._
//   import ohnosequences.awstools.ec2._
//   import ohnosequences.awstools.regions.Region._
//
//   case object rna16sCompat extends Compatible(
//     amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
//     rna16s,
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