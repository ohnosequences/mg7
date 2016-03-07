
```scala
package ohnosequences.mg7

import ohnosequences.datasets._
import ohnosequences.cosas._, types._, klists._

case object data {

  // Flash:
  case object pairedReads1 extends FileData("reads1")("fastq.gz")
  case object pairedReads2 extends FileData("reads2")("fastq.gz")

  case object mergedReads extends FileData("reads")("fastq")
  case object flashStats extends FileData("stats")("txt")

  case object flashInput extends DataSet(pairedReads1 :?: pairedReads2 :?: |[AnyData])
  case object flashOutput extends DataSet(mergedReads :?: flashStats :?: |[AnyData])


  // Reads after splitting (multiple files in a virtual S3 folder):
  case object fastaChunks extends Data("reads-chunks")

  case object splitInput extends DataSet(mergedReads :?: |[AnyData])
  case object splitOutput extends DataSet(fastaChunks :?: |[AnyData])


  // Blast input:
  case object fastaChunk extends FileData("reads")("fastq")
  // Blast output for each chunk:
  case object blastChunkOut extends FileData("blast.chunk")("csv")

  case object blastInput extends DataSet(fastaChunk :?: |[AnyData])
  case object blastOutput extends DataSet(blastChunkOut :?: |[AnyData])


  // all output chunks together:
  case object blastChunks extends Data("blast-chunks")
  // after merging chunks:
  case object blastResult extends FileData("blast")("csv")

  case object mergeInput extends DataSet(blastChunks :?: |[AnyData])
  case object mergeOutput extends DataSet(blastResult :?: |[AnyData])


  // Assignment output:
  case object lcaCSV extends FileData("lca")("csv")
  case object bbhCSV extends FileData("bbh")("csv")

  case object assignmentInput extends DataSet(blastResult :?: |[AnyData])
  case object assignmentOutput extends DataSet(lcaCSV :?: bbhCSV :?: |[AnyData])


  // Counting output:
  case object lcaDirectCountsCSV extends FileData("lca.direct.counts")("csv")
  case object bbhDirectCountsCSV extends FileData("bbh.direct.counts")("csv")
  case object lcaAccumCountsCSV extends FileData("lca.accum.counts")("csv")
  case object bbhAccumCountsCSV extends FileData("bbh.accum.counts")("csv")

  case object countingInput extends DataSet(lcaCSV :?: bbhCSV :?: |[AnyData])
  case object countingOutput extends DataSet(
    lcaDirectCountsCSV :?:
    bbhDirectCountsCSV :?:
    lcaAccumCountsCSV :?:
    bbhAccumCountsCSV :?:
    |[AnyData]
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