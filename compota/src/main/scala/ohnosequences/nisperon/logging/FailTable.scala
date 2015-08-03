package ohnosequences.nisperon.logging

import ohnosequences.nisperon.AWS
import com.amazonaws.services.dynamodbv2.model._
import java.util
import java.text.SimpleDateFormat
import java.util.Date
import ohnosequences.awstools.ddb.Utils
import org.clapper.avsl.Logger

case class Failure(taskId: String, time: Long, instanceId: String, message: String) {
  val format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")

  def formattedTime(): String = {
    format.format(new Date(time))
  }
}

//todo add sqs queue for this
//todo add table with all success (or jsut retrive it)
case class FailTable(aws: AWS, name: String) {
  val logger = Logger(this.getClass)

  val hashKeyName = "h"
  val rangeKeyName = "r"
  val bodyKeyName = "b"

  val hashAttribute = new AttributeDefinition().withAttributeName(hashKeyName).withAttributeType(ScalarAttributeType.S)
  val rangeAttribute = new AttributeDefinition().withAttributeName(rangeKeyName).withAttributeType(ScalarAttributeType.S)

  def create() {
    Utils.createTable(
      ddb = aws.ddb,
      tableName = name,
      hash = hashAttribute,
      range = Some(rangeAttribute),
      logger = logger,
      waitForCreation = false
    )
  }

  //todo add call for this
  def delete() {
    Utils.deleteTable(aws.ddb, name)
  }

  def hashKey(taskId: String): AttributeValue = {
    new AttributeValue().withS(taskId)
  }

  def rangeKey(failure: Failure): AttributeValue = {
    //val timeStamp = System.currentTimeMillis()
    new AttributeValue().withS(failure.instanceId + ":" + failure.time)
  }



  def fail(taskId: String, instanceId: String, message: String) = {
    val failure = Failure(taskId, System.currentTimeMillis(), instanceId, message)
    val item = new util.HashMap[String, AttributeValue]()
    item.put(hashKeyName, hashKey(failure.taskId))
    item.put(rangeKeyName, rangeKey(failure))
    item.put(bodyKeyName,  new AttributeValue().withS(message))
    aws.ddb.putItem(name, item)
  }

  def fails(tasksId: String): Int = {
    val item = new util.HashMap[String, Condition]()
    item.put(hashKeyName, new Condition()
      .withAttributeValueList(hashKey(tasksId))
      .withComparisonOperator(ComparisonOperator.EQ)
    )
    aws.ddb.query(new QueryRequest()
      .withTableName(name)
      .withKeyConditions(item)
      .withSelect(Select.COUNT)
    ).getCount
  }


  //add link to instance id
  def failsChunk(lastKey: Option[(String, String)], limit: Int = 20): (Option[(String, String)], List[Failure]) = {
    logger.info("failsChunk(" + lastKey + ")")
    var req = new ScanRequest()
      .withTableName(name)
      .withLimit(limit)
      .withSelect(Select.ALL_ATTRIBUTES)


    req = lastKey match {
      case None => req
      case Some((hash, range)) => {
        val item = new util.HashMap[String, AttributeValue]()
        item.put(hashKeyName, new AttributeValue().withS(hash))
        item.put(rangeKeyName, new AttributeValue().withS(range))
        req.withExclusiveStartKey(item)
      }
    }
    import collection.JavaConversions._

    val scanResp = aws.ddb.scan(req)
    val failures = scanResp.getItems.toList.map { item =>
      val taskId = item.get(hashKeyName).getS
      val rangeR = """([^:]+):(\d+)""".r
      val (instanceId, timestamp) = item.get(rangeKeyName).getS match {
        case rangeR(i, t) => (i, t.toLong)
        case _ => ("unknown", 0L)
      }
      val message = item.get(bodyKeyName).getS()
      Failure(taskId, timestamp, instanceId, message)
    }

    
    val lastEvKey = scanResp.getLastEvaluatedKey

    val newLastKey = if(lastEvKey != null && lastEvKey.containsKey(hashKeyName) && lastEvKey.containsKey(rangeKeyName)) {
      Some((lastEvKey.get(hashKeyName).getS(), lastEvKey.get(rangeKeyName).getS()))
    } else {
      None
    }

    (newLastKey, failures)
    
  }


}
