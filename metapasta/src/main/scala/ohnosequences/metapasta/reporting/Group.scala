package ohnosequences.metapasta.reporting


trait AnyGroup {
  def name: String
  val samples: List[SampleId]
}

case class SamplesGroup(name: String, samples: List[SampleId]) extends AnyGroup

case class ProjectGroup(name: String, samples: List[SampleId]) extends AnyGroup {
}

case class OneSampleGroup(sample: SampleId) extends AnyGroup {
  override def name: String = sample.id
  override  val samples = List(sample)
}
