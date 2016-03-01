package ohnosequences

import ohnosequences.cosas._, types._, klists._
import ohnosequences.blast.api._

package object mg7 {

  type ID = String
  type TaxID = ID
  type ReadID = ID
  type NodeID = ID

  type LCA = Option[NodeID]
  type BBH = Option[NodeID]

  type SampleID = ID
  type StepName = String

  def parseInt(str: String): Option[Int] = util.Try(str.toInt).toOption

  val defaultBlastOptions: blastn.Options := blastn.OptionsVals =
    blastn.defaults.update(
      num_threads(1) ::
      word_size(42) ::
      max_target_seqs(10) ::
      evalue(0.001) ::
      blastn.task(blastn.megablast) ::
      *[AnyDenotation]
    )

}
