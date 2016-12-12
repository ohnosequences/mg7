
```scala
package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._
import com.github.tototoshi.csv._
import better.files._

// Some minimal CSV utils, will be replaced by a specialized library based on cosas
case object csv {

  sealed class Column(lbl: String) extends Type[String](lbl)

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

    case object AveragePident extends Column("Average-Pident")
  }

  // TODO: make these values configurable (see #31)
  case object refDB {
    import csv.columns._

    val columns =
      ReadID :×:
      Taxa   :×:
      |[Column]
  }


  case object stats {
    import csv.columns._

    val columns =
      SampleID    :×:
      InputPairs  :×:
      Merged      :×:
      NotMerged   :×:
      NoBlasthits :×:
      |[Column]
  }

  // TODO: make these values configurable (see #31)
  case object assignment {
    import csv.columns._

    val columns =
      ReadID  :×:
      Taxa    :×:
      TaxName :×:
      TaxRank :×:
      Pident  :×:
      |[Column]

    type Columns =
      ReadID.type  :×:
      Taxa.type    :×:
      TaxName.type :×:
      TaxRank.type :×:
      Pident.type  :×:
      |[Column]
  }

  // TODO: this is all quite clumsy, but will be improved
  case object counts {
    import csv.columns._

    val columns =
      Lineage       :×:
      Taxa          :×:
      TaxRank       :×:
      TaxName       :×:
      Count         :×:
      AveragePident :×:
      |[Column]

    type Columns = // columns.type
      Lineage.type       :×:
      Taxa.type          :×:
      TaxRank.type       :×:
      TaxName.type       :×:
      Count.type         :×:
      AveragePident.type :×:
      |[Column]

    def header(countsType: String): Columns#Raw =
      Lineage(Lineage.label)             ::
      Taxa(Taxa.label)                   ::
      TaxRank(TaxRank.label)             ::
      TaxName(TaxName.label)             ::
      Count(countsType)                  ::
      AveragePident(AveragePident.label) ::
      *[AnyDenotation]

    // case object direct {
    //   val absolute  = header("direct.absolute.counts")
    //   val frequency = header("direct.frequency.counts")
    // }
    // case object accum {
    //   val absolute  = header("accum.absolute.percentage")
    //   val frequency = header("accum.frequency.percentage")
    // }
  }


  implicit class productTypeOps[P <: AnyProductType](val p: P) extends AnyVal {

    def labels: Seq[String] = p.types.asList.map { _.label }
  }


  case class Row[Cs <: AnyProductType](val columns: Cs, val values: Seq[String]) {

    def toMap: Map[AnyType, String] = columns.types.asList.zip(values).toMap

    def select[C <: AnyType](column: C)(implicit
      check: C isOneOf Cs#Types#AllTypes
    ): String =
      this.toMap.apply(column)
  }

  case object Row {

    def apply[Cs <: AnyProductType](cs: Cs)(vs: Cs#Raw): Row[Cs] =
      Row(cs, vs.asList.map(_.value.toString))
  }


  case object UnixCSVFormat extends DefaultCSVFormat {
    override val lineTerminator: String = "\n"
  }

  // NOTE: this is a simple wrapper for the tototoshi CSVReader to work with the Row type
  case class Reader[Cs <: AnyProductType](val columns: Cs)(val file: File) {

    private lazy val csvReader = CSVReader.open(file.toJava)(UnixCSVFormat)
    def close(): Unit = csvReader.close()

    def rows: Iterator[Row[Cs]] = csvReader.iterator.map { vs => Row(columns, vs) }
  }

  // NOTE: this is a simple wrapper for the tototoshi CSVWriter to work with the Row type
  case class Writer[Cs <: AnyProductType](val columns: Cs)(val file: File, val append: Boolean = true) {

    private lazy val csvWriter = CSVWriter.open(file.toJava, append)(UnixCSVFormat)
    def close(): Unit = csvWriter.close()

    def addRow(row: Row[Cs]): Unit = csvWriter.writeRow(row.values)
    def addVals(vs: Cs#Raw):  Unit = csvWriter.writeRow(Row(columns)(vs).values)

    def writeHeader(): Unit = csvWriter.writeRow(columns.labels)
  }
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