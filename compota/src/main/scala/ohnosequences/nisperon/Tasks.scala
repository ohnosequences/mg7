package ohnosequences.nisperon


object Tasks {
  def generateChild(parentId: String, nispero: String, count: Int) = parentId + "." + nispero + "_" + count

  //todo nisperoName regexp!!!!
  val idR = """.+_(\d+)_(\d+)$""".r

  def getOperand(id: String): Option[Int] = {
    id match {
      case idR(ord, c) => Some(ord.toInt)
      case _ => None
    }
  }

  def parent(id: String) = id.replaceAll("\\.[^\\.]+$", "")

}
