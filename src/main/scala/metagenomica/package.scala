package ohnosequences

import ohnosequences.mg7.bio4j.taxonomyTree._
import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.blast.api._

// import com.github.tototoshi.csv._
import better.files._

package object mg7 {

  type ID = String
  type TaxID = ID
  type ReadID = ID
  type NodeID = ID

  type LCA = Option[AnyTaxonNode]
  type BBH = Option[AnyTaxonNode]

  type SampleID = ID
  type StepName = String

  def parseInt(str: String): Option[Int] = util.Try(str.toInt).toOption
  def parseDouble(str: String): Option[Double] = util.Try(str.toDouble).toOption


  type BlastArgumentsVals =
    (db.type    := db.Raw)    ::
    (query.type := query.Raw) ::
    (out.type   := out.Raw)   ::
    *[AnyDenotation]


  // We set here all options explicitly
  val defaultBlastnOptions: blastn.Options := blastn.OptionsVals =
    blastn.options(
      num_threads(1) ::
      blastn.task(blastn.blastn) ::
      evalue(BigDecimal(1E-5)) ::
      max_target_seqs(500) ::
      strand(Strands.both) ::
      word_size(28) ::
      show_gis(false) ::
      ungapped(false) ::
      penalty(-2)  ::
      reward(1) ::
      perc_identity(99.5) ::
      *[AnyDenotation]
    )
}
