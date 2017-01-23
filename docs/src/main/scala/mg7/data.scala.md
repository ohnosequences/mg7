
```scala
package ohnosequences.mg7

import ohnosequences.datasets._
import ohnosequences.cosas._, types._, klists._

case object data {

  case object pairedReads1 extends FileData("reads1")("fastq.gz")
  case object pairedReads2 extends FileData("reads2")("fastq.gz")

  case object mergedReads    extends FileData("merged")("fastq")
  case object pair1NotMerged extends FileData("pair1.not-merged")("fastq")
  case object pair2NotMerged extends FileData("pair2.not-merged")("fastq")
  case object flashHistogram extends FileData("stats")("hist")

  case object flashInput  extends DataSet(
    pairedReads1 :×:
    pairedReads2 :×:
    |[AnyData]
  )
  case object flashOutput extends DataSet(
    mergedReads :×:
    pair1NotMerged :×:
    pair2NotMerged :×:
    flashHistogram :×:
    |[AnyData]
  )

  // Reads after splitting (multiple files in a virtual S3 folder):
  case object fastaChunks extends Data("reads-chunks")

  case object splitInput  extends DataSet(mergedReads :×: |[AnyData])
  case object splitOutput extends DataSet(fastaChunks :×: |[AnyData])

  // Blast input:
  case object fastaChunk extends FileData("reads")("fastq")
  // Blast output for each chunk:
  case object blastChunk extends FileData("blast.chunk")("csv")
  case object noHitsChunk extends Data("no-blast-hits.fa")

  case object blastInput extends DataSet(
    fastaChunk :×:
    |[AnyData]
  )
  case object blastOutput extends DataSet(
    blastChunk :×:
    noHitsChunk :×:
    |[AnyData]
  )

  // Assignment output:
  case object lcaChunk extends FileData("lca.chunk")("csv")
  case object bbhChunk extends FileData("bbh.chunk")("csv")

  case object assignInput extends DataSet(
    blastChunk :×:
    |[AnyData]
  )
  case object assignOutput extends DataSet(
    lcaChunk :×:
    bbhChunk :×:
    |[AnyData]
  )

  // all output chunks together:
  case object blastChunksFolder extends Data("blast-chunks")
  case object blastNoHitsFolder extends Data("blast-no-hits")
  case object lcaChunksFolder   extends Data("lca-chunks")
  case object bbhChunksFolder   extends Data("bbh-chunks")
  // after merging chunks:
  case object blastResult extends FileData("blast")("csv")
  case object blastNoHits extends FileData("blast.no-hits")("fasta")
  case object lcaCSV      extends FileData("lca")("csv")
  case object bbhCSV      extends FileData("bbh")("csv")

  case object mergeInput extends DataSet(
    blastChunksFolder :×:
    blastNoHitsFolder :×:
    lcaChunksFolder   :×:
    bbhChunksFolder   :×:
    |[AnyData]
  )
  case object mergeOutput extends DataSet(
    blastResult :×:
    blastNoHits :×:
    lcaCSV      :×:
    bbhCSV      :×:
    |[AnyData]
  )

  // Counting output:
  class DataCounts(prefix: String) {

    case object direct extends DirectAccum("direct")
    case object accum  extends DirectAccum("accumulated")

    class DirectAccum(directaccum: String) {

      case object absolute extends AbsRel("absolute.counts")
      case object relative extends AbsRel("frequency.percentage")

      class AbsRel(absrel: String)
        extends FileData(s"${prefix}.${directaccum}.${absrel}")("csv")
    }
  }
  case object lca extends DataCounts("lca")
  case object bbh extends DataCounts("bbh")

  case object countInput extends DataSet(
    lcaCSV :×:
    bbhCSV :×:
    |[AnyData]
  )
  case object countOutput extends DataSet(
    lca.direct.absolute :×:
    lca.accum.absolute  :×:
    lca.direct.relative :×:
    lca.accum.relative  :×:
    bbh.direct.absolute :×:
    bbh.accum.absolute  :×:
    bbh.direct.relative :×:
    bbh.accum.relative  :×:
    |[AnyData]
  )

  case object sampleID extends FileData("sample-id")("txt")
  case object sampleStatsCSV extends FileData("sample.stats")("csv")

  case object statsInput extends DataSet(
    sampleID       :×:
    pairedReads1   :×:
    mergedReads    :×:
    pair1NotMerged :×:
    blastNoHits    :×:
    |[AnyData]
  )
  case object statsOutput extends DataSet(
    sampleStatsCSV :×:
    |[AnyData]
  )

  case object sampleStatsFolder extends Data("stats")
  case object summaryStatsCSV extends FileData("summary.stats")("csv")

  case object summaryInput extends DataSet(
    sampleStatsFolder :×:
    |[AnyData]
  )
  case object summaryOutput extends DataSet(
    summaryStatsCSV :×:
    |[AnyData]
  )
}

```




[main/scala/mg7/bundles.scala]: bundles.scala.md
[main/scala/mg7/configs.scala]: configs.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/defaults.scala]: defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[main/scala/mg7/pipeline.scala]: pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/fqnames.scala]: ../../../test/scala/mg7/fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: ../../../test/scala/mg7/mock/illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: ../../../test/scala/mg7/mock/pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: ../../../test/scala/mg7/PRJEB6592/PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: ../../../test/scala/mg7/referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../test/scala/mg7/taxonomy.scala.md
[test/scala/mg7/testData.scala]: ../../../test/scala/mg7/testData.scala.md
[test/scala/mg7/testDefaults.scala]: ../../../test/scala/mg7/testDefaults.scala.md