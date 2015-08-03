package ohnosequences.metapasta

trait Factory[C, +T] {
  def build(ctx: C): T
}
