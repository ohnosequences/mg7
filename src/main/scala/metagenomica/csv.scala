package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._

import com.github.tototoshi.csv._
import better.files._

// Some minimal CSV utils, will be replaced by a specialized library based on cosas

case object csv {

  case object UnixTSVFormat extends TSVFormat {
    override val lineTerminator: String = "\n"
  }

  case object UnixCSVFormat extends DefaultCSVFormat {
    override val lineTerminator: String = "\n"
  }

  def newWriter(file: File, append: Boolean = true): CSVWriter =
    CSVWriter.open(file.toJava, append)(UnixCSVFormat)

  def newReader(file: File): CSVReader =
    CSVReader.open(file.toJava)(UnixCSVFormat)

  // TODO: rewrite this with product types:
  case object columnNames {

    val ReadID  = "Read-ID"
    val TaxID   = "Tax-ID"
    val TaxName = "Tax-name"
    val TaxRank = "Tax-rank"
    val Count   = "Count"

    val statsHeader: List[String] = List(
      "Sample-ID",
      "Input-pairs",
      "Merged",
      "Not-merged",
      "No-Blast-hits"
    )
  }

  case class Row[Hs <: AnyProductType](
    val header: Hs,
    val values: Seq[String]
  ) {

    def toMap: Map[AnyType, String] = header.types.asList.zip(values).toMap

    def select[C <: AnyType](column: C)(implicit
      check: C isOneOf Hs#Types#AllTypes
    ): String =
      this.toMap.apply(column)
  }

  case class Reader[Hs <: AnyProductType](val header: Hs, val csvReader: CSVReader) {

    def rows: Iterator[Row[Hs]] = csvReader.iterator.map { Row(header, _) }
  }

  implicit def toCSVReader[Hs <: AnyProductType](r: Reader[Hs]): CSVReader = r.csvReader

  case object Reader {

    def apply[Hs <: AnyProductType](header: Hs, file: File): Reader[Hs] =
      Reader(header, CSVReader.open(file.toJava)(UnixCSVFormat))
  }

}
