package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api.{ outputFields => out, _ }


case object defaults {

  // We set here all options explicitly
  val blastnOptions: blastn.Options := blastn.OptionsVals =
    blastn.options(
      /* This actually depends on the workers instance type */
      num_threads(4)              ::
      blastn.task(blastn.blastn)  ::
      evalue(BigDecimal(1E-100))  ::
      /* We're going to use all hits to do global sample-coherent assignment. But not now, so no reason for this to be huge */
      max_target_seqs(150)        ::
      strand(Strands.both)        ::
      word_size(46)               ::
      show_gis(false)             ::
      ungapped(false)             ::
      penalty(-2)                 ::
      reward(1)                   ::
      /* 95% is a reasonable minimum. If it does not work, be more stringent with read preprocessing */
      perc_identity(95.0) ::
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
        num_threads(4)              ::
        word_size(46)               ::
        evalue(BigDecimal(1E-100))  ::
        max_target_seqs(10000)      ::
        perc_identity(98.0)         ::
        *[AnyDenotation]
      )

    case class parameters(val refDBs: AnyReferenceDB*) extends MG7Parameters(
      splitInputFormat = FastQInput,
      splitChunkSize   = 1000,
      blastCommand     = blastn,
      blastOutRec      = defaults.blastnOutputRecord,
      blastOptions     = blastnOptions.value,
      referenceDBs     = refDBs.toSet
    )
  }

  case object PacBio {

    lazy val blastnOptions =
      defaults.blastnOptions.update(
        reward(1)                   ::
        penalty(-2)                 ::
        word_size(72)               ::
        perc_identity(98.5)         ::
        max_target_seqs(10000)      ::
        evalue(BigDecimal(1e-100))  ::
        *[AnyDenotation]
      )

    case class parameters(val refDBs: AnyReferenceDB*) extends MG7Parameters(
      splitInputFormat = FastQInput,
      splitChunkSize   = 100,
      blastCommand     = blastn,
      blastOptions     = blastnOptions.value,
      blastOutRec      = defaults.blastnOutputRecord,
      referenceDBs     = refDBs.toSet
    )
  }

}
