package ohnosequences.nisperon.queues

import java.util.concurrent.ArrayBlockingQueue
import ohnosequences.nisperon.{AWS, Serializer, Monoid}
import org.clapper.avsl.Logger
import ohnosequences.awstools.s3.ObjectAddress


class S3Writer[T](aws: AWS, monoid: Monoid[T], queueName: String, serializer: Serializer[T], threads: Int = 1) {
  val batchSize = 1
  val bufferSize = batchSize * (threads + 1)
  val buffer = new ArrayBlockingQueue[(String, T)](bufferSize)

  @volatile var error = false
  @volatile var launched = false

  @volatile var errorMessage = ""
  val logger = Logger(this.getClass)


  def put(id: String, value: T) {
    if(error) throw new Error(errorMessage)
    buffer.put(id -> value)
  }

  def init() {
    if(!launched) {
      launched = true
      for (i <- 1 to threads) {
        new WriterThread(i).start()
      }
    }
  }

  def reportError(errorM: String) {
    errorMessage = errorM
    error = true
    buffer.clear()
  }

  def flush() {
    if(error) {
      error = false
      throw new Error(errorMessage)
    }
    for (i <- 1 to bufferSize * 2) {
      buffer.put("id" -> monoid.unit)
    }
    if(error) {
      error = false
      throw new Error(errorMessage)
    }
  }


  class WriterThread(id: Int) extends Thread("S3 writer " + id + " " + queueName) {
    setDaemon(true)
    override def run() {
      while(true) {
        try {
          val (id, value) = buffer.take()
          if (!value.equals(monoid.unit)) {
            aws.s3.putWholeObject(ObjectAddress(queueName, id), serializer.toString(value))
          }
        } catch {
          case t: Throwable => {
            val message = t.toString + " " + t.getMessage
            logger.warn(message)
            t.printStackTrace()
            reportError(message)
          }
        }
      }
    }
  }
}
