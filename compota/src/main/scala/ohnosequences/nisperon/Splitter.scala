package ohnosequences.nisperon

trait Splitter[T] {
  def split(t: T): List[T]
}
