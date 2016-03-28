
```scala
package ohnosequences

import ohnosequences.mg7.bio4j.taxonomyTree._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.blast.api._

package object mg7 {

  type ID = String
  type TaxID = ID
  type ReadID = ID
  type NodeID = ID

  type LCA = Option[AnyTaxonNode]
  type BBH = Option[AnyTaxonNode]

  type SampleID = ID
  type StepName = String

  def parseInt(str: String): Option[Int] = util.Try(str.toInt).toOption

  case object columnNames {

    val ReadID = "Read-ID"
    val TaxID = "Tax-ID"
    val TaxName = "Tax-name"
    val TaxRank = "Tax-rank"
    val Count = "Count"
  }


  case object defaultBlastOutRec extends BlastOutputRecord(
      outputFields.qseqid   :?:
      outputFields.qlen     :?:
      outputFields.qstart   :?:
      outputFields.qend     :?:
      outputFields.sseqid   :?:
      outputFields.slen     :?:
      outputFields.sstart   :?:
      outputFields.send     :?:
      outputFields.bitscore :?:
      outputFields.sgi      :?:
      |[AnyOutputField]
    )

  val defaultBlastOptions: blastn.Options := blastn.OptionsVals =
    blastn.defaults.update(
      num_threads(1) ::
      word_size(42) ::
      max_target_seqs(10) ::
      evalue(0.001) ::
      blastn.task(blastn.megablast) ::
      *[AnyDenotation]
    )

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