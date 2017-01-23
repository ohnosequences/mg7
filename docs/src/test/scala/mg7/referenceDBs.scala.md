
```scala
package ohnosequences.test.mg7

import ohnosequences.mg7._
import ohnosequences.db.rna16s

case object rna16sRefDB extends ReferenceDB(
  rna16s.dbName,
  rna16s.data.blastDBS3,
  rna16s.data.id2taxasS3
)

```




[main/scala/mg7/bundles.scala]: ../../../main/scala/mg7/bundles.scala.md
[main/scala/mg7/configs.scala]: ../../../main/scala/mg7/configs.scala.md
[main/scala/mg7/csv.scala]: ../../../main/scala/mg7/csv.scala.md
[main/scala/mg7/data.scala]: ../../../main/scala/mg7/data.scala.md
[main/scala/mg7/defaults.scala]: ../../../main/scala/mg7/defaults.scala.md
[main/scala/mg7/loquats/1.flash.scala]: ../../../main/scala/mg7/loquats/1.flash.scala.md
[main/scala/mg7/loquats/2.split.scala]: ../../../main/scala/mg7/loquats/2.split.scala.md
[main/scala/mg7/loquats/3.blast.scala]: ../../../main/scala/mg7/loquats/3.blast.scala.md
[main/scala/mg7/loquats/4.assign.scala]: ../../../main/scala/mg7/loquats/4.assign.scala.md
[main/scala/mg7/loquats/5.merge.scala]: ../../../main/scala/mg7/loquats/5.merge.scala.md
[main/scala/mg7/loquats/6.count.scala]: ../../../main/scala/mg7/loquats/6.count.scala.md
[main/scala/mg7/package.scala]: ../../../main/scala/mg7/package.scala.md
[main/scala/mg7/parameters.scala]: ../../../main/scala/mg7/parameters.scala.md
[main/scala/mg7/pipeline.scala]: ../../../main/scala/mg7/pipeline.scala.md
[main/scala/mg7/referenceDB.scala]: ../../../main/scala/mg7/referenceDB.scala.md
[test/scala/mg7/counts.scala]: counts.scala.md
[test/scala/mg7/fqnames.scala]: fqnames.scala.md
[test/scala/mg7/mock/illumina.scala]: mock/illumina.scala.md
[test/scala/mg7/mock/pacbio.scala]: mock/pacbio.scala.md
[test/scala/mg7/PRJEB6592/PRJEB6592.scala]: PRJEB6592/PRJEB6592.scala.md
[test/scala/mg7/referenceDBs.scala]: referenceDBs.scala.md
[test/scala/mg7/taxonomy.scala]: taxonomy.scala.md
[test/scala/mg7/testData.scala]: testData.scala.md
[test/scala/mg7/testDefaults.scala]: testDefaults.scala.md