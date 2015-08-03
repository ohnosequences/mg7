package ohnosequences.metapasta.instructions

import ohnosequences.nisperon.{Monoid, MapInstructions, AWS}
import ohnosequences.awstools.s3.ObjectAddress
import java.io.{PrintWriter, File}
import org.clapper.avsl.Logger
import scala.collection.mutable.ListBuffer
import ohnosequences.parsers.S3ChunksReader
import ohnosequences.formats.{RawHeader, FASTQ}
import scala.collection.mutable
import com.amazonaws.services.dynamodbv2.model.{ScalarAttributeType, AttributeDefinition, AttributeValue}
import ohnosequences.nisperon.logging.S3Logger
import ohnosequences.nisperon.bundles.NisperonMetadataBuilder
import ohnosequences.metapasta._
import ohnosequences.metapasta.MergedSampleChunk
import ohnosequences.parsers.S3ChunksReader
import ohnosequences.formats.RawHeader
import scala.Some
import ohnosequences.formats.FASTQ
import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.metapasta.AssignTable
import ohnosequences.metapasta.databases._
import ohnosequences.metapasta.MergedSampleChunk
import ohnosequences.parsers.S3ChunksReader
import ohnosequences.formats.RawHeader
import scala.Some
import ohnosequences.formats.FASTQ
import ohnosequences.metapasta.ReadsStats
import ohnosequences.metapasta.TaxInfo
import ohnosequences.metapasta.AssignTable



class LastInstructions(aws: AWS,
                       metadataBuilder: NisperonMetadataBuilder, //for bio4j
                       assignmentConfiguration: AssignmentConfiguration,
                       databaseFactory: DatabaseFactory[LastDatabase16S],
                       lastCommandTemplate: String,
                       fastaInput: Boolean = false,
                       logging: Boolean,
                       resultDirectory: ObjectAddress,
                       readsDirectory: ObjectAddress
                       ) extends
   MapInstructions[List[MergedSampleChunk],  (AssignTable, Map[(String, AssignmentType), ReadsStats])]  {

  val logger = Logger(this.getClass)

  case class LastContext(nodeRetriever: NodeRetriever, database: LastDatabase16S, last: Last, assigner: Assigner)
  override type Context = LastContext

  override def prepare() = {
    val lm = aws.s3.createLoadingManager()
    val nodeRetreiver = new BundleNodeRetrieverFactory().build(metadataBuilder)
    val lastDatabase = databaseFactory.build(lm)
    val last = new LastFactory().build(lm)
    val giMapper = new InMemoryGIMapperFactory().build(lm)
    val fastasWriter = new FastasWriter(aws, readsDirectory, nodeRetreiver)
    val assigner = new Assigner(new Bio4JTaxonomyTree(nodeRetreiver), lastDatabase, giMapper, assignmentConfiguration, extractHeader, Some(fastasWriter))
    LastContext(nodeRetreiver, lastDatabase, last, assigner)
  }


  //todo fix header
  def extractHeader(s: String) = s.replace("@", "").split("\\s")(0)

  def apply(input: List[MergedSampleChunk], s3logger: S3Logger, context: LastContext): (AssignTable, Map[(String, AssignmentType), ReadsStats]) = {


    //todo fix head
    val chunk = input.head

    //parsing
    val reader = S3ChunksReader(aws.s3, chunk.fastq)
    val parsed: List[FASTQ[RawHeader]] = reader.parseChunk[RawHeader](chunk.range._1, chunk.range._2)._1


    val inputFile = new File(if (fastaInput) "reads.fasta" else "reads.fastq")
    val outputFile = new File("out.last.maf")

    logger.info("saving reads to " + inputFile.getPath)
    val writer = new PrintWriter(inputFile)

    var emptyInput = true
    parsed.foreach { fastq =>
      val s = if (fastaInput) {
        fastq.toFasta
      } else {
        fastq.toFastq
      }
      if(emptyInput && !s.trim.isEmpty) {
        emptyInput = false
      }
    }
    writer.close()


    val startTime = System.currentTimeMillis()
    val code = if(emptyInput) {
      logger.warn("empty chunk.. skipping mapping")
      val pw = new PrintWriter(outputFile)
      pw.println("")
      pw.close()
      0
    } else {
      context.last.launch(lastCommandTemplate, context.database, inputFile, outputFile, fastaInput)
    }
    val endTime = System.currentTimeMillis()

    logger.info("last: " + (endTime - startTime + 0.0) / parsed.size + " ms per read")

    if(code != 0) {
      throw new Error("LAST finished with error code " + code)
    }

    logger.info("reading LAST result")
    val resultRaw = Utils.readFile(outputFile)


    if(logging) {
      logger.info("uploading result to S3")
      s3logger.uploadFile(outputFile)
     // aws.s3.putObject(ObjectAddress(logs.bucket, logs.key + "/" + output), new File(output))
    }


    logger.info("parsing LAST result")
    //M00476_38_000000000_A3FHW_1_1101_20604_2554_1_N_0_28	gi|313494140|gb|GU939576.1|	99.21	253	2	0	1	253	362	614	3e-127	 457

    //last
    //1027    gi|130750839|gb|EF434347.1|     497     253     +       1354    M00476:38:000000000-A3FHW:1:1101:15679:1771     0       253     +       253     253
   // val blastHit = """\s*([^\s]+)\s+([^\s]+)\s+([^\s]+).+""".r
    val lastHit = """\s*([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+.+""".r
    val comment = """#(.*)""".r



    val hits = new ListBuffer[Hit]()

    resultRaw.linesIterator.foreach {
      case comment(c) => //logger.info("skipping comment: " + c)
      case lastHit(_score, name1, start1, algSize1, strand1, seqSize1, name2) => {
        val readId = extractHeader(name2)
        val score = Utils.parseDouble(_score)
        hits += Hit(ReadId(readId), RefId(name1), score)
      }

      case l => logger.error("can't parse: " + l)
    }

    context.assigner.assign(s3logger, ChunkId(chunk), parsed, hits.toList)

    //result.toList
  }

}
