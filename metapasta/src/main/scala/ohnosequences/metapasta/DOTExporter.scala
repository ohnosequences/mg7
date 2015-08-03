package ohnosequences.metapasta

import java.io.{PrintWriter, File}
import org.clapper.avsl.Logger
import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.metapasta.reporting.{FileType, FileTypeA}

object DOTExporter {

  val logger = Logger(this.getClass)

  def installGraphiz() {
    import scala.sys.process._

    logger.info("installing graphviz")
    "yum install -y graphviz".!
  }

  //1 [label="root", shape=box];
// b -- d;
  def generateDot(data: Map[Taxon, TaxInfo], nodeRetriever: com.ohnosequences.bio4j.titan.model.util.NodeRetrieverTitan, dst: File) {
    logger.info("generating dot file: " + dst.getAbsolutePath)

    val result = new PrintWriter(dst)


    val specialTaxa = Set(Assigned.taxon, NoHit.taxon, NoTaxId.taxon, NotAssignedCat.taxon, FileType.assignedToOtherRank)
    result.println("graph tax {")
    data.foreach { case (taxon, taxInfo) =>
      //print node

      if(specialTaxa.contains(taxon)) {
        result.println(taxon.taxId + "[label=\"" + taxon.taxId + "\\n" + taxInfo +  "\", shape=box];")
      } else {

        try {
          logger.info("taxInfo: " + taxInfo)
          val node = nodeRetriever.getNCBITaxonByTaxId(taxon.taxId)
          val name = node.getScientificName()
          result.println(taxon.taxId + " [label=\"" + name + "\\n" + taxInfo +  "\", shape=box];")
        } catch {
          case t: Throwable => logger.warn("couldn't retrieve taxon from id " + t.toString)
          //t.printStackTrace()
        }

        try {
          val node = nodeRetriever.getNCBITaxonByTaxId(taxon.taxId)
          val parent = node.getParent().getTaxId()
          result.println(parent + "--" + taxon.taxId + ";")
        } catch {
          case t: Throwable => logger.warn("couldn't retrieve parent taxon for " + t.toString)

          //

          // logger.warn(t.toString)
            //t.printStackTrace()
        }
      }
    }

    result.println("}")
    result.close()
  }

  def generatePdf(dot: File, pdf: File) {
    logger.info("generating pdf " + pdf.getAbsolutePath)
    val cmd = """dot -Tpdf -o$pdf$ $dot$"""
      .replace("$dot$", dot.getAbsolutePath)
      .replace("$pdf$", pdf.getAbsolutePath)
    import scala.sys.process._
    cmd.!
  }
}
