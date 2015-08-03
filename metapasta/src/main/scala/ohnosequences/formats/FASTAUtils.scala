package ohnosequences.formats



object FASTAUtils {
  def main2(args: Array[String]) {
    val fasta = io.Source.fromFile(args(0))
    val output = new java.io.PrintWriter(args(1))
    var c = 0
    var totalSeq = 0L
    var totalLen = 0L 
    fasta.getLines().foreach { line =>
      c += 1
      if (c % 10000 == 0) {
        println("processed " + c + " lines")
      }
      if (line.startsWith(">")) {
        output.println("\n" + line)
        totalSeq += 1
      } else {
        totalLen += line.size
        output.print(line)
      }
    }
    output.close()
    println("total seq: " + totalSeq)
    println("total len: " + totalLen)
  }
}
