package ohnosequences.metagenomica

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
  case object readsChunks extends Data("reads-chunks")

  case object splitInput extends DataSet(mergedReads :×: |[AnyData])
  case object splitOutput extends DataSet(readsChunks :×: |[AnyData])


  // Blast input:
  case object readsChunk extends FileData("reads")("fastq")
  // Blast output for each chunk:
  case object blastChunkOut extends FileData("blast.chunk")("csv")

  case object blastInput extends DataSet(readsChunk :×: |[AnyData])
  case object blastOutput extends DataSet(blastChunkOut :×: |[AnyData])


  // all output chunks together:
  case object blastChunks extends Data("blast-chunks")
  // after merging chunks:
  case object blastResult extends FileData("blast")("csv")

  case object mergeInput extends DataSet(blastChunks :×: |[AnyData])
  case object mergeOutput extends DataSet(blastResult :×: |[AnyData])


  // Assignment output:
  case object lcaCSV extends FileData("lca")("csv")
  case object bbhCSV extends FileData("bbh")("csv")

  case object assignmentInput extends DataSet(blastResult :×: |[AnyData])
  case object assignmentOutput extends DataSet(lcaCSV :×: bbhCSV :×: |[AnyData])


  // Counting output:
  case object lcaCountsCSV extends FileData("lca.counts")("csv")
  case object bbhCountsCSV extends FileData("bbh.counts")("csv")

  case object countingInput extends DataSet(lcaCSV :×: bbhCSV :×: |[AnyData])
  case object countingOutput extends DataSet(lcaCountsCSV :×: bbhCountsCSV :×: |[AnyData])

}
