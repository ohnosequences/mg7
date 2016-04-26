
```scala
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
```

This actually depends on the workers instance type

```scala
      num_threads(4) ::
      blastn.task(blastn.blastn) ::
      evalue(BigDecimal(1E-100)) ::
```

We're going to use all hits to do global sample-coherent assignment. But not now, so no reason for this to be huge

```scala
      max_target_seqs(150) ::
      strand(Strands.both) ::
      word_size(46) ::
      show_gis(false) ::
      ungapped(false) ::
      penalty(-2)  ::
      reward(1) ::
```

95% is a reasonable minimum. If it does not work, be more stringent with read preprocessing

```scala
      perc_identity(95.0) ::
      *[AnyDenotation]
    )
}

```




[main/scala/mg7/bio4j/bundle.scala]: bio4j/bundle.scala.md
[main/scala/mg7/bio4j/taxonomyTree.scala]: bio4j/taxonomyTree.scala.md
[main/scala/mg7/bio4j/titanTaxonomyTree.scala]: bio4j/titanTaxonomyTree.scala.md
[main/scala/mg7/csv.scala]: csv.scala.md
[main/scala/mg7/data.scala]: data.scala.md
[main/scala/mg7/dataflow.scala]: dataflow.scala.md
[main/scala/mg7/dataflows/full.scala]: dataflows/full.scala.md
[main/scala/mg7/dataflows/noFlash.scala]: dataflows/noFlash.scala.md
[main/scala/mg7/loquats/1.flash.scala]: loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: loquats/6.count.scala.md
[main/scala/mg7/loquats/7.stats.scala]: loquats/7.stats.scala.md
[main/scala/mg7/loquats/8.summary.scala]: loquats/8.summary.scala.md
[main/scala/mg7/package.scala]: package.scala.md
[main/scala/mg7/parameters.scala]: parameters.scala.md
[test/scala/mg7/counts.scala]: ../../../test/scala/mg7/counts.scala.md
[test/scala/mg7/lca.scala]: ../../../test/scala/mg7/lca.scala.md
[test/scala/mg7/pipeline.scala]: ../../../test/scala/mg7/pipeline.scala.md
[test/scala/mg7/taxonomy.scala]: ../../../test/scala/mg7/taxonomy.scala.md