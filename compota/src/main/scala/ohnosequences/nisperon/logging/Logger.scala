package ohnosequences.nisperon.logging


trait Logger {
  def warn(s: String)

  def error(s: String)

  def info(s: String)

  def benchExecute[T](description: String)(statement: =>T): T = {
    val t1 = System.currentTimeMillis()
    val res = statement

    val t2 = System.currentTimeMillis()
    info(description + " finished in " + (t2 - t1) + " ms")
    res
  }
}



class ConsoleLogger(prefix: String, verbose: Boolean = true) extends Logger {
  override def info(s: String) = {
    if (verbose) println("[" + "INFO " + prefix + "]: " + s)
  }

  override def error(s: String) {
    println("[" + "ERROR " + prefix + "]: " + s)
  }

  override def warn(s: String) = {
    if (verbose) println("[" + "WARN " + prefix + "]: " + s)
  }
}

