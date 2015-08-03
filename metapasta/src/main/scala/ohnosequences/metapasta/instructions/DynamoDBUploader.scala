package ohnosequences.metapasta.instructions

import ohnosequences.nisperon.{MapInstructions, AWS}
import com.amazonaws.services.dynamodbv2.model._
import scala.collection.JavaConversions._
import org.clapper.avsl.Logger
import ohnosequences.awstools.ddb.Utils
import java.util
import ohnosequences.nisperon.logging.S3Logger
import ohnosequences.metapasta.ReadInfo


class DynamoDBUploader(aws: AWS, readsTable: String, samplesTable: String) extends MapInstructions[List[ReadInfo], Unit] {

  override type Context = Unit

  val logger = Logger(this.getClass)

  val sampleAttr = "sample"
  val chunkAttr = "chunk"

  val hash = new AttributeDefinition().withAttributeName(sampleAttr).withAttributeType(ScalarAttributeType.S)
  val range = new AttributeDefinition().withAttributeName(chunkAttr).withAttributeType(ScalarAttributeType.S)

  override def prepare() {
    Utils.createTable(aws.ddb, readsTable, ReadInfo.hash, Some(ReadInfo.range), logger, 100, 1)
    Utils.createTable(aws.ddb, samplesTable, hash, Some(range), logger, 1, 1)
  }

  def apply(input: List[ReadInfo], s3logger: S3Logger, context: Context) {
    val batchSize = 40 // x25

    var c = -1
    input.grouped(25).foreach { chunk =>
      c += 1
      val writeOperations = new java.util.ArrayList[WriteRequest]()
      chunk.foreach { readInfo =>
        writeOperations.add(new WriteRequest()
          .withPutRequest(new PutRequest()
          .withItem(readInfo.toDynamoItem(c / batchSize))
          ))
      }



      if (!writeOperations.isEmpty) {

        var operations: java.util.Map[String, java.util.List[WriteRequest]] = new java.util.HashMap[String, java.util.List[WriteRequest]]()
        operations.put(readsTable, writeOperations)
        do {
          //to
          try {
            val res = aws.ddb.batchWriteItem(new BatchWriteItemRequest()
              .withRequestItems(operations)            )
            operations = res.getUnprocessedItems
            val size = operations.values().map(_.size()).sum
            logger.info("unprocessed: " + size)
          } catch {
            case t: ProvisionedThroughputExceededException => logger.warn(t.toString + " " + t.getMessage)
          }
        } while (!operations.isEmpty)

        //write to chunks table
        if(c % batchSize == 0) {
          val item = new util.HashMap[String, AttributeValue]()
          item.put(sampleAttr, new AttributeValue().withS(chunk.head.sample))
          item.put(chunkAttr, new AttributeValue().withS(chunk.head.chunkId(c / batchSize)))

          val request = new PutItemRequest()
            .withTableName(samplesTable)
            .withItem(item)

          var uploaded = false

          while (!uploaded) {
            try {
              aws.ddb.putItem(request)
              uploaded = true
            } catch {
              case t: Throwable => logger.warn("couldn't upload chunk id " + t.toString + " " + t.getMessage)
            }
          }
        }
      }

    }
  }
}
