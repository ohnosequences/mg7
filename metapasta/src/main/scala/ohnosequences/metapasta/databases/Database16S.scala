package ohnosequences.metapasta.databases



trait Database16S {
  val name: String
  def parseGI(refId: String): Option[String]
}

trait LastDatabase16S extends Database16S {}

trait BlastDatabase16S extends Database16S {}

