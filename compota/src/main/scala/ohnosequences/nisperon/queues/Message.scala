package ohnosequences.nisperon.queues

trait Message[T] {
  val id: String
  def value(): T
  def delete(): Unit
  def changeMessageVisibility(secs: Int)
}



class ConstantMessage[T](val id: String, t: T) extends Message[T] {

  def value(): T = t

  def delete() {}

  def changeMessageVisibility(secs: Int) {}

}

