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

  def lookup[A, B](a: A, m: Map[A, B]): (A, B) = a -> m.apply(a)


  type BlastArgumentsVals =
    (db.type    := db.Raw)    ::
    (query.type := query.Raw) ::
    (out.type   := out.Raw)   ::
    *[AnyDenotation]


  // We set here all options explicitly
  val defaultBlastnOptions: blastn.Options := blastn.OptionsVals =
    blastn.options(
      /* This actually depends on the workers instance type */
      num_threads(4) ::
      blastn.task(blastn.blastn) ::
      evalue(BigDecimal(1E-100)) ::
      /* We're going to use all hits to do global sample-coherent assignment. But not now, so no reason for this to be huge */
      max_target_seqs(150) ::
      strand(Strands.both) ::
      word_size(46) ::
      show_gis(false) ::
      ungapped(false) ::
      penalty(-2)  ::
      reward(1) ::
      /* 95% is a reasonable minimum. If it does not work, be more stringent with read preprocessing */
      perc_identity(95.0) ::
      *[AnyDenotation]
    )
}
