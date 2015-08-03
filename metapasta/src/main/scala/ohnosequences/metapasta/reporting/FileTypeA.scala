package ohnosequences.metapasta.reporting

import ohnosequences.metapasta._
import ohnosequences.metapasta.reporting.spreadsheeet.AnyAttribute.For
import scala.collection.mutable
import ohnosequences.nisperon.{maxLongMonoid, AWS, intMonoid, longMonoid}
import ohnosequences.metapasta.reporting.spreadsheeet._
import ohnosequences.awstools.s3.ObjectAddress

object FileType {
  type Item = (Taxon, (TaxonInfo, mutable.HashMap[(SampleId, AssignmentType), PerSampleData]))

  val assignedToOtherRank = Taxon("Not assigned at this rank")
  val emptyStringMonoid = new StringConstantMonoid("")
}

trait FileType {
  def additinalItems(stats: Map[(String, AssignmentType), ReadsStats]): List[FileType.Item]

  def attributes(stats: Map[(String, AssignmentType), ReadsStats]): List[AnyAttribute.For[FileType.Item]]

  def destination(dst: ObjectAddress): ObjectAddress
}

case class FileTypeA(group: AnyGroup, rank: Option[TaxonomyRank]) extends FileType {

  import FileType.{Item, emptyStringMonoid}

  override def destination(dst: ObjectAddress): ObjectAddress = {
    rank match {
      case None => dst / (group.name + ".frequencies.csv")
      case Some(rr) => dst / (group.name + "." + rr.toString + ".frequencies.csv")
    }

  }

  object taxId extends StringAttribute[Item]("TaxonomyID", new StringConstantMonoid("total")) {
    override def execute(item: Item, index: Int, context: Context): String = {
      item._1.taxId
    }
  }

  object taxonomyName extends StringAttribute[Item]("TaxonomyName", emptyStringMonoid) {
    override def execute(item: Item, index: Int, context: Context): String = {
      item._2._1.scientificName
    }
  }

  object taxonomyRank extends StringAttribute[Item]("TaxonomyRank", emptyStringMonoid) {
    override def execute(item: Item, index: Int, context: Context): String = {
      item._2._1.rank
    }
  }

  case class TotalMerged(sampleId: SampleId, totalMerged: Long)
    extends LongAttribute[Item](sampleId.id + "." + "total.merged", maxLongMonoid, hidden = true) {
    override def execute(item: Item, index: Int, context: Context): Long = totalMerged
  }

  case class SampleDirect1(sampleId: SampleId, assignmentType: AssignmentType)
    extends LongAttribute[Item](sampleId.id + "." + assignmentType + ".direct.counts" + "_", longMonoid, hidden = true) {

    override def execute(item: Item, index: Int, context: Context): Long = {
      item._2._2.get(sampleId -> assignmentType).map(_.direct).getOrElse(0)
    }

    override def printTotal(total: Long): String = ""

  }

  case class SampleDirect2(sampleId: SampleId, assignmentType: AssignmentType, sampleDirect1: SampleDirect1, totalMerged: TotalMerged)
    extends LongAttribute[Item](sampleId.id + "." + assignmentType + ".direct.counts", longMonoid) {
    override def execute(item: Item, index: Int, context: Context): Long = {
      if (item._1.equals(FileType.assignedToOtherRank)) {
        context.getTotal(totalMerged) - context.getTotal(sampleDirect1)
      } else {
        item._2._2.get(sampleId -> assignmentType).map(_.direct).getOrElse(0)
      }
    }
  }


  case class SampleCumulative1(sampleId: SampleId, assignmentType: AssignmentType) extends
  LongAttribute[Item](sampleId.id + "." + assignmentType + ".cumulative.counts", longMonoid, hidden = true) {
    override def execute(item: Item, index: Int, context: Context): Long = {
      item._2._2.get(sampleId -> assignmentType).map(_.cumulative).getOrElse(0)
    }

    override def printTotal(total: Long): String = ""
  }

  case class SampleCumulative2(sampleId: SampleId, assignmentType: AssignmentType, sampleCumulative1: SampleCumulative1, totalMerged: TotalMerged)
    extends LongAttribute[Item](sampleId.id + "." + assignmentType + ".cumulative.counts", longMonoid) {
    override def execute(item: Item, index: Int, context: Context): Long = {
      if (item._1.equals(FileType.assignedToOtherRank)) {
        context.getTotal(totalMerged) - context.getTotal(sampleCumulative1)
      } else {
        item._2._2.get(sampleId -> assignmentType).map(_.cumulative).getOrElse(0)
      }
    }

    override def printTotal(total: Long): String = ""
  }

  def attributes(stats: Map[(String, AssignmentType), ReadsStats]) = {

    val res = new mutable.ListBuffer[AnyAttribute.For[Item]]()


    res += taxId
    res += taxonomyName
    res += taxonomyRank


    for (sample <- group.samples) {
      val totalMerged = TotalMerged(sample, stats((sample.id, BBH)).merged)
      res += totalMerged
      for (assignmentType <- List(BBH, LCA)) {

        val sd1 = SampleDirect1(sample, assignmentType)
        res += sd1
        val sd2 = SampleDirect2(sample, assignmentType, sd1, totalMerged)
        res += sd2
        res += Normalize(sd2, totalMerged, sample.id + "." + assignmentType + ".direct.percentage", percentage = true)

        val sc1 = SampleCumulative1(sample, assignmentType)
        res += sc1
        val sc2 = SampleCumulative2(sample, assignmentType, sc1, totalMerged)
        res += sc2
        res += Normalize(sc2, totalMerged, sample.id + "." + assignmentType + ".cumulative.percentage", percentage = true)
      }
    }
    res.toList
  }

  override def additinalItems(stats: Map[(String, AssignmentType), ReadsStats]): List[Item] = {
    rank match {
      case None => List()
      case Some(r) => {
        val assignMap = new mutable.HashMap[(SampleId, AssignmentType), PerSampleData]()
        for (sample <- group.samples) {
          for (assignmentType <- List(BBH, LCA)) {
            assignMap.put((sample, assignmentType), PerSampleData(0, 0))
          }
        }
        val assignToOtherKind: FileType.Item = (Taxon("Not assigned at this rank"), (TaxonInfo("", ""), assignMap))
        List(assignToOtherKind)
      }
    }
  }
}

