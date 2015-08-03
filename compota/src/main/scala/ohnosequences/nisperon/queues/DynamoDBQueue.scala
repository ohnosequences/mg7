package ohnosequences.nisperon.queues

import ohnosequences.nisperon.{Serializer, Monoid, AWS}
import com.amazonaws.services.dynamodbv2.model._
import scala.collection.JavaConversions._
import org.clapper.avsl.Logger
import ohnosequences.awstools.ddb.Utils
import scala.collection.mutable.ListBuffer
import ohnosequences.nisperon.Tasks

import scala.util.{Success, Failure, Try}

class DynamoDBQueueAbstract[T](
                        aws: AWS,
                        name: String,
                        monoid: Monoid[T],
                        serializer: Serializer[T],
                        throughputs: (Int, Int),
                        deadLetterQueueName: String
                        ) extends MonoidQueue[T](name, monoid, serializer) {

  override val merger: QueueMerger[T] = new DefaultQueueMerger(DynamoDBQueueAbstract.this, aws.s3)


  def createBatchWriteItemRequest(table: String, items: List[Map[String, AttributeValue]]): BatchWriteItemRequest = {
    val writeOperations = new java.util.ArrayList[WriteRequest]()
    items.foreach { item =>
      writeOperations.add(new WriteRequest()
        .withPutRequest(new PutRequest()
        .withItem(item)
        ))
    }

    val map = new java.util.HashMap[String, java.util.List[WriteRequest]]()
    map.put(table, writeOperations)

    new BatchWriteItemRequest().withRequestItems(map)
  }


  val idAttr = "id"
  val valueAttr = "val"

  val logger = Logger(this.getClass)

  val sqsQueue = new SQSQueue[T](aws.sqs.sqs, name, serializer, deadLetterQueueName = Some(deadLetterQueueName))

  var sqsWriter: Option[SQSWriter[T]] = None
  var sqsReader: Option[SQSReader[T]] = None


  val ddbWriter = new DynamoDBWriter(aws, monoid, name, serializer, idAttr, valueAttr, true)

  def delete() {
    Utils.deleteTable(aws.ddb, name)
    sqsQueue.delete()
  }

  def put(parentId: String, nispero: String, values: List[T]) {
    sqsWriter match {
      case Some(writer) => {
        var c = 0
        values.filter(!_.equals(monoid.unit)).map {
          value =>
            c += 1
            val id = Tasks.generateChild(parentId, nispero, c)
            ddbWriter.put(id, value)
        }
        ddbWriter.flush()
        c = 0
        values.filter(!_.equals(monoid.unit)).map {
          value =>
            c += 1
            val id = Tasks.generateChild(parentId, nispero, c)
            writer.write(id, value)
        }
        writer.flush()
      }
      case None => throw new Error("write to a not initialized queue")
    }
  }


  def read(): Message[T] = {
    sqsReader match {
      case Some(reader) => {
        val rawMessage = reader.read

        new Message[T] {
          val id: String = rawMessage.id

          def value(): T = rawMessage.value()

          def delete() {
            aws.ddb.deleteItem(new DeleteItemRequest()
              .withTableName(name)
              .withKey(Map(idAttr -> new AttributeValue().withS(id)))
            )
            rawMessage.delete()

          }

          def changeMessageVisibility(secs: Int): Unit = rawMessage.changeMessageVisibility(secs)
        }
      }
      case None => throw new Error("read from a not initialized queue")
    }
  }

  def initRead() {
    init()
    sqsReader = Some(sqsQueue.getReader(false))
  }

  def init() {
    Utils.createTable(aws.ddb, name, new AttributeDefinition(idAttr, ScalarAttributeType.S), None, logger, throughputs._1, throughputs._2)
  }

  def initWrite() {
    init()
    sqsWriter = Some(sqsQueue.getWriter(monoid))
    ddbWriter.init()
  }

  //need for reset state...
  def reset() {
    sqsReader match {
      case Some(reader) => reader.reset()
      case None => throw new Error("reset a not initialized queue")
    }
  }


  def sqsQueueInfo(): Option[SQSQueueInfo] = {
    sqsQueue.getQueueInfo
  }

  //  def inflight: Int = {
//    aws.sqs.createQueue(sqsQueue.name).getApproximateNumberOfMessages
//  }


  def isEmpty: Boolean = {
    Try {
      val count = aws.ddb.scan(new ScanRequest()
        .withTableName(name)
        .withSelect(Select.COUNT)
        .withLimit(1)
      ).getCount
      //println(count)
      count == 0
    } match {
      case Failure(t) => {
        false
      }
      case Success(b) => {
        b
      }
    }
  }

  def list(): List[String] = {
    val result = ListBuffer[String]()
    var lastKey: java.util.Map[String, AttributeValue] = null
    do {
      val items = aws.ddb.scan(new ScanRequest()
        .withTableName(name)
        .withAttributesToGet(idAttr)
      )
      result ++= items.getItems.map(_.get(idAttr).getS)
      lastKey = items.getLastEvaluatedKey
    } while (lastKey != null)
    result.toList
  }



  type Listening = java.util.Map[java.lang.String, com.amazonaws.services.dynamodbv2.model.AttributeValue]
//todo someone throws NPE
// /queue/metatest010_snapshotmergedSampleChunks/messages
//   java.lang.NullPointerException
// at ohnosequences.nisperon.queues.DynamoDBQueue.listChunk(DynamoDBQueue.s
//   cala:191)
// at ohnosequences.nisperon.console.Console.listMessages(Console.scala:263
// )
// at ohnosequences.nisperon.console.ConsolePlan$$anonfun$intent$1.applyOrE
// lse(Server.scala:99)
// at ohnosequences.nisperon.console.ConsolePlan$$anonfun$intent$1.applyOrE
// lse(Server.scala:45)
// at scala.PartialFunction$OrElse.apply(PartialFunction.scala:162)
// at ohnosequences.nisperon.console.Auth$$anonfun$apply$1.applyOrElse(Serv
//   er.scala:30)
// at ohnosequences.nisperon.console.Auth$$anonfun$apply$1.applyOrElse(Serv
//   List(Node(metatest010_snapshotpairedSamples), Node(metatest010_snapshotmergedSam
  def listChunk(limit: Int, lastKey: Option[String]): (Option[String], List[String]) = {
    val result = ListBuffer[String]()
   // var lastKey: java.util.Map[String, AttributeValue] = null

    val request = new ScanRequest()
      .withTableName(name)
      .withAttributesToGet(idAttr)
      .withLimit(limit)

    lastKey match {
      case Some(key) => request.addExclusiveStartKeyEntry(idAttr, new AttributeValue().withS(key))
      case None => ()

    }
    //do {
      val items = aws.ddb.scan(request)



      result ++= items.getItems.map(_.get(idAttr).getS)

    val newLastKey = if (items.getLastEvaluatedKey != null && items.getLastEvaluatedKey.containsKey(idAttr)) {
      Some(items.getLastEvaluatedKey.get(idAttr).getS)
    } else {
      None
    }

    //  items.getLastEvaluatedKey.get(idAttr)
    //} while (lastKey != null)
    (newLastKey, result.toList)

  }

  def read(id: String): Option[T] = {
    try {
      aws.ddb.getItem(new GetItemRequest()
        .withTableName(name)
        .withKey(Map(idAttr -> new AttributeValue().withS(id)))
      ).getItem match {
        case null => None
        case item => Some(serializer.fromString(item.get(valueAttr).getS))
      }
    } catch {
      case t: Throwable => logger.warn("message not found: " + id); None
    }
  }

  def delete(id: String) {
    try {
      aws.ddb.deleteItem(new DeleteItemRequest()
        .withTableName(name)
        .withKey(Map(idAttr -> new AttributeValue().withS(id)))
      )
    } catch {
      case t: Throwable => logger.warn("message not found: " + id); None
    }
  }
}
