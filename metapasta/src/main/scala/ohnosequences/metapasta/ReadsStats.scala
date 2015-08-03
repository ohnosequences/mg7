package ohnosequences.metapasta

import ohnosequences.nisperon.{JsonSerializer, Serializer, Monoid}
import scala.collection.mutable



sealed  trait AssignmentCategory {
  def taxon: Taxon
}
case object Assigned extends AssignmentCategory {
  override def taxon = Taxon("assigned")
}
case object NoHit extends AssignmentCategory {
  override def taxon = Taxon("No hit")
}
case object NoTaxId extends AssignmentCategory {
  override def taxon = Taxon("Not assigned via GI")
}
case object NotMerged extends AssignmentCategory {
  override def taxon = Taxon("notmerged")
}
case object NotAssignedCat extends AssignmentCategory {
  override def taxon = Taxon("Not assigned due to threshold")
}


class ReadStatsBuilder(var wrongRefIds: mutable.HashSet[RefId] = new mutable.HashSet[RefId]()) {

  var total = 0L
  var merged = 0L
  var notMerged = 0L //problems with flash
  var noHit = 0L //no hit: too strong mapping parameters
  var noTaxId = 0L
  var notAssigned = 0L //thresholds are to strict, in some cases (best blast hit) it can be due to wrong refs
  var assigned = 0L
   //all wrong refs are ignored

  var bbhAssigned = 0L
  var lcaAssigned = 0L
  var lineAssigned = 0L

  def incrementByAssignment(assignment:  Assignment) { assignment match {
    case TaxIdAssignment(_, _, lca, line, bbh) => {
      if (lca) lcaAssigned +=1
      if (line) lineAssigned +=1
      if (bbh) bbhAssigned +=1
      assigned += 1
    }
    case NoTaxIdAssignment(_) => noTaxId += 1
    case NotAssigned(_, _, _) => notAssigned += 1
  }}

  def incrementByCategory(cat: AssignmentCategory) { cat match {
    case NoHit => noHit += 1
    case NoTaxId => noTaxId += 1
    case NotMerged => notMerged += 1
    case Assigned => assigned += 1
    case NotAssignedCat => notAssigned += 1
  }}



  //for checks
  def incrementMerged() {
    merged += 1
  }

  def incrementTotal() {
    total += 1
  }


  def addWrongRefId(id: RefId) = {wrongRefIds += id}

  def build = ReadsStats(
      total = total,
      merged = merged,
      notMerged = notMerged,
      noHit = noHit,
      noTaxId = noTaxId,
      notAssigned = notAssigned,
      assigned = assigned,
      wrongRefIds = wrongRefIds.map(_.refId).toSet,
      lcaAssigned = lcaAssigned,
      lineAssigned = lineAssigned,
      bbhAssigned = bbhAssigned
    )
}



case class ReadsStats(total: Long,
                      merged: Long,
                      notMerged: Long,
                      noHit: Long,
                      noTaxId: Long,
                      notAssigned: Long,
                      assigned: Long,
                      wrongRefIds: Set[String] = Set[String](),
                      lcaAssigned: Long,
                      lineAssigned: Long,
                      bbhAssigned: Long) {
  def mult(y: ReadsStats): ReadsStats = readsStatsMonoid.mult(this, y)
}


//todo final e-mail generation
object readsStatsMonoid extends Monoid[ReadsStats] {

  override def mult(x: ReadsStats, y: ReadsStats): ReadsStats = {
    ReadsStats(
      total = x.total + y.total,
      merged = x.merged + y.merged,
      notMerged = x.notMerged + y.notMerged,
      noHit = x.noHit + y.noHit,
      noTaxId = x.noTaxId + y.noTaxId,
      notAssigned = x.notAssigned + y.notAssigned,
      assigned = x.assigned + y.assigned,
      wrongRefIds = x.wrongRefIds ++ y.wrongRefIds,
      lcaAssigned = x.lcaAssigned + y.lcaAssigned,
      lineAssigned = x.lineAssigned + y.lineAssigned,
      bbhAssigned = x.bbhAssigned + y.bbhAssigned
    )
  }

  val _unit = ReadsStats(0, 0, 0, 0, 0, 0, 0, Set[String](), 0, 0, 0)
  override def unit: ReadsStats = _unit
}

object readsStatsSerializer extends Serializer[Map[(String, AssignmentType), ReadsStats]] {

  val rawStatsSerializer = new JsonSerializer[Map[String, ReadsStats]]()
  override def toString(t: Map[(String, AssignmentType), ReadsStats]): String = {
    val raw: Map[String, ReadsStats] = t.map { case (sampleAssignmentType, stats)  =>
      (sampleAssignmentType._1 + "###" + sampleAssignmentType._2.toString, stats)
    }
    rawStatsSerializer.toString(raw)
  }

  override def fromString(s: String): Map[(String, AssignmentType), ReadsStats] = {
    val raw : Map[String, ReadsStats]= rawStatsSerializer.fromString(s)
    raw.map { case (sampleAssignmentType, stats)  =>
      val parts = sampleAssignmentType.split("###")
      ((parts(0), AssignmentType.fromString(parts(1))), stats)
    }
  }
}