case class FileTypeB(project: ProjectGroup) extends FileType {

  override def destination(dst: ObjectAddress): ObjectAddress = {
    dst / (project.name + ".direct.absolute.freq.csv")
  }

  import FileType.{Item}

  object taxonomyName extends StringAttribute[Item]("TaxonomyName", new StringConstantMonoid("total")) {
    override def execute(item: Item, index: Int, context: Context): String = {
      item._2._1.scientificName
    }
  }

  case class SampleDirect(sampleId: SampleId, assignmentType: AssignmentType) extends LongAttribute[Item](sampleId.id + "." + assignmentType + ".direct.counts", longMonoid) {
    override def execute(item: Item, index: Int, context: Context): Long = {
      item._2._2.get(sampleId -> assignmentType).map(_.direct).getOrElse(0)
    }
  }

  def attributes(stats: Map[(String, AssignmentType), ReadsStats]) = {


    val sampleAttributes = new mutable.ListBuffer[SampleDirect]()
    for (sample <- project.samples) {
      for (assignmentType <- List(BBH, LCA)) {
        val sd = SampleDirect(sample, assignmentType)
        sampleAttributes += sd
      }
    }

    val res = new mutable.ListBuffer[AnyAttribute.For[Item]]()
    res += taxonomyName
    res ++= sampleAttributes
    for ((assignmentType, attrs) <- sampleAttributes.groupBy(_.assignmentType)) {
      res += Sum(attrs.toList)
    }

    res.toList
  }

  override def additinalItems(stats: Map[(String, AssignmentType), ReadsStats]): List[(Taxon, (TaxonInfo, mutable.HashMap[(SampleId, AssignmentType), PerSampleData]))] = List()
}

