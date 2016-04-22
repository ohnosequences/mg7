
```scala
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
  }

  val statsHeader = List[String](
    "Sample-ID",
    "Input-pairs",
    "Merged",
    "Not-merged",
    "No-Blast-hits"
  )

  val assignHeader = List[String](
    columnNames.ReadID,
    columnNames.TaxID,
    columnNames.TaxName,
    columnNames.TaxRank
  )

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

```




[test/scala/mg7/pipeline.scala]: ../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/lca.scala]: ../../../test/scala/mg7/lca.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: dataflows/noFlash.scala.md
[main/scala/mg7/dataflows/full.scala]: dataflows/full.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/bio4j/bundle.scala]: bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: bio4j/taxonomyTree.scala.md
[main/scala/mg7/dataflow.scala]: dataflow.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/loquats/7.stats.scala]: loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: loquats/8.summary.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md