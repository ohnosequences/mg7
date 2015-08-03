package ohnosequences.metapasta

import scala.collection.mutable
import ohnosequences.metapasta.databases.{GIMapper, Database16S}
import ohnosequences.formats.{RawHeader, FASTQ}
import ohnosequences.nisperon.{AWS, MapMonoid}
import ohnosequences.nisperon.logging.{Logger, S3Logger}


case class ReadId(readId: String)
case class RefId(refId: String)
case class Hit(readId: ReadId, refId: RefId, score: Double)


trait AssignerAlgorithm {


  def assignAll(taxonomyTree: Tree[Taxon], hits: List[Hit], reads: List[FASTQ[RawHeader]], getTaxIds: (List[Hit], Logger) =>  (List[(Hit, Taxon)], Set[RefId]), logger: Logger): (mutable.HashMap[ReadId, Assignment], mutable.HashSet[RefId]) = {
    val result = new mutable.HashMap[ReadId, Assignment]()

    val wrongRefIdsAll = new mutable.HashSet[RefId]()

    val hitsPerReads: Map[ReadId, List[Hit]] = hits.groupBy(_.readId)

    for ( (readId, hits) <- hitsPerReads) {
      val (hitsTaxa, wrongRefIds) = getTaxIds(hits, logger)
      wrongRefIdsAll ++= wrongRefIds
      if (hitsTaxa.isEmpty) {
        result.put(readId, NoTaxIdAssignment(hits.map(_.refId).toSet))
      } else {
        result.put(readId, assign(taxonomyTree, hitsTaxa, logger))
      }

    }
    (result, wrongRefIdsAll)

  }

  def assign(taxonomyTree: Tree[Taxon], hitsTaxa: List[(Hit, Taxon)], logger: Logger): Assignment
}

class LCAAlgorithm(assignmentConfiguration: AssignmentConfiguration) extends AssignerAlgorithm {
  override def assign(taxonomyTree: Tree[Taxon], hitsTaxa: List[(Hit, Taxon)], logger: Logger): Assignment = {

    val maxScore = hitsTaxa.map(_._1.score).max

    hitsTaxa.filter { case (hit, taxon) =>
      hit.score >= assignmentConfiguration.bitscoreThreshold &&
      hit.score >= assignmentConfiguration.p * maxScore
    } match {
      case Nil => NotAssigned("threshold", hitsTaxa.map(_._1.refId).toSet, hitsTaxa.map(_._2).toSet)
      case filteredHitsTaxa => {
        val taxa = filteredHitsTaxa.map(_._2).toSet
        val refIds = filteredHitsTaxa.map(_._1.refId).toSet

        TreeUtils.isInLine(taxonomyTree, taxa.toSet) match {
          case Some(specific) => {
            logger.info("taxa form a line: " + taxa.map(_.taxId) + " most specific: " + specific.taxId)
            TaxIdAssignment(specific, hitsTaxa.map(_._1.refId).toSet, line = true)
          }
          case None => {
            //calculating lca
            val lca = TreeUtils.lca(taxonomyTree, taxa)
            logger.info("taxa not in a line: " + taxa.map(_.taxId) + " lca: " + lca.taxId)
            TaxIdAssignment(lca, refIds, lca = true)
          }
        }
      }
    }
  }
}

object BBHAlgorithm extends AssignerAlgorithm {
  override def assign(taxonomyTree: Tree[Taxon], hitsTaxa: List[(Hit, Taxon)], logger: Logger): Assignment = {
    val (hit, taxon) = hitsTaxa.max(new Ordering[(Hit, Taxon)] {
      override def compare(x: (Hit, Taxon), y: (Hit, Taxon)): Int = (y._1.score - x._1.score).signum
    })
    TaxIdAssignment(taxon, Set(hit.refId), bbh = true)
  }
}

sealed trait Assignment {
  type AssignmentCat <: AssignmentCategory
}

case class TaxIdAssignment(taxon: Taxon, refIds: Set[RefId], lca: Boolean = false, line: Boolean = false, bbh: Boolean = false) extends Assignment {
  type AssignmentCat = Assigned.type
}

case class NoTaxIdAssignment(refIds: Set[RefId]) extends Assignment {
  type AssignmentCat = NoTaxId.type
}

case class NotAssigned(reason: String, refIds: Set[RefId], taxIds: Set[Taxon]) extends Assignment {
  type AssignmentCat = NotAssignedCat.type
}



object AssignerAlgorithms {



 // def groupHits(hits: List[Hit], )
}

