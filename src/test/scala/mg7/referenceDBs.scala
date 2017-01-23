package ohnosequences.test.mg7

import ohnosequences.mg7._
import ohnosequences.db.rna16s

case object rna16sRefDB extends ReferenceDB(
  rna16s.dbName,
  rna16s.data.blastDBS3,
  rna16s.data.id2taxasS3
)
