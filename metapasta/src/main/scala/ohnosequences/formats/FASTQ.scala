package ohnosequences.formats

import ohnosequences.awstools.s3.ObjectAddress


trait Header {
  // things that we parse and want to write to the table:
  def attributesMap: Map[String, DynamoValue]

  // raw/casava/...
  val format: String

  val raw: String
  override def toString = raw

  def getRaw = raw.replaceAll("^@", "")

  // full map that we write to the table:
  def toMap: Map[String, DynamoValue] = Map(
    "headerFormat" -> DynamoValue.string(format)
    , "header" -> DynamoValue.string(raw)
  ) ++ attributesMap
}

case class RawHeader(raw: String) extends Header {
  val format = "raw"
  def attributesMap = Map()
}

// <instrument>:<run number>:<flowcell ID>:<lane>:<tile>:<x-pos>:<y-pos> <read>:<is filtered>:<control number>:<index sequence>
case class CasavaHeader(raw: String)(
  instrument: String     // Instrument ID
  , runNumber: Long        // Run number on instrument
  , flowcellID: String     // ?
  , lane: Long             // Lane number
  , tile: Long             // Tile number
  , xPos: Long             // X coordinate of cluster
  , yPos: Long             // Y coordinate of cluster
  , read: Long             // Read number. 1 can be single read or read 2 of paired-end
  , isFiltered: String     // Y if the read is filtered, N otherwise
  , controlNumber: Long    // 0 when none of the control bits are on, otherwise it is an even number
  , indexSequence: String  // Index sequence
  ) extends Header {

  val format = "casava"
  def attributesMap = Map(
    "instrument" -> DynamoValue.string(instrument)
    , "runNumber" -> DynamoValue.long(runNumber)
    , "flowcellID" -> DynamoValue.string(flowcellID)
    , "lane" -> DynamoValue.long(lane)
    , "tile" -> DynamoValue.long(tile)
    , "xPos" -> DynamoValue.long(xPos)
    , "yPos" -> DynamoValue.long(yPos)
    , "read" -> DynamoValue.long(read)
    , "isFiltered" -> DynamoValue.string(isFiltered)
    , "controlNumber" -> DynamoValue.long(controlNumber)
    , "indexSequence" -> DynamoValue.string(indexSequence)
  )

}

/* FASTQ type with header type parameter */
case class FASTQ[H <: Header](header: H, sequence: String, optHeader: String, quality: String) {
  type HeaderType = H

  override def toString = Seq(
    "@header: " + header
    , "squence: " + sequence
    , "+header: " + optHeader
    , "quality: " + quality
  ) mkString "\r"

  def sequenceSplitter(s: String): String = {
    val sb = new StringBuilder
    for (p <- s.grouped(80)) {
      sb.append(p)
      sb.append(System.lineSeparator())
    }
    sb.toString()
  }



  def toFasta: String = {
   // ">" + header.getRaw.replaceAll("\\s+", "_") + System.lineSeparator() + sequence
    ">" + header.getRaw.split("\\s+")(0) + System.lineSeparator() + sequenceSplitter(sequence)
  }

  def toFasta(additionalHeader: String): String = {
    // ">" + header.getRaw.replaceAll("\\s+", "_") + System.lineSeparator() + sequence
    ">" + header.getRaw.split("\\s+")(0) + "|" + additionalHeader + System.lineSeparator() + sequenceSplitter(sequence)
  }

  def toFastq: String = {
    "@" + header.getRaw.split("\\s+")(0)  + System.lineSeparator() + sequence + System.lineSeparator() + optHeader + System.lineSeparator() + quality

  }
}

