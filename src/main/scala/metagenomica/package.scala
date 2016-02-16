package ohnosequences

package object mg7 {

  type ID = String
  type GI = ID
  type TaxID = ID
  type ReadID = ID
  type NodeID = ID

  type LCA = Option[NodeID]
  type BBH = Option[NodeID]

  type SampleID = ID
  type StepName = String

  def parseInt(str: String): Option[Int] = util.Try(str.toInt).toOption

}
