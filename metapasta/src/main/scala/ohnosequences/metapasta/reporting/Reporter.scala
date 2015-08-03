package ohnosequences.metapasta.reporting

import ohnosequences.metapasta._
import org.clapper.avsl.Logger
import ohnosequences.nisperon._
import ohnosequences.awstools.s3.{S3, ObjectAddress}
import ohnosequences.metapasta.ReadsStats
import ohnosequences.metapasta.AssignTable
import scala.collection.mutable
import ohnosequences.metapasta.reporting.spreadsheeet.CSVExecutor
import scala.collection.mutable.ListBuffer
import java.io.{PrintWriter, File}


case class PerSampleData(direct: Long, cumulative: Long)

case class TaxonInfo(scientificName: String, rank: String)

case class SampleId(id: String)

case class SampleTag(tag: String)

class Reporter(aws: AWS,
               tablesAddresses: List[ObjectAddress],
               statsAddresses: List[ObjectAddress],
               tags: Map[SampleId, List[SampleTag]],
               nodeRetriever: NodeRetriever,
               destination: ObjectAddress,
               projectName: String
               ) {
  val assignmentTableSerializer = ohnosequences.metapasta.assignTableSerializer
  val statsMonoid = new MapMonoid[(String, AssignmentType), ReadsStats](readsStatsMonoid)
  val statsSerializer = readsStatsSerializer

  def read[T](address: ObjectAddress, serializer: Serializer[T]): T = {
     serializer.fromString(aws.s3.readWholeObject(address))
  }

  val logger = Logger(this.getClass)

  def generate() {
    logger.info("reading assignment tables")

    var table: AssignTable = assignTableMonoid.unit
    for (obj <- tablesAddresses) {
      logger.info("reading from " + obj)
      val t = read[AssignTable](obj, assignmentTableSerializer)
      table = assignTableMonoid.mult(table, t)
    }

    var stats: Map[(String, AssignmentType), ReadsStats] = statsMonoid.unit

    logger.info("reading reads stats")
    for (obj <- statsAddresses) {
      logger.info("reading from " + obj)
      val t = read[Map[(String, AssignmentType), ReadsStats]](obj, statsSerializer)
      stats = statsMonoid.mult(stats, t)
    }

    val samplesS: Set[String] = stats.keySet.map(_._1)
    val samples = samplesS.toList.map(SampleId)
    val pg = ProjectGroup(projectName, samples)


    val groups: List[AnyGroup] = generateGroups(samples, tags) ++ samples.map { s => OneSampleGroup(s) } :+ pg

    for (group <- groups) {
      val groupStats = stats.filterKeys {
        sampleAssignmentType =>
          group.samples.map(_.id).contains(sampleAssignmentType._1)
      }

      val groupTable = AssignTable(table.table.filterKeys {
        sampleAssignmentType =>
          group.samples.map(_.id).contains(sampleAssignmentType._1)
      })
      projectSpecific(groupTable, groupStats, group)
    }
  }


  def generateGroups(samples: List[SampleId], tags: Map[SampleId, List[SampleTag]]): List[SamplesGroup] = {
    logger.info("generating groups")
    val allTags = new mutable.HashSet[SampleTag]()
    for ((sample, sampleTags) <- tags) {
      allTags ++= sampleTags
    }

    val groups = new ListBuffer[SamplesGroup]()

    for (tag <- allTags) {
      groups += SamplesGroup(tag.tag + "+", tags.filter(_._2.contains(tag)).keys.toList)
      groups += SamplesGroup(tag.tag + "-", tags.filterNot(_._2.contains(tag)).keys.toList)
    }
    groups.toList
  }



  //stats should contain only stats for group.samples
  def projectSpecific(table: AssignTable, stats: Map[(String, AssignmentType), ReadsStats], group: AnyGroup) {

    group match {
      case ProjectGroup(name, s) => logger.info("generating project specific files")
      case SamplesGroup(name, s) => logger.info("generating group specific files for group: " + name)
      case OneSampleGroup(s) => logger.info("generating sample specific files for sample: " + s.id)
    }


    val ranks: List[Option[TaxonomyRank]] = TaxonomyRank.ranks.map(Some(_)) ++ List(None)



//    for ((sampleAssignmentType, stat) <- stats) {
//      val sampleId = SampleId(sampleAssignmentType._1)
//      val assignmentType = sampleAssignmentType._2
//      //val d = (stat.notMerged + stat.notAssigned).toInt
//      //will be calculated after
//      assignedToOtherLevel.put((sampleId, assignmentType), PerSampleData(0, 0))
//    }

    for (r <- ranks) {

      val fileTypes = r match {
        case None => {
          group match {
            case pg @ ProjectGroup(name, s) => List(FileTypeA(pg, r), FileTypeB(pg), FileTypeC(pg))
            case g @ SamplesGroup(name, s) => List(FileTypeA(g, r), FileTypeD(g))
            case osg @ OneSampleGroup(s) => List(FileTypeA(osg, r))
          }
        }
        case Some(rr) => {
          List(FileTypeA(group, r))
        }
      }


      r match {
        case None => logger.info("generating for all kinds")
        case Some(rr) => logger.info("generating for " + rr + " kind")
      }

      val items: Iterable[FileType.Item] = r match {
        case None => {
          prepareMapping(table, r)
        }
        case Some(rr) => {
          //val assignedToOtherLevel = new mutable.HashMap[(SampleId, AssignmentType), PerSampleData]()
          //val assignedToOtherLevelItem = (FileType.assignedOnOtherKind, (TaxonInfo("", ""), assignedToOtherLevel))
          prepareMapping(table, r) //+= assignedToOtherLevelItem
        }
      }

      for (fType <- fileTypes) {
        val csvPrinter = new CSVExecutor[FileType.Item](fType.attributes(stats), items)
        val resultS = csvPrinter.execute()
        val dst = group match {
          case ProjectGroup(name, ss) => destination
          case SamplesGroup(name, ss) => destination / name
          case OneSampleGroup(ss) => destination / ss.id
        }
        aws.s3.putWholeObject(fType.destination(dst), resultS)
      }

    }
  }


  def getTaxInfo(taxon: Taxon): Option[TaxonInfo] = {
    try {
      val node = nodeRetriever.nodeRetriever.getNCBITaxonByTaxId(taxon.taxId)
      Some(TaxonInfo(node.getScientificName(), node.getRank()))
    } catch {
      case t: Throwable => {
        //t.printStackTrace();
        logger.warn("couldn't retrieve taxon info for taxon: " + taxon.taxId)
        //TaxonInfo("", "")
        None
      }
    }
  }



  def prepareMapping(table: AssignTable, rank: Option[TaxonomyRank]): mutable.HashMap[Taxon, (TaxonInfo, mutable.HashMap[(SampleId, AssignmentType), PerSampleData])] = {

    val result = new mutable.HashMap[Taxon, (TaxonInfo, mutable.HashMap[(SampleId, AssignmentType), PerSampleData])]()

    for ((sampleAssignmentType, taxonsInfos) <-  table.table) {

      val sampleId = SampleId(sampleAssignmentType._1)
      val assignmentType = sampleAssignmentType._2

      for ((taxon, taxonInfo) <- taxonsInfos) {
        val taxonInfoInfo = getTaxInfo(taxon).getOrElse(TaxonInfo("", ""))

        val filter = rank match {
          case None => {
            false
          }
          case Some(rk) => {
            //special taxa
            val specialTaxa = List(NoHit.taxon, NotAssignedCat.taxon, NoTaxId.taxon)
            if (specialTaxa.contains(taxon) || taxonInfoInfo.rank.equals(rk.toString)) {
              false
            } else {
              true
            }
          }
        }

        if(!filter) {
          result.get(taxon) match {
            case None => {
              val map = new mutable.HashMap[(SampleId, AssignmentType), PerSampleData]()

              map.put((sampleId, assignmentType), new PerSampleData(direct = taxonInfo.count, cumulative = taxonInfo.acc))
              result.put(taxon, (taxonInfoInfo, map))
            }
            case Some((taxoninfo, map)) => {
              map.put((sampleId, assignmentType), new PerSampleData(direct = taxonInfo.count, cumulative = taxonInfo.acc))
            }
          }
        }
      }
    }

    result
  }


}
