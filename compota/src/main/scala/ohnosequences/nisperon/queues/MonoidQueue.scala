package ohnosequences.nisperon.queues

import ohnosequences.nisperon._
import java.util.concurrent.ArrayBlockingQueue
import com.amazonaws.services.sqs.model.{SendMessageBatchRequestEntry, SendMessageBatchRequest}
import java.util
import org.clapper.avsl.Logger

import scala.collection.JavaConversions._



trait MonoidQueueAux {
  type MA
  val monoid: Monoid[MA]

  val serializer: Serializer[MA]

  val merger: QueueMerger[MA]

  val name: String

  //todo message class

  def initRead()

  def initWrite()

  def put(parentId: String, nispero: String, values: List[MA])

  def read(): Message[MA]


  def reset()

  //tracking stuff

  def sqsQueueInfo(): Option[SQSQueueInfo]

  def isEmpty: Boolean

  def list(): List[String]

  def read(id: String): Option[MA]

  def readRaw(id: String): Option[String] = {
    read(id).map(serializer.toString)
  }

  def delete(id: String)

  def delete()

  //type LastK
  def listChunk(limit: Int, lastKey: Option[String] = None): (Option[String], List[String])


}



abstract class MonoidQueue[M](val name: String, val monoid: Monoid[M], val serializer: Serializer[M]) extends MonoidQueueAux {
  type MA = M
}


//
//class BufferedMonoidQueue[M](aws: AWS, name: String, monoid: Monoid[M], val serializer: Serializer[M]) extends MonoidQueue[M](name, monoid) {
//
//  val logger = Logger(this.getClass)
//
//  @volatile var queue: Option[ohnosequences.awstools.sqs.Queue] = None
//
//  val outBufferSize = 10
//  val outBuffer = new ArrayBlockingQueue[M](outBufferSize)
//
//  val inBufferSize = 20
//  val inBuffer = new ArrayBlockingQueue[ohnosequences.awstools.sqs.Message](inBufferSize)
//
//  @volatile var failMessage: Option[String] = None
//
//  def fail(message: String) {
//   // flush()
//    failMessage = Some(message)
//  }
//
//  object writer extends Thread(name + "_writer") {
//    override def run() {
//      while (failMessage.isEmpty) {
//        try {
//
//          val entries = new util.ArrayList[SendMessageBatchRequestEntry]()
//
//          (1 to outBufferSize).map { n =>
//            val m = outBuffer.take()
//            if (!m.equals(monoid.unit)) {
//              entries.add(new SendMessageBatchRequestEntry()
//                .withId(n.toString)
//                .withMessageBody(serializer.toString(m))
//              )
//            }
//          }
//
//          if(!entries.isEmpty) {
//            aws.sqs.sqs.sendMessageBatch(new SendMessageBatchRequest()
//              .withQueueUrl(queue.get.url)
//              .withEntries(entries)
//            )
//          } else {
//            logger.warn("skipping empty batch")
//          }
//        } catch {
//          case t: Throwable => fail(t.toString + " message: " + t.getMessage)
//        }
//      }
//    }
//  }
//
//  object reader extends Thread(name + "_reader") {
//
//    override def run() {
//      while(failMessage.isEmpty) {
//        //println("reader started")
//        try {
//          val messages = queue.map(_.receiveMessages(10)).getOrElse(List[ohnosequences.awstools.sqs.Message]())
//
//          messages.foreach { m =>
//            inBuffer.put(m)
//          }
//
//          if(messages.isEmpty) {
//            Thread.sleep(100)
//          } else {
//            Thread.sleep(5)
//          }
//        } catch {
//          case t: Throwable => fail(t.toString + " message: " + t.getMessage)
//        }
//      }
//    }
//  }
//
//  override def toString() = {
//    queue.map(_.getArn).toString
//  }
//
//  def put(value: M) {
//    failMessage match {
//      case None => {
//        if(!value.equals(monoid.unit)) {
//          outBuffer.put(value)
//        }
//      }
//      case Some(m) => throw new Error(m)
//    }
//  }
//
//  def flush() {
//    //logger.info("")
//    (1 to outBufferSize).map { n =>
//      outBuffer.put(monoid.unit)
//    }
//  }
//
//  def init() {
//    if(queue.isEmpty) {
//      queue = Some(aws.sqs.createQueue(name))
//      queue.foreach(_.setVisibilityTimeout(10))
//      writer.setDaemon(true)
//      writer.start()
//      reader.setDaemon(true)
//      reader.start()
//      visibilityExtender.setDaemon(true)
//      visibilityExtender.start()
//    }
//  }
//
//  def clear() {
//    visibilityExtender.clear()
//  }
//
//
//
//  def read(): Message[M] = {
//  //  println("read")
//    failMessage match {
//      case None =>
//
//        var message: ohnosequences.awstools.sqs.Message = null
//
//        var taken = false
//        while(!taken) {
//          message = inBuffer.take()
//          //println("taked: " + message)
//          //check timeout
//          try {
//            message.changeVisibilityTimeout(20)
//            taken = true
//          } catch {
//            case t: Throwable => logger.warn("skipping expired message")
//          }
//        }
//
//        visibilityExtender.addMessage(message)
//
//        new Message[M] {
//          def extendVisibility(secs: Int): Unit = message.changeVisibilityTimeout(secs)
//
//          def value(): M = serializer.fromString(message.body)
//
//          def delete() {
//            visibilityExtender.deleteMessage(message)
//            queue.foreach(_.deleteMessage(message))
//          }
//      }
//      case Some(m) => throw new Error(m)
//    }
//  }
//
//  object visibilityExtender extends Thread("extender_" + name) {
//
//    val messages = new java.util.concurrent.ConcurrentHashMap[String, ohnosequences.awstools.sqs.Message]()
//
//
//    def addMessage(m: ohnosequences.awstools.sqs.Message) {
//
//    //  println("messsage put")
//      messages.put(m.receiptHandle, m)
//    }
//
//    def deleteMessage(m: ohnosequences.awstools.sqs.Message) {
//      messages.remove(m.receiptHandle)
//    }
//
//    def clear() {
//      messages.clear()
//    }
//
//    override def run() {
//     // println(this.getName + " started")
//
//      while(failMessage.isEmpty) {
//        messages.values().foreach { m =>
//          try {
//           // println("extending timeout for " + m.receiptHandle)
//            m.changeVisibilityTimeout(20)
//          } catch {
//            case t: Throwable => {
//              println("warning: invalid id" + t.getLocalizedMessage)
//              messages.remove(m.receiptHandle)
//            }
//          }
//        }
//        Thread.sleep(10 * 1000)
//      }
//    }
//  }
//
//
//}




