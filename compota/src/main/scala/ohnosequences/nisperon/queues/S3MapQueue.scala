package ohnosequences.nisperon.queues

import ohnosequences.nisperon._
import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.nisperon.logging.{ConsoleLogger, Logger}


class S3MapQueueAbstract[K, V](val aws: AWS, name: String, monoid: Monoid[V], val kSerializer: Serializer[K], vSerializer: Serializer[V],
                               val incrementSerializer: IncrementalSerializer[V], val prefix: ObjectAddress)
  extends MonoidQueue[Map[K, V]](name, new MapMonoid(monoid), new MapSerializer(kSerializer, vSerializer)) {

  override val merger: QueueMerger[Map[K, V]] = new S3MapQueueMerger(S3MapQueueAbstract.this)

  override def listChunk(limit: Int, lastKey: Option[String]): (Option[String], List[String]) = {
    (None, List[String]())
  }

  def listTerms(): (Traversable[(K, V)], Int) = {
    val ids = aws.s3.listObjects(prefix.bucket, prefix.key)
    var counter = 0
    (new Traversable[(K, V)] {
      override def foreach[U](f: ((K, V)) => U) {
        ids.foreach { address =>
          extractKey(address) match {
            case None => ()
            case Some(key) => {
              f((key, vSerializer.fromString(aws.s3.readWholeObject(address))))
              counter += 1
            }
          }
        }
      }
    }, counter)
  }


  def objectName(key: K, id: String) = {
    prefix / kSerializer.toString(key) / id
  }

  def extractKey(objectAddress: ObjectAddress): Option[K] = {
    val parts  = objectAddress.key.split('/').toList.reverse
    parts match {
      case Nil => None
      case id :: key :: rest => Some(kSerializer.fromString(key))
      case id :: Nil => None
    }

  }

  override def delete(): Unit = {}

  override def delete(id: String): Unit = {}

  override def read(id: String): Option[MA] = None

  override def list(): List[String] = List[String]()

  override def isEmpty: Boolean = true

  override def sqsQueueInfo(): Option[SQSQueueInfo] = None

  override def reset(): Unit = {}

  override def read(): Message[MA] = new ConstantMessage("", Map[K, V]())

  override def put(parentId: String, nispero: String, values: List[Map[K, V]]): Unit = {
    var c = 0
    for (map <- values) {
      c += 1
      for ((key, value) <- map if !monoid.unit.equals(value)) {
        val id = Tasks.generateChild(parentId, nispero, c)
        val address = objectName(key, id)
        aws.s3.putWholeObject(address, vSerializer.toString(value))
      }
    }
  }

  override def initWrite(): Unit = {}

  override def initRead(): Unit = {}
}


class S3MapQueueMerger[K, V](queue: S3MapQueueAbstract[K, V]) extends QueueMerger[Map[K, V]] {
  override def merge(destination: ObjectAddress) {
    val logger = new ConsoleLogger("S3MapQueueMerger")
    val merger = new MapIncrementalMerger(queue.aws.s3, queue.kSerializer, queue.incrementSerializer, logger)
    val (items, size) = queue.listTerms()
    merger.merge(destination, items, Some(size))

  }
}