class Assigner(taxonomyTree: Tree[Taxon],
               database: Database16S,
               giMapper: GIMapper,
               assignmentConfiguration: AssignmentConfiguration,
               extractReadHeader: String => String,
               fastasWriter: Option[FastasWriter]) {

 // val tree: Tree[Taxon] = new Bio4JTaxonomyTree(nodeRetriever)


  def assign(logger: Logger, chunk: ChunkId, reads: List[FASTQ[RawHeader]], hits: List[Hit]):
    (AssignTable, Map[(String, AssignmentType), ReadsStats]) = {



    val (lcaAssignments, lcaWrongRefIds) = logger.benchExecute("LCA assignment") {
      new LCAAlgorithm(assignmentConfiguration).assignAll(taxonomyTree, hits, reads, getTaxIds, logger)
    }

    val lcaRes = logger.benchExecute("preparing results") {
      prepareAssignedResults(logger, chunk, LCA, reads, lcaAssignments, lcaWrongRefIds)
    }


    val (bbhAssignments, bbhWrongRefIds) = logger.benchExecute("BBH assignment") {
      BBHAlgorithm.assignAll(taxonomyTree, hits, reads, getTaxIds, logger)
    }

    val bbhRes = logger.benchExecute("preparing results") {
      prepareAssignedResults(logger, chunk, BBH, reads, bbhAssignments, bbhWrongRefIds)
    }

    (assignTableMonoid.mult(lcaRes._1, bbhRes._1), Map((chunk.sample.id, LCA) -> lcaRes._2, (chunk.sample.id, BBH) -> bbhRes._2))

  }

  def getTaxIdFromRefId(refId: RefId, logger: Logger): Option[Taxon] = {
    database.parseGI(refId.refId) match {
      case Some(gi) => giMapper.getTaxIdByGi(gi) match {
        case None => /*logger.error("database error: can't parse taxId from gi: " + refId);*/ None
        case Some(taxon) => Some(taxon)
      }
      case None => {
        //logger.error("database error: can't parse gi from ref id: " + refId)
        None
      }
    }
  }

  //
  def getTaxIds(hits: List[Hit], logger: Logger): (List[(Hit, Taxon)], Set[RefId]) = {
    val wrongRefs = new mutable.HashSet[RefId]()
    val taxons = hits.flatMap { hit =>
      getTaxIdFromRefId(hit.refId, logger) match {
        case Some(taxon) if taxonomyTree.isNode(taxon) => Some((hit, taxon))
        case None => {
          logger.warn("couldn't find taxon for ref id: " + hit.refId.refId)
          wrongRefs += hit.refId
          None
        }
        case Some(taxon) => {
          logger.warn("taxon with id: " + taxon.taxId + " is not presented in " + database.name)
          wrongRefs += hit.refId
          None
        }
      }
    }
    (taxons, wrongRefs.toSet)
  }


  def prepareAssignedResults(logger: Logger, chunk: ChunkId,
                             assignmentType: AssignmentType,
                             reads: List[FASTQ[RawHeader]],
                             assignments: mutable.HashMap[ReadId, Assignment],
                             wrongRefId: mutable.HashSet[RefId]): (AssignTable, ReadsStats) = {

    val readsStatsBuilder = new ReadStatsBuilder(wrongRefId)

    reads.foreach {
      fastq =>
        val readId = ReadId(extractReadHeader(fastq.header.getRaw))
        assignments.get(readId) match {
          case None => {
            //nohit
            fastasWriter.foreach(_.writeNoHit(fastq, readId, chunk.sample))
            logger.info(readId + " -> " + "no hits")
            readsStatsBuilder.incrementByCategory(NoHit)
          }
          case Some(assignment) => {
            fastasWriter.foreach(_.write(chunk.sample, fastq, readId, assignment))
            logger.info(readId + " -> " + assignment)
            readsStatsBuilder.incrementByAssignment(assignment)
          }
        }
    }

    fastasWriter.foreach(_.uploadFastas(chunk, assignmentType))

    val assignTable = mutable.HashMap[Taxon, TaxInfo]()

    //generate assign table
    assignments.foreach {
      case (readId, TaxIdAssignment(taxon, refIds, _, _, _)) =>
        assignTable.get(taxon) match {
          case None => assignTable.put(taxon, TaxInfo(1, 1))
          case Some(TaxInfo(count, acc)) => assignTable.put(taxon, TaxInfo(count + 1, acc + 1))
        }
        TreeUtils.getLineageExclusive(taxonomyTree, taxon).foreach { p  =>
            assignTable.get(p) match {
              case None => assignTable.put(p, TaxInfo(0, 1))
              case Some(TaxInfo(count, acc)) => assignTable.put(p, TaxInfo(count, acc + 1))
            }
        }
      case _ => ()
    }

    val readStats = readsStatsBuilder.build

    assignTable.put(NoHit.taxon, TaxInfo(readStats.noHit, readStats.noHit))
    assignTable.put(NoTaxId.taxon, TaxInfo(readStats.noTaxId, readStats.noTaxId))
    assignTable.put(NotAssignedCat.taxon, TaxInfo(readStats.notAssigned, readStats.notAssigned))

    (AssignTable(Map((chunk.sample.id, assignmentType) -> assignTable.toMap)), readStats)
  }

}



