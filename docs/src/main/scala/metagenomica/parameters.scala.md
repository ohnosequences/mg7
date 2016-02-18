
```scala
package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._

import ohnosequences.datasets._

import ohnosequences.flash.api._

import ohnosequences.blast.api._

import scala.util.Try


trait AnyMG7Parameters {

  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults.update(
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )

  type BlastOutRec <: AnyBlastOutputRecord.For[blastn.type]
  val  blastOutRec: BlastOutRec
```

This is the number of reads in each chunk after the `split` step

```scala
  // TODO: would be nice to have Nat here
  val chunkSize: Int
}

abstract class MG7Parameters[
  BR <: AnyBlastOutputRecord.For[blastn.type]
](val readsLength: illumina.Length,
  val blastOutRec: BR,
  val chunkSize: Int = 5
// )(implicit
  // TODO: add a check for minimal set of properties in the record (like bitscore and sgi)
) extends AnyMG7Parameters {

  type BlastOutRec = BR
}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: bundles/bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: bundles/blast.scala.md
[main/scala/metagenomica/bundles/blast16s.scala]: bundles/blast16s.scala.md
[main/scala/metagenomica/bundles/flash.scala]: bundles/flash.scala.md
[main/scala/metagenomica/bundles/gis.scala]: bundles/gis.scala.md
[main/scala/metagenomica/data.scala]: data.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: package.scala.md
[main/scala/metagenomica/parameters.scala]: parameters.scala.md
[test/scala/bundles.scala]: ../../../test/scala/bundles.scala.md
[test/scala/lca.scala]: ../../../test/scala/lca.scala.md
[test/scala/metagenomica/pipeline.scala]: ../../../test/scala/metagenomica/pipeline.scala.md