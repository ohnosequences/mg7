package ohnosequences.nisperon.queues

import ohnosequences.nisperon.logging.ConsoleLogger
import ohnosequences.awstools.s3.{S3, ObjectAddress}
import ohnosequences.nisperon.{MonoidInMemoryMerger, Nisperon, Serializer}

////todo write to other queue!!! ??
////todo use previous results!!


object QueueMerger {
  def destination(results: ObjectAddress, queue: MonoidQueueAux): ObjectAddress = {
    results / queue.name
  }
}

trait QueueMerger[M] {




  def merge(destination: ObjectAddress)
}

class DefaultQueueMerger[M](queue: MonoidQueue[M], s3: S3) extends QueueMerger[M] {

  val logger = new ConsoleLogger("merger")

//  def mergeDestination(nisperon: Nisperon, queue: MonoidQueueAux): ObjectAddress = {
//    ObjectAddress(nisperon.nisperonConfiguration.bucket, "results/" + queue.name)
//  }

  def merge(destination: ObjectAddress) = {
    logger.info("retrieving messages from the queue " + queue.name)
    val ids = queue.list()
    val lazyParts = new Traversable[M] {
      override def foreach[U](f: (M) => U): Unit = {
        ids.foreach { id =>
          queue.read(id) match {
            case None => logger.error("message " + id + " not found")
            case Some(part) => f(part)
          }
        }
      }
    }
    val merger = new MonoidInMemoryMerger(s3, queue.monoid, queue.serializer, logger)
    merger.merge(destination, lazyParts, Some(ids.size))
  }


}
