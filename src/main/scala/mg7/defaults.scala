package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api.{ outputFields => out, _ }


case object defaults {

  val blastnOptions: blastn.Options := blastn.OptionsVals =
    blastn.options(
      /* This actually depends on the workers instance type */
      num_threads(4)              ::
      blastn.task(blastn.blastn)  ::
      evalue(BigDecimal(1E-100))  ::
      max_target_seqs(10000)      ::
      strand(Strands.both)        ::
      word_size(46)               ::
      show_gis(false)             ::
      ungapped(false)             ::
      penalty(-2)                 ::
      reward(1)                   ::
      perc_identity(95.0)         ::
      *[AnyDenotation]
    )

  case object blastnOutputRecord extends BlastOutputRecord(
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

  /*
    ## Default Illumina parameters

    These parameters are a sensible default for Illumina reads.
  */
  case object Illumina {

    lazy val blastnOptions =
      defaults.blastnOptions.update(
        word_size(46)               ::
        evalue(BigDecimal(1E-100))  ::
        perc_identity(98.0)         ::
        *[AnyDenotation]
      )

    class Parameters(val refDBs: AnyReferenceDB*) extends MG7Parameters(
      splitInputFormat = FastQInput,
      splitChunkSize   = 1000,
      blastCommand     = blastn,
      blastOutRec      = defaults.blastnOutputRecord,
      blastOptions     = blastnOptions.value,
      referenceDBs     = refDBs.toSet
    )

    def apply(refDBs: AnyReferenceDB*): Parameters = new Parameters(refDBs: _*)
  }

  case object PacBio {

    lazy val blastnOptions =
      defaults.blastnOptions.update(
        reward(1)                   ::
        penalty(-2)                 ::
        word_size(72)               ::
        evalue(BigDecimal(1e-100))  ::
        perc_identity(98.5)         ::
        *[AnyDenotation]
      )

    class Parameters(val refDBs: AnyReferenceDB*) extends MG7Parameters(
      splitInputFormat = FastQInput,
      splitChunkSize   = 100,
      blastCommand     = blastn,
      blastOptions     = blastnOptions.value,
      blastOutRec      = defaults.blastnOutputRecord,
      referenceDBs     = refDBs.toSet
    )

    def apply(refDBs: AnyReferenceDB*): Parameters = new Parameters(refDBs: _*)
  }

}
