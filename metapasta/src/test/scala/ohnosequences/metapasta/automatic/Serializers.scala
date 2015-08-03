package ohnosequences.metapasta.automatic

import ohnosequences.metapasta._
import ohnosequences.metapasta.reporting.{FileType, TaxonInfo}
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}

import scala.collection.mutable

object Serializers extends Properties("Serializers") {
  property("assign table") = forAll (Gen.choose(1, 100)) { n =>
    val assignTableRaw = new mutable.HashMap[Taxon, TaxInfo]()
    assignTableRaw.put(NoTaxId.taxon, TaxInfo(n, n))
    assignTableRaw.put(Taxon("uuuuu"), TaxInfo(n, n))

    val assignTable = AssignTable(Map( ("sample1" -> LCA) -> assignTableRaw.toMap))

    assignTableSerializer.fromString(assignTableSerializer.toString(assignTable)).equals(assignTable)
  }
}
