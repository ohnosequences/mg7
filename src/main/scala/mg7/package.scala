package ohnosequences

import ohnosequences.cosas._, types._, klists._
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

    /* This method is like standard maxBy, but accumulates _all_ maximum elements */
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
