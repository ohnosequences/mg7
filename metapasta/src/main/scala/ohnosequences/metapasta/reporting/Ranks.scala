package ohnosequences.metapasta.reporting


trait TaxonomyRank


object TaxonomyRank {
  val ranks = List(Genus, Phylum, Species, Class, Order, Superkingdom)
}

case object Genus extends TaxonomyRank {
  override def toString: String = "genus"
}

case object Phylum extends TaxonomyRank {
  override def toString: String = "phylum"
}

case object Species extends TaxonomyRank {
  override def toString: String = "species"
}

case object Class extends TaxonomyRank {
  override def toString: String = "class"
}

case object Order extends TaxonomyRank {
  override def toString: String = "order"
}

case object Superkingdom extends TaxonomyRank {
  override def toString: String = "superkingdom"
}

case object NoRank extends TaxonomyRank {
  override def toString: String = "no rank"
}


