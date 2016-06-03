
```scala
package ohnosequences.mg7

import ohnosequences.statika._
import ohnosequences.loquat.utils._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


// TODO: the non-bundle part of the trait could be put in the blast-api lib
trait AnyReferenceDB extends AnyBundle {
  val name: String
  val blastDBS3:  S3Folder
  val id2taxasS3: S3Object

  lazy val blastDB:     File = blastDBS3.key.toFile
  lazy val blastDBName: File = blastDB / s"${name.stripSuffix(".fasta")}.fasta"
  lazy val id2taxas:    File = file"${name}.id2taxas.tsv"

  def instructions: AnyInstructions = {

    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

      transferManager.download(blastDBS3, file".")
      transferManager.download(id2taxasS3, id2taxas)
    } -&-
    say(s"Reference database ${name} was dowloaded to ${blastDB.path}")
  }
}


abstract class ReferenceDB(
  val name: String,
  val blastDBS3:  S3Folder,
  val id2taxasS3: S3Object
) extends Bundle() with AnyReferenceDB

```




[main/scala/mg7/bio4j/bundle.scala]: bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: bio4j/taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/dataflow.scala]: dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[main/scala/mg7/referenceDB.scala]: referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/lca.scala]: ../../../test/scala/mg7/lca.scala.md
[test/scala/mg7/pipeline.scala]: ../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../test/scala/mg7/taxonomy.scala.md