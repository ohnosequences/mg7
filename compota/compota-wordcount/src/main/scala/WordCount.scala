package ohnosequences.compota.wordcount

import ohnosequences.nisperon._
import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.nisperon.bundles.NisperonMetadataBuilder
import ohnosequences.nisperon.console.Server
import ohnosequences.nisperon.logging.S3Logger
import java.io.File
import ohnosequences.nisperon.Tasks
import ohnosequences.nisperon.queues.ProductQueue


object wordSplitInstructions extends Instructions[String, List[String]] {

  type Context = Unit

  override def prepare() {}

  override def solve(input: String, logger: S3Logger, context: Context): List[List[String]] = {
    logger.info("current folder: " + new File(".").getAbsolutePath)
    logger.info("input: " + input)
    logger.uploadFile(new File("/etc/fstab"))
    input.split("\\s+").toList.map(List(_))
  }
}

object countInstructions extends MapInstructions[List[String], (Map[String, Int], Int)] {

  type Context = Unit

  override def prepare() {}

  override def apply(input: List[String], logger: S3Logger, context: Context): (Map[String, Int], Int) = {
    logger.info("input: " + input)
    logger.info("parent: " + Tasks.parent("parent.test2"))
    val r = if (input.contains("tar")) {
      2 / 0
    } else {
      1
    }
    (input.map(_ -> r).toMap, input.size)
  }
}


object Wordcount extends Nisperon {


  override val nisperonConfiguration = NisperonConfiguration(
    metadataBuilder = new NisperonMetadataBuilder(new generated.metadata.wordcount()),
    email = "museeer@gmail.com",
    password = "password",
    autoTermination = false
  )

  val text = s3queue(
    name = "text",
    monoid = stringMonoid,
    serializer = stringSerializer
  )

  val words = s3queue(
    name = "words",
    monoid = new ListMonoid[String],
    serializer = new JsonSerializer[List[String]]
  )

  val counts = s3queue(
    name = "counts",
    monoid = new MapMonoid[String, Int](intMonoid),
    serializer = new JsonSerializer[Map[String, Int]]
  )

  val totalCounts = s3queue(
    name = "tcounts",
    monoid = intMonoid,
    serializer = intSerializer
  )

  val splitNispero = nispero(
    inputQueue = text,
    outputQueue = words,
    instructions = wordSplitInstructions,
    nisperoConfiguration = NisperoConfiguration(nisperonConfiguration, "split")
  )

  val countNispero = nispero(
    inputQueue = words,
    outputQueue = ProductQueue(counts, totalCounts),
    instructions = countInstructions,
    nisperoConfiguration = NisperoConfiguration(nisperonConfiguration, "count")
  )



  override def addTasks(): Unit = {
    text.initWrite()
    text.put("0", "", List("text a b c d d d d d e e e e e e tar tar tar tar tar"))
  }

  override def undeployActions(solved: Boolean): Option[String] = {None}

  override def checks(): Unit = {}

  override def additionalHandler(args: List[String]): Unit = {
    args match {
      case "console" :: Nil => {
        logger.info("running console")
        new Server(Wordcount).start()
      }
    }
  }
}
