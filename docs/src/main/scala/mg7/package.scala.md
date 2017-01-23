
```scala
package ohnosequences

import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.loquat._
import ohnosequences.blast.api._

package object mg7 {

  type ID = String
  type Taxon  = ID
  type Taxa   = Seq[Taxon]
  type ReadID = ID
  type NodeID = ID

  type TitanTaxon = ncbitaxonomy.TitanNode

  type LCA = ncbitaxonomy.titan.ncbiTitanTaxon
  type BBH = ncbitaxonomy.titan.ncbiTitanTaxon

  type SampleID = ID
  type StepName = String

  type DataMappings[DP <: AnyDataProcessingBundle] = List[DataMapping[DP]]

  def parseInt(str: String): Option[Int] = util.Try(str.toInt).toOption
  def parseLong(str: String): Option[Long] = util.Try(str.toLong).toOption
  def parseDouble(str: String): Option[Double] = util.Try(str.toDouble).toOption

  // Used in dataMappings to get value pairs from another maps
  def lookup[A, B](a: A, m: Map[A, B]): (A, B) = a -> m.apply(a)

  implicit class TraversableOps[T](val col: Traversable[T]) extends AnyVal {
```

This method is like standard maxBy, but accumulates _all_ maximum elements

```scala
    def maximumsBy[X](f: T => X)(implicit cmp: Ordering[X]): List[T] =
      col.foldLeft(List[T]()) {
        case (a :: acc, t) if (    cmp.lt(f(t), f(a)) ) => a :: acc
        case (a :: acc, t) if ( cmp.equiv(f(t), f(a)) ) => t :: a :: acc
        // either acc is empty or t is the new maximum
        case (_, t) => List(t)
      }

    def  maximums(implicit cmp: Ordering[T]): List[T] = maximumsBy(identity[T])
  }

  implicit class SeqDoubleOps(val seq: Seq[Double]) extends AnyVal {

    def average: Double = seq.sum / seq.length
  }


  type BlastArgumentsVals =
    (db.type    := db.Raw)    ::
    (query.type := query.Raw) ::
    (out.type   := out.Raw)   ::
    *[AnyDenotation]
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