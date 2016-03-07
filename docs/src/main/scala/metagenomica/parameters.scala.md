
```scala
package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._

import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api._

sealed trait SplitInputFormat
case object FastaInput extends SplitInputFormat
case object FastQInput extends SplitInputFormat

trait AnyMG7Parameters {

  val outputS3Folder: (SampleID, StepName) => S3Folder
```

Flash parameters

```scala
  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults.update(
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )
```

Split parameters
This is the number of reads in each chunk after the `split` step

```scala
  // TODO: would be nice to have Nat here
  val splitChunkSize: Int
  val splitInputFormat: SplitInputFormat
```

BLAST parameters

```scala
  type BlastOutRec <: AnyBlastOutputRecord.For[blastn.type]
  val  blastOutRec: BlastOutRec

  val blastOptions: blastn.Options := blastn.OptionsVals

  val referenceDB: bundles.AnyReferenceDB
}

abstract class MG7Parameters[
  BR <: AnyBlastOutputRecord.For[blastn.type]
](
  val outputS3Folder: (SampleID, StepName) => S3Folder,
  val readsLength: illumina.Length,
  val splitInputFormat: SplitInputFormat = FastQInput,
  val blastOutRec: BR = defaultBlastOutRec,
  val blastOptions: blastn.Options := blastn.OptionsVals = defaultBlastOptions,
  val splitChunkSize: Int = 10,
  val referenceDB: bundles.AnyReferenceDB = bundles.rnaCentral
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
[main/scala/metagenomica/bundles/filterGIs.scala]: bundles/filterGIs.scala.md
[main/scala/metagenomica/bundles/flash.scala]: bundles/flash.scala.md
[main/scala/metagenomica/bundles/referenceDB.scala]: bundles/referenceDB.scala.md
[main/scala/metagenomica/bundles/referenceMap.scala]: bundles/referenceMap.scala.md
[main/scala/metagenomica/data.scala]: data.scala.md
[main/scala/metagenomica/dataflow.scala]: dataflow.scala.md
[main/scala/metagenomica/dataflows/noFlash.scala]: dataflows/noFlash.scala.md
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