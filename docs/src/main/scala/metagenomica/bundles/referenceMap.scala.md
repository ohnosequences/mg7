
```scala
package ohnosequences.mg7.bundles

import ohnosequences.mg7._

import ohnosequences.statika._
import ohnosequences.loquat.utils._
import ohnosequences.awstools.s3.S3Object

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._
import com.github.tototoshi.csv._


trait AnyReferenceIDsMap extends AnyBundle {
  val name: String
  val s3Address: S3Object

  lazy val destination: File = File(name)

  def instructions: AnyInstructions = {

    LazyTry {
      // val transferManager = new TransferManager(new ProfileCredentialsProvider("default"))
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      transferManager.download(s3Address, destination)
    } -&-
    say(s"Reference IDs mapping ${name} was dowloaded and unpacked to ${destination.path}")
  }

  def mapping: Map[ID, TaxID] = {
    // Reading TSV file with mapping something-taxId
    val tsvReader: CSVReader = CSVReader.open( destination.toJava )(new TSVFormat {})

    val idsMap: Map[ID, TaxID] = tsvReader.iterator.map { row =>
      row(0) -> row(1)
    }.toMap

    tsvReader.close

    idsMap
  }
}
```

This is what will be used in the assignment loquat

```scala
case object filteredGIs extends Bundle() with AnyReferenceIDsMap {
  val name = "gi_taxid_filtered.csv"
  val s3Address = S3Object("resources.ohnosequences.com", s"16s/${name}")
}

case object rnaCentralIDsMap extends Bundle() with AnyReferenceIDsMap {
  val name = "id2taxa.filtered.4.0.tsv"
  val s3Address = S3Object("resources.ohnosequences.com", s"rnacentral/4.0/${name}")
}

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