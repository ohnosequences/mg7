
```scala
package ohnosequences.mg7

import ohnosequences.cosas._, types._, klists._, typeUnions._
import ohnosequences.awstools.s3._
import ohnosequences.datasets._
import ohnosequences.flash.api._
import ohnosequences.blast.api.{ outputFields => out, _ }


case object defaults {

  val blastnOptions: blastn.Options := blastn.OptionsVals =
    blastn.options(
```

This actually depends on the workers instance type

```scala
      num_threads(4)              ::
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
```


## Default Illumina parameters

These parameters are a sensible default for Illumina reads.


```scala
  case object Illumina {

    lazy val blastnOptions =
      defaults.blastnOptions.update(
        perc_identity(98.0) ::
        *[AnyDenotation]
      )

    class Parameters(val refDBs: AnyReferenceDB*) extends MG7Parameters(
      splitInputFormat = FastQInput,
      splitChunkSize   = 100,
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
        perc_identity(98.5) ::
        *[AnyDenotation]
      )

    class Parameters(val refDBs: AnyReferenceDB*) extends MG7Parameters(
      splitInputFormat = FastQInput,
      splitChunkSize   = 10,
      blastCommand     = blastn,
      blastOptions     = blastnOptions.value,
      blastOutRec      = defaults.blastnOutputRecord,
      referenceDBs     = refDBs.toSet
    ) {

      override def blastFilter(row: csv.Row[BlastOutRecKeys]): Boolean = {
        row.select(out.qcovs).toDouble >= 99.0
      }
    }

    def apply(refDBs: AnyReferenceDB*): Parameters = new Parameters(refDBs: _*)
  }

}

```




[main/scala/mg7/bundles.scala]: bundles.scala.md
[main/scala/mg7/configs.scala]: configs.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/defaults.scala]: defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[main/scala/mg7/pipeline.scala]: pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: referenceDB.scala.md
[test/scala/mg7/counts.scala]: ../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/fqnames.scala]: ../../../test/scala/mg7/fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: ../../../test/scala/mg7/mock/illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: ../../../test/scala/mg7/mock/pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: ../../../test/scala/mg7/PRJEB6592/PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: ../../../test/scala/mg7/referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../test/scala/mg7/taxonomy.scala.md
[test/scala/mg7/testData.scala]: ../../../test/scala/mg7/testData.scala.md
[test/scala/mg7/testDefaults.scala]: ../../../test/scala/mg7/testDefaults.scala.md