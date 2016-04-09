package ohnosequences.mg7

import com.github.tototoshi.csv._
import better.files._

// Some minimal CSV utils, will be replaced by a specialized library based on cosas

case object csv {

  case object UnixCSVFormat extends DefaultCSVFormat {
    override val lineTerminator: String = "\n"
  }

  def newWriter(file: File, append: Boolean = true): CSVWriter =
    CSVWriter.open(file.toJava, append)(UnixCSVFormat)

  def newReader(file: File): CSVReader =
    CSVReader.open(file.toJava)(UnixCSVFormat)

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
      "No-Blast-hits",
      "LCA-not-assigned",
      "BBH-not-assigned"
    )
  }

  case class Row[H](
    val header: Seq[H],
    val values: Seq[String]
  ) {

    def toMap: Map[H, String] = header.zip(values).toMap

    def select(column: H): String = this.toMap.apply(column)
  }
}
