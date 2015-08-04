package ohnosequences.metagenomica

case object parsers {
  
  val blastHitRegex = """^\s*([^\s]+)\s+([^\s]+).*?([^\s]+)\s*$""".r
  val blastCommentRegex = """#(.*)""".r
}
