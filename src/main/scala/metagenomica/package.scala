package ohnosequences

import scala.util.Try

package object metagenomica {

  type ID = String
  type GI = ID
  type TaxID = ID
  type ReadID = ID
  type NodeID = ID

  type LCA = Option[NodeID]
  type BBH = Option[NodeID]


  def parseInt(str: String): Option[Int] = Try(str.toInt).toOption

}
