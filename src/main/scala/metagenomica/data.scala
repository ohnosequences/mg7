package ohnosequences.mg7

import ohnosequences.datasets._
import ohnosequences.cosas._, types._, klists._

case object data {

  // Flash:
  case object pairedReads1 extends FileData("reads1")("fastq.gz")
  case object pairedReads2 extends FileData("reads2")("fastq.gz")

  case object mergedReads extends FileData("reads")("fastq")
  case object flashStats extends FileData("stats")("txt")

  case object flashInput extends DataSet(pairedReads1 :×: pairedReads2 :×: |[AnyData])
  case object flashOutput extends DataSet(mergedReads :×: flashStats :×: |[AnyData])


  // Reads after splitting (multiple files in a virtual S3 folder):
  case object fastaChunks extends Data("reads-chunks")
  case object mergedReadsNumber extends Data("merged-reads-number")

  case object splitInput extends DataSet(mergedReads :×: |[AnyData])
  case object splitOutput extends DataSet(fastaChunks :×: mergedReadsNumber :×: |[AnyData])


  // Blast input:
  case object fastaChunk extends FileData("reads")("fastq")
  // Blast output for each chunk:
  case object blastChunkOut extends FileData("blast.chunk")("csv")
  case object noHitsHeaders extends Data("no-blast-hits")

  case object blastInput extends DataSet(fastaChunk :×: |[AnyData])
  case object blastOutput extends DataSet(
    blastChunkOut :×:
    noHitsHeaders :×:
    |[AnyData]
  )


  // all output chunks together:
  case object blastChunksFolder extends Data("blast-chunks")
  case object blastNoHitsFolder extends Data("blast-chunks")
  // after merging chunks:
  case object blastResult extends FileData("blast")("csv")
  case object blastNoHits extends Data("blast.no-hits")

  case object mergeInput extends DataSet(
    blastChunksFolder :×:
    blastNoHitsFolder :×: 
    |[AnyData]
  )
  case object mergeOutput extends DataSet(
    blastResult :×:
    blastNoHits :×:
    |[AnyData]
  )


  // Assignment output:
  case object lcaCSV extends FileData("lca")("csv")
  case object bbhCSV extends FileData("bbh")("csv")

  case object assignmentInput extends DataSet(blastResult :×: |[AnyData])
  case object assignmentOutput extends DataSet(lcaCSV :×: bbhCSV :×: |[AnyData])


  // Counting output:
  case object lcaDirectCountsCSV extends FileData("lca.direct.counts")("csv")
  case object lcaAccumCountsCSV extends FileData("lca.accum.counts")("csv")
  case object lcaDirectFreqCountsCSV extends FileData("lca.direct.frequency.counts")("csv")
  case object lcaAccumFreqCountsCSV extends FileData("lca.accum.frequency.counts")("csv")

  case object bbhDirectCountsCSV extends FileData("bbh.direct.counts")("csv")
  case object bbhAccumCountsCSV extends FileData("bbh.accum.counts")("csv")
  case object bbhDirectFreqCountsCSV extends FileData("bbh.direct.frequency.counts")("csv")
  case object bbhAccumFreqCountsCSV extends FileData("bbh.accum.frequency.counts")("csv")

  case object countingInput extends DataSet(
    lcaCSV :×:
    bbhCSV :×:
    mergedReadsNumber :×:
    |[AnyData]
  )
  case object countingOutput extends DataSet(
    lcaDirectCountsCSV :×:
    lcaAccumCountsCSV :×:
    lcaDirectFreqCountsCSV :×:
    lcaAccumFreqCountsCSV :×:
    bbhDirectCountsCSV :×:
    bbhAccumCountsCSV :×:
    bbhDirectFreqCountsCSV :×:
    bbhAccumFreqCountsCSV :×:
    |[AnyData]
  )

}
