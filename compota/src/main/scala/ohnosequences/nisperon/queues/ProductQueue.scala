package ohnosequences.nisperon.queues

import ohnosequences.nisperon.{Monoid, Serializer, ProductMonoid}
import ohnosequences.nisperon.Tasks
import ohnosequences.awstools.s3.ObjectAddress

//todo triple
class ProductSerializer[X, Y](x: Monoid[X], y: Monoid[Y]) extends Serializer[(X, Y)] {
  def fromString(s: String): (X, Y) = (x.unit, y.unit)

  def toString(t: (X, Y)): String = ""
}

class ProductMessage[X, Y](mx: Message[X], my: Message[Y]) extends Message[(X, Y)] {

  val id: String = mx.id + "," + my.id

  def value(): (X, Y) = (mx.value(), my.value())

  def delete() {
    mx.delete()
    my.delete()
  }

  def changeMessageVisibility(secs: Int) {
    mx.changeMessageVisibility(secs)
    my.changeMessageVisibility(secs)
  }
}

object ProductQueue {
  def flatQueue(queue: MonoidQueueAux): List[MonoidQueueAux] = {
    queue match {
      case ProductQueue(q1, q2) => flatQueue(q1) ++ flatQueue(q2)
      case q => List(q)
    }
  }
}

case class ProductQueue[X, Y](xQueue: MonoidQueue[X], yQueue: MonoidQueue[Y])
  extends MonoidQueue[(X, Y)](xQueue.name + "_" + yQueue.name, new ProductMonoid(xQueue.monoid, yQueue.monoid), new ProductSerializer[X, Y](xQueue.monoid, yQueue.monoid)) {


  override val merger: QueueMerger[(X, Y)] = new QueueMerger[(X, Y)] {
    override def merge(destination: ObjectAddress) {}
  }

  def nisperoX(nispero: String) = nispero + "_1"

  def nisperoY(nispero: String) = nispero + "_2"

  def list(): List[String] = xQueue.list() ++ yQueue.list()

  //todo it's wrong!
  def read(id: String): Option[(X, Y)] = {
    // id.nispero_2_nn


    Tasks.getOperand(id) match {
      case Some(1) => xQueue.read(id).map(_ -> yQueue.monoid.unit)
      case Some(2) => yQueue.read(id).map(xQueue.monoid.unit -> _)
      case _ => None
    }


    //val pairP = """"""


//    xQueue.read(xId(id)) match {
//      case Some(x) => Some(x -> yQueue.monoid.unit)
//      case None => yQueue.read(yId(id)) match {
//        case Some(y) => Some(xQueue.monoid.unit -> y)
//        case None => None
//      }
//      case None => None
//    }
//    (xQueue.read(xId(id)), yQueue.read(yId(id))) match {
//      case (Some(x), Some(y)) => Some(x, y)
//      case _ => None
//    }
//    (xQueue.read(id).getOrElse(xQueue.monoid.unit), yQueue.read(id).getOrElse(yQueue.monoid.unit)) match {
//      case (x, y) if x.equals(xQueue.monoid.unit) && y.equals(yQueue.monoid.unit) => None
//      case s => Some(s)
//    }
  }
  def delete(id: String) {
    Tasks.getOperand(id) match {
      case Some(1) => xQueue.delete(id)
      case Some(2) => xQueue.delete(id)
      case _ => ()
    }
  }

  def delete() {
    xQueue.delete()
    yQueue.delete()
  }

  def initRead() {
    xQueue.initRead()
    yQueue.initRead()
  }

  def initWrite() {
    xQueue.initWrite()
    yQueue.initWrite()
  }

  //for normal pid_nispero_2_idd
  def put(parentId: String, nispero: String, values: List[(X, Y)]) {
    xQueue.put(parentId, nispero + "_1", values.map(_._1))
    yQueue.put(parentId, nispero + "_2", values.map(_._2))
  }

  // todo fix his api!!
  def reset() {
    xQueue.reset()
    yQueue.reset()
  }

  //todo reading from product queue
  def read(): Message[(X, Y)] = {
    throw new Error("unreadable product queue!")
    new ProductMessage(xQueue.read(), yQueue.read())
  }

  def isEmpty: Boolean = xQueue.isEmpty && yQueue.isEmpty

  def sqsQueueInfo(): Option[SQSQueueInfo] = {
    None
  }

  //todo fix it!
  //override type Listening = xQueue.Listening

  //override def listNextChunk(limit: Int, listening: Listening): (Listening, List[String]) = xQueue.listNextChunk(limit, listening)

  //todo write a lot of batches!!!
  //todo but what does mean batch for s3??????????????
  //do we realy need this???
  //todo product queue is a very special thing and should have support on many levels....

  override def listChunk(limit: Int, lastKey: Option[String]): (Option[String], List[String]) ={
    throw new Error("list chunk on a product queue")
    xQueue.listChunk(limit, lastKey)
  }


}

