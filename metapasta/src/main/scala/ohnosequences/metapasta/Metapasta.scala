package ohnosequences.metapasta

import ohnosequences.nisperon._
import ohnosequences.nisperon.queues.{QueueMerger, ProductQueue}
import scala.collection.mutable
import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.metapasta.instructions.{LastInstructions, BlastInstructions, FlashInstructions}
import ohnosequences.metapasta.reporting._
import java.io.File

abstract class Metapasta(configuration: MetapastaConfiguration) extends Nisperon {



  override val aws = new AWS(new File(System.getProperty("user.home"), "metapasta.credentials"))

  val nisperonConfiguration: NisperonConfiguration = NisperonConfiguration(
    managerGroupConfiguration = configuration.managerGroupConfiguration,
    metamanagerGroupConfiguration = configuration.metamanagerGroupConfiguration,
    defaultInstanceSpecs = configuration.defaultInstanceSpecs,
    metadataBuilder = configuration.metadataBuilder,
    email = configuration.email,
    autoTermination = true,
    timeout = configuration.timeout,
    password = configuration.password,
    removeAllQueues = configuration.removeAllQueues
  )

  object pairedSamples extends DynamoDBQueue (
    name = "pairedSamples",
    monoid = new ListMonoid[PairedSample],
    serializer = new JsonSerializer[List[PairedSample]],
    throughputs = (1, 1)
  )

  val writeThroughput = configuration.mergeQueueThroughput match {
    case Fixed(m) => m
    case SampleBased(ratio, max) => math.max(ratio * configuration.samples.size, max).toInt
  }

  object mergedSampleChunks extends DynamoDBQueue(
    name = "mergedSampleChunks",
    monoid = new ListMonoid[MergedSampleChunk](),
    serializer = new JsonSerializer[List[MergedSampleChunk]](),
    throughputs = (writeThroughput, 1)
  )

  object readsStats extends S3Queue(
    name = "readsStats",
    monoid = new MapMonoid[(String, AssignmentType), ReadsStats](readsStatsMonoid),
    serializer = readsStatsSerializer
  )


  object assignTable extends S3Queue(
    name = "table",
    monoid = assignTableMonoid,
    serializer = assignTableSerializer
  )

  override val mergingQueues = List(assignTable, readsStats)

  val flashNispero = nispero(
    inputQueue = pairedSamples,
    outputQueue = ProductQueue(readsStats, mergedSampleChunks),
    instructions = new FlashInstructions(
      aws, configuration.chunksSize, ObjectAddress(nisperonConfiguration.bucket, "reads"),
    configuration.chunksThreshold, configuration.flashTemplate),
    nisperoConfiguration = NisperoConfiguration(nisperonConfiguration, "flash")
  )

  val bio4j = new Bio4jDistributionDist(configuration.metadataBuilder)

  //val lastInstructions =  new LastInstructions(aws, new NTLastDatabase(aws), bio4j, configuration.lastTemplate)


  val mappingInstructions: MapInstructions[List[MergedSampleChunk],  (AssignTable, Map[(String, AssignmentType), ReadsStats])] =
    configuration match {
      case b: BlastConfiguration => new BlastInstructions(
        aws = aws,
        metadataBuilder = configuration.metadataBuilder,
        assignmentConfiguration = b.assignmentConfiguration,
        blastCommandTemplate = b.blastTemplate,
        databaseFactory = b.databaseFactory,
        useXML = b.xmlOutput,
        logging = configuration.logging,
        resultDirectory = ObjectAddress(nisperonConfiguration.bucket, "results"),
        readsDirectory = ObjectAddress(nisperonConfiguration.bucket, "reads")
      )
      case l: LastConfiguration => new LastInstructions(
        aws = aws,
        metadataBuilder = configuration.metadataBuilder,
        assignmentConfiguration = l.assignmentConfiguration,
        lastCommandTemplate = l.lastTemplate,
        databaseFactory = l.databaseFactory,
        fastaInput = l.useFasta,
        logging = configuration.logging,
        resultDirectory = ObjectAddress(nisperonConfiguration.bucket, "results"),
        readsDirectory = ObjectAddress(nisperonConfiguration.bucket, "reads")
      )
    }


