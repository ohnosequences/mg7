//package ohnosequences.metapasta.reporting
//
//import ohnosequences.metapasta.{DOTExporter, TaxInfo, NodeRetriever, Metapasta}
//import ohnosequences.awstools.s3.ObjectAddress
//import scala.collection.mutable
//import java.io.File
//import org.clapper.avsl.Logger
//import ohnosequences.nisperon.queues.Merger
//
//
//case class CSVGenerator(metapasta: Metapasta, nodeRetriever: NodeRetriever) {
//
//  val logger = Logger(this.getClass)
//
//  def generateGroupCSV(group: Group) {
//
//    //reading merged results
//    val tableAddress = Merger.mergeDestination(metapasta, metapasta.assignTable)
//
//
//    logger.info("reading assign table " + metapasta)
//
//
//    val tables = metapasta.assignTable.serializer.fromString(metapasta.aws.s3.readWholeObject(tableAddress))
//
//
//
////    //tax -> (sample -> taxinfo)
////    val resultCSV = new mutable.StringBuilder()
////    logger.info("transposing table")
////    val finalTaxInfo = mutable.HashMap[String, mutable.HashMap[String, TaxInfo]]()
////
////    val perSampleTotal = mutable.HashMap[String, TaxInfo]()
////    var totalCount0 = 0
////    var totalAcc0 = 0
////    table.foreach {
////      case (sample, map) =>
////        var sampleTotal = TaxInfoMonoid.unit
////
////        map.foreach {
////          case (tax, taxInfo) =>
////            sampleTotal = TaxInfoMonoid.mult(taxInfo, sampleTotal)
////            finalTaxInfo.get(tax) match {
////              case None => {
////                val initMap = mutable.HashMap[String, TaxInfo](sample -> taxInfo)
////                finalTaxInfo.put(tax, initMap)
////              }
////              case Some(sampleMap) => {
////                sampleMap.get(sample) match {
////                  case None => sampleMap.put(sample, taxInfo)
////                  case Some(oldTaxInfo) => {
////                    sampleMap.put(sample, TaxInfoMonoid.mult(taxInfo, oldTaxInfo))
////                  }
////                }
////              }
////            }
////
////        }
////        perSampleTotal.put(sample, sampleTotal)
////        totalCount0 += sampleTotal.count
////        totalAcc0 += sampleTotal.acc
////    }
////
////
////    resultCSV.append("#;taxId;")
////    resultCSV.append("name;rank;")
////    table.keys.foreach {
////      sample =>
////        resultCSV.append(sample + ".count;")
////        resultCSV.append(sample + ".acc;")
////    }
////    resultCSV.append("total.count;total.acc\n")
////
////    var totalCount1 = 0
////    var totalAcc1 = 0
////    finalTaxInfo.foreach {
////      case (taxid, map) =>
////        resultCSV.append(taxid + ";")
////        val (name, rank) = try {
////          val node = nodeRetriever.nodeRetriever.getNCBITaxonByTaxId(taxid)
////          (node.getScientificName(), node.getRank())
////        } catch {
////          case t: Throwable => ("", "")
////        }
////        resultCSV.append(name + ";")
////        resultCSV.append(rank + ";")
////
////
////        //mappingInstructions.nodeRetriever.g
////
////        var taxCount = 0
////        var taxAcc = 0
////        table.keys.foreach {
////          sample =>
////            map.get(sample) match {
////              case Some(taxInfo) => {
////                resultCSV.append(taxInfo.count + ";" + taxInfo.acc + ";")
////                taxCount += taxInfo.count
////                taxAcc += taxInfo.acc
////              }
////              case None => {
////                resultCSV.append(0 + ";" + 0 + ";")
////              }
////            }
////        }
////        resultCSV.append(taxCount + ";" + taxAcc + "\n")
////        totalCount1 += taxCount
////        totalAcc1 += taxAcc
////      //calculating total
////    }
////    resultCSV.append("total; ; ;")
////    table.keys.foreach {
////      sample =>
////        resultCSV.append(perSampleTotal(sample).count + ";")
////        resultCSV.append(perSampleTotal(sample).acc + ";")
////    }
////
////    if (totalCount0 == totalCount1) {
////      resultCSV.append(totalCount0 + ";")
////    } else {
////      resultCSV.append("\n# " + totalCount0 + "!=" + totalCount1 + ";")
////    }
////
////    if (totalAcc0 == totalAcc1) {
////      resultCSV.append(totalAcc0 + "\n")
////    } else {
////      resultCSV.append("\n# " + totalAcc0 + "!=" + totalAcc1 + "\n")
////    }
////
////
////    val result = ObjectAddress(nisperonConfiguration.bucket, "results/" + "result.csv")
////    aws.s3.putWholeObject(result, resultCSV.toString())
////
////    if (configuration.generateDot) {
////      logger.info("generate dot files")
////      DOTExporter.installGraphiz()
////      table.foreach {
////        case (sample, map) =>
////          val dotFile = new File(sample + ".dot")
////          val pdfFile = new File(sample + ".pdf")
////          DOTExporter.generateDot(map, nodeRetriever.nodeRetriever, new File(sample + ".dot"))
////          DOTExporter.generatePdf(dotFile, pdfFile)
////          val res = ObjectAddress(nisperonConfiguration.bucket, "results/viz/" + sample + ".pdf")
////          aws.s3.putObject(res, pdfFile)
////      }
////    }
////
////    None
//
//  }
//
//}
