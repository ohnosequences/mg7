package ohnosequences.metagenomica

import ohnosequences.datasets._, dataSets._, fileType._

case object data {

  case object fastq extends DefaultFileType
  // many files in a virtual S3 folder:
  case object ChunksDataType extends AnyDataType { val label = "chunks" }


  // Flash input:
  case object pairedReads1 extends FileData("reads1.fastq")(gz)
  case object pairedReads2 extends FileData("reads2.fastq")(gz)

  // Flash output:
  case object mergedReads extends FileData("reads")(fastq)
  case object flashStats extends FileData("stats")(txt)

  // Reads after splitting:
  case object readsChunks extends Data(ChunksDataType, "reads-chunks")

  // Blast input:
  case object readsChunk extends FileData("reads")(fastq)
  // Blast output: (for each chunk)
  case object blastChunkOut extends FileData("blast.chunk")(csv)

  // Blast output: (all output chunks together)
  case object blastChunks extends Data(ChunksDataType, "blast-chunks")
  // Blast output: (after merging chunks)
  case object blastResult extends FileData("blast")(csv)

  // Assignment output:
  case object lcaCSV extends FileData("lca")(csv)
  case object bbhCSV extends FileData("bbh")(csv)

  // Counting output:
  case object lcaCountsCSV extends FileData("lca.counts")(csv)
  case object bbhCountsCSV extends FileData("bbh.counts")(csv)

}
