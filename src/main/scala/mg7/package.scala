package ohnosequences

import ohnosequences.mg7.bio4j.taxonomyTree._
import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.blast.api._

package object mg7 {

  type ID = String
  type Taxa = ID
  type ReadID = ID
  type NodeID = ID

  type LCA = AnyTaxonNode
  type BBH = AnyTaxonNode

  type SampleID = ID
  type StepName = String

  def parseInt(str: String): Option[Int] = util.Try(str.toInt).toOption
  def parseLong(str: String): Option[Long] = util.Try(str.toLong).toOption
  def parseDouble(str: String): Option[Double] = util.Try(str.toDouble).toOption

  // Used in dataMappings to get value pairs from another maps
  def lookup[A, B](a: A, m: Map[A, B]): (A, B) = a -> m.apply(a)

  // TODO why not create an ordering from `f` and use std library?
  def maximums[T, X](s: Iterable[T])(f: T => X)(implicit cmp: Ordering[X]): List[T] =
    s.foldLeft(List[T]()) {
      case (a :: acc, t) if (    cmp.lt(f(t), f(a)) ) => a :: acc
      case (a :: acc, t) if ( cmp.equiv(f(t), f(a)) ) => t :: a :: acc
      // either acc is empty or t is the new maximum
      case (_, t) => List(t)
    }

  def averageOf(vals: Seq[Double]): Double = vals.sum / vals.length

  type BlastArgumentsVals =
    (db.type    := db.Raw)    ::
    (query.type := query.Raw) ::
    (out.type   := out.Raw)   ::
    *[AnyDenotation]
}
