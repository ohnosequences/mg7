package ohnosequences

import ohnosequences.mg7.bio4j.taxonomyTree._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.blast.api._

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

  case object columnNames {

    val ReadID = "Read-ID"
    val TaxID = "Tax-ID"
    val TaxName = "Tax-name"
    val TaxRank = "Tax-rank"
    val Count = "Count"
  }


  type BlastArgumentsVals =
    (db.type    := db.Raw)    ::
    (query.type := query.Raw) ::
    (out.type   := out.Raw)   ::
    *[AnyDenotation]


  import ohnosequences.blast.api.{ outputFields => out }
  case object defaultBlastOutRec extends BlastOutputRecord(
    // query
    out.qseqid      :×:
    out.qstart      :×:
    out.qend        :×:
    out.qlen        :×:
    // reference
    out.sseqid      :×:
    out.sstart      :×:
    out.send        :×:
    out.slen        :×:
    // alignment
    out.evalue      :×:
    out.score       :×:
    out.bitscore    :×:
    out.length      :×:
    out.pident      :×:
    out.mismatch    :×:
    out.gaps        :×:
    out.gapopen     :×:
    out.qcovs       :×:
    |[AnyOutputField]
  )

  val defaultBlastOptions: blastn.Options := blastn.OptionsVals =
    blastn.defaults.update(
      num_threads(1)                ::
      word_size(42)                 ::
      max_target_seqs(10)           ::
      evalue(BigDecimal(0.001))     ::
      blastn.task(blastn.megablast) ::
      *[AnyDenotation]
    )

}