  val mapNispero = nispero(
    inputQueue = mergedSampleChunks,
    outputQueue = ProductQueue(assignTable, readsStats),
    instructions = mappingInstructions,
    nisperoConfiguration = NisperoConfiguration(nisperonConfiguration, "map", workerGroup = configuration.mappingWorkers)
  )


  //todo test failed actions ...
  override def undeployActions(force: Boolean): Option[String] = {
    if (force) {
      return None
    }

    val nodeRetriever = new BundleNodeRetrieverFactory().build(configuration.metadataBuilder)

    val tableAddress = QueueMerger.destination(nisperonConfiguration.results, assignTable)
    val statsAddress = QueueMerger.destination(nisperonConfiguration.results,  readsStats)

    logger.info("reading assign table " + tableAddress)

    val tables = assignTable.serializer.fromString(aws.s3.readWholeObject(tableAddress))

    val tagging  = new mutable.HashMap[SampleId, List[SampleTag]]()

    for ((sample, tags) <- configuration.tagging) {
      tagging.put(SampleId(sample.name), tags)
    }

    val reporter = new Reporter(aws, List(tableAddress), List(statsAddress), tagging.toMap, nodeRetriever,
      ObjectAddress(nisperonConfiguration.bucket, "results"), nisperonConfiguration.id)
    reporter.generate()


    logger.info("merge FASTA files")

    val reads = ObjectAddress(nisperonConfiguration.bucket, "reads")
    val results = ObjectAddress(nisperonConfiguration.bucket, "results")

    val merger = new FastaMerger(aws, reads, results, configuration.samples.map(_.name))
    merger.merge()

    if(configuration.generateDot) {
      logger.info("generate dot files")
      DOTExporter.installGraphiz()
      tables.table.foreach { case (sampleAssignmentType, map) =>
        val sample = sampleAssignmentType._1
        val assignmentType = sampleAssignmentType._2
        val dotFile = new File(sample  + "." + assignmentType + ".tree.dot")
        val pdfFile = new File(sample  + "." + assignmentType + ".tree.pdf")
        DOTExporter.generateDot(map, nodeRetriever.nodeRetriever,dotFile)
        DOTExporter.generatePdf(dotFile, pdfFile)
        aws.s3.putObject(S3Paths.treeDot(results, sample, assignmentType), pdfFile)
        aws.s3.putObject(S3Paths.treePdf(results, sample, assignmentType), pdfFile)
      }
    }
    None
  }



  def additionalHandler(args: List[String]) {

    args match {
      case "merge" :: "fastas" :: Nil => {
        val reads = ObjectAddress(nisperonConfiguration.bucket, "reads")
        val results = ObjectAddress(nisperonConfiguration.bucket, "results")

        val merger = new FastaMerger(aws, reads, results, configuration.samples.map(_.name))
        merger.merge()
      }
      case "undeploy" :: "actions" :: Nil => undeployActions(false)
      case _ =>  undeployActions(false)
    }
  }


  override def checkConfiguration(verbose: Boolean): Boolean = {
    logger.info("checking samples")
    configuration.samples.forall { sample =>
      val t = aws.s3.objectExists(sample.fastq1, None)
      if (verbose) println("aws.s3.objectExists(" + sample.fastq1 + ") = " + t)
      t
    } &&
     configuration.samples.forall { sample =>
      val t = aws.s3.objectExists(sample.fastq2, None)
      if (verbose) println("aws.s3.objectExists(" + sample.fastq2 + ") = " + t)
      t
    } && {
      logger.info("checking tagging")
      configuration.tagging.forall { case (sample, tags) =>
        configuration.samples.contains(sample)
      }
    } && super.checkConfiguration(verbose)

  }

  def addTasks() {
      pairedSamples.initWrite()
      val t1 = System.currentTimeMillis()
      configuration.samples.foreach {
        sample =>
          pairedSamples.put(sample.name, "", List(List(sample)))
      }
      val t2 = System.currentTimeMillis()
      logger.info("tasks added (in " + (t2 - t1) + " ms)")
  }
}