case class FileTypeC(project: ProjectGroup) extends FileType {

  import FileType.Item

  override def destination(dst: ObjectAddress): ObjectAddress = {
    dst / (project.name + ".direct.relative.freq.csv")
  }

  case class TotalMerged(sampleId: SampleId, totalMerged: Long) extends LongAttribute[Item](sampleId.id + "." + "total.merged", maxLongMonoid, hidden = true) {
    override def execute(item: Item, index: Int, context: Context): Long = totalMerged
  }

  object taxonomyName extends StringAttribute[Item]("TaxonomyName", new StringConstantMonoid("total")) {
    override def execute(item: Item, index: Int, context: Context): String = {
      item._2._1.scientificName
    }
  }

  case class SampleDirect(sampleId: SampleId, assignmentType: AssignmentType) extends LongAttribute[Item](sampleId.id + "." + assignmentType + ".direct.counts", longMonoid, hidden = true) {
    override def execute(item: Item, index: Int, context: Context): Long = {
      item._2._2.get(sampleId -> assignmentType).map(_.direct).getOrElse(0)
    }
  }

  def attributes(stats: Map[(String, AssignmentType), ReadsStats]) = {
    val res = new mutable.ListBuffer[AnyAttribute.For[Item]]()
    res += taxonomyName

    for (sample <- project.samples) {
      val totalMerged = new TotalMerged(sample, stats((sample.id, BBH)).merged)
      res += totalMerged
      for (assignmentType <- List(BBH, LCA)) {
        val sd = SampleDirect(sample, assignmentType)
        res += sd
        res += Normalize(sd, totalMerged, sample.id + "." + assignmentType + ".direct.percentage", percentage = true)
      }
    }
    res.toList
  }

  override def additinalItems(stats: Map[(String, AssignmentType), ReadsStats]): List[(Taxon, (TaxonInfo, mutable.HashMap[(SampleId, AssignmentType), PerSampleData]))] = List()
}

case class FileTypeD(group: SamplesGroup) extends FileType {


  import FileType.{Item, emptyStringMonoid}

  override def additinalItems(stats: Map[(String, AssignmentType), ReadsStats]): List[(Taxon, (TaxonInfo, mutable.HashMap[(SampleId, AssignmentType), PerSampleData]))] = List()

  override def destination(dst: ObjectAddress): ObjectAddress = {
    dst / (group.name + ".frequencies.complete.csv")
  }

  case class TotalMerged(sampleId: SampleId, totalMerged: Long) extends LongAttribute[Item](sampleId.id + "." + "total.merged", maxLongMonoid, hidden = true) {
    override def execute(item: Item, index: Int, context: Context): Long = totalMerged
  }

  object taxonomyName extends StringAttribute[Item]("TaxonomyName", emptyStringMonoid) {
    override def execute(item: Item, index: Int, context: Context): String = {
      item._2._1.scientificName
    }
  }

  case class SampleDirect(sampleId: SampleId, assignmentType: AssignmentType) extends LongAttribute[Item](sampleId.id + "." + assignmentType + ".direct.counts", longMonoid, true) {
    override def execute(item: Item, index: Int, context: Context): Long = {
      item._2._2.get(sampleId -> assignmentType).map(_.direct).getOrElse(0)
    }
  }


  case class SampleCumulative(sampleId: SampleId, assignmentType: AssignmentType) extends LongAttribute[Item](sampleId.id + "." + assignmentType + ".cumulative.counts", longMonoid, true) {
    override def execute(item: Item, index: Int, context: Context): Long = {
      item._2._2.get(sampleId -> assignmentType).map(_.cumulative).getOrElse(0)
    }

    override def printTotal(total: Long): String = ""
  }

  def attributes(stats: Map[(String, AssignmentType), ReadsStats]) = {

    val res = new mutable.ListBuffer[AnyAttribute.For[Item]]()

    res += taxonomyName

    val relDirect = new mutable.ListBuffer[(AssignmentType, DoubleAttribute[Item])]()
    val relCumulative = new mutable.ListBuffer[(AssignmentType, DoubleAttribute[Item])]()

    for (sample <- group.samples) {
      val totalMerged = TotalMerged(sample, stats((sample.id, BBH)).merged)
      res += totalMerged
      for (assignmentType <- List(BBH, LCA)) {
        val sd = SampleDirect(sample, assignmentType)
        res += sd
        val rd = Normalize(sd, totalMerged, sample.id + "." + assignmentType + ".direct.percentage", percentage = true)
        res += rd
        relDirect += ((assignmentType, rd))

        val sc = SampleCumulative(sample, assignmentType)
        res += sc
        val rc = Normalize(sc, totalMerged, sample.id + "." + assignmentType + ".cumulative.percentage", percentage = true)
        res += rc
        relCumulative += ((assignmentType, rc))
      }
    }

    for ((assignmentType, attrs) <- relDirect.groupBy(_._1)) {
      res += Average(attrs.toList.map(_._2))
    }

    res.toList
  }

}

