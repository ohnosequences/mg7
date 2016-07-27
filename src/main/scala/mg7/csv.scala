package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._
import com.github.tototoshi.csv._
import better.files._

// Some minimal CSV utils, will be replaced by a specialized library based on cosas
case object csv {

  case object UnixCSVFormat extends DefaultCSVFormat {

    override val lineTerminator: String =
      "\n"
  }

  def newWriter(file: File, append: Boolean = true): CSVWriter =
    CSVWriter.open(file.toJava, append)(UnixCSVFormat)

  def newReader(file: File): CSVReader =
    CSVReader.open(file.toJava)(UnixCSVFormat)


  abstract class Column(lbl: String) extends Type[String](lbl) { col: Singleton => }

  case object columns {

    case object Lineage extends Column("Lineage")
    case object ReadID  extends Column("Read-ID")
    case object Taxa    extends Column("Taxa")
    case object TaxName extends Column("Tax-name")
    case object TaxRank extends Column("Tax-rank")
    case object Count   extends Column("Count")
    case object Pident  extends Column("Pident")

    case object SampleID    extends Column("Sample-ID")
    case object InputPairs  extends Column("Input-pairs")
    case object Merged      extends Column("Merged")
    case object NotMerged   extends Column("Not-merged")
    case object NoBlasthits extends Column("No-Blast-hits")
  }

  // TODO: make these values configurable (see #31)
  val statsColumns =
    columns.SampleID    :×:
    columns.InputPairs  :×:
    columns.Merged      :×:
    columns.NotMerged   :×:
    columns.NoBlasthits :×:
    |[Column]

  // TODO: make these values configurable (see #31)
  val assignColumns =
    columns.ReadID  :×:
    columns.Taxa    :×:
    columns.TaxName :×:
    columns.TaxRank :×:
    columns.Pident  :×:
    |[Column]


  implicit class productTypeOps[P <: AnyProductType](val p: P) extends AnyVal {

    def labels: Seq[String] = p.types.asList.map { _.label }
  }


  case class Row[Cs <: AnyProductType](val columns: Cs)(val values: String*) {

    def toMap: Map[AnyType, String] = columns.types.asList.zip(values).toMap

    def select[C <: AnyType](column: C)(implicit
      check: C isOneOf Cs#Types#AllTypes
    ): String =
      this.toMap.apply(column)
  }

  case object Row {

    def apply[Cs <: AnyProductType](vs: AnyDenotation.Of[Cs]): Row[Cs] =
      Row(vs.tpe)(vs.value.asList.map(_.value.toString): _*)
  }

  case class Reader[Cs <: AnyProductType](val columns: Cs, val csvReader: CSVReader) {

    def rows: Iterator[Row[Cs]] = csvReader.iterator.map { vs => Row(columns)(vs: _*) }
  }

  implicit def toCSVReader[Cs <: AnyProductType](r: Reader[Cs]): CSVReader = r.csvReader

  case object Reader {

    def apply[Cs <: AnyProductType](columns: Cs, file: File): Reader[Cs] =
      Reader(columns, CSVReader.open(file.toJava)(UnixCSVFormat))
  }
}
