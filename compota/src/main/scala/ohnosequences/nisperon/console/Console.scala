package ohnosequences.nisperon.console

import ohnosequences.nisperon.{NisperoGraph, NisperoAux, Nisperon, Tasks}
import scala.xml.{Node, NodeSeq}
import com.amazonaws.services.autoscaling.model.AutoScalingGroup
import collection.JavaConversions._
import ohnosequences.nisperon.queues.{ProductQueue, MonoidQueueAux}
import scala.collection.mutable.HashMap
import org.clapper.avsl.Logger
import ohnosequences.nisperon.logging.{FailTable, S3Logger}
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException

//todo dynamodb error Warning! com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException: Status Code: 400, AWS Service: AmazonDynamoDBv2, AWS Request ID: UFL5RSPG7NOCUVOST2I3MELKMVVV4KQNSO5AEMVJF66Q9ASUAAJG, AWS Error Code: ResourceNotFoundException, AWS Error Message: Requested resource not found
case class Console(nisperon: Nisperon, server: Server) {

  val aws = nisperon.aws
  val as = aws.as.as


  val logger = Logger(this.getClass)

  val failTable = new FailTable(aws, nisperon.nisperonConfiguration.errorTable)

  val nisperoGraph = new NisperoGraph(nisperon.nisperos)

  val queues = nisperoGraph.queues

  def getWorkersAutoScaligGroup(nispero: NisperoAux): List[AutoScalingGroup] = {
    AWSTools.describeAutoScalingGroup(aws, nispero.nisperoConfiguration.workersGroupName)
  }

  def sideBar():  NodeSeq = {
    <ul class="nav nav-sidebar">
      {nisperosLinks()}
    </ul>
    <ul class="nav nav-sidebar">
      <li><a href="/errors">errors</a></li>
      <li><a href="#" class="undeploy">undeploy</a></li>
    </ul>
  }
//  <li><a href="/shutdown">shutdown</a></li>

  def nisperosLinks(): NodeSeq = {
    val l = for {(name, nispero) <- nisperon.nisperos}
    yield <li>
        <a href={"/nispero/" + name}>
          {name}
        </a>
      </li>
    l.toList
  }

  //      <thead>
  //        <tr>
  //          <th>property</th>
  //          <th>value</th>
  //        </tr>
  //      </thead>

  def workersPropertiesStatic(nispero: NisperoAux): NodeSeq = {
    <div> {
      warn(nispero.nisperoConfiguration.workersGroupName + " auto scaling group doesn't exist")
    }
    <table class="table table-striped topMargin20">
      <tbody>
        <tr>
          <td class="col-md-6">auto scaling group</td>
          <td class="col-md-6">
            {nispero.nisperoConfiguration.workersGroupName}
          </td>
        </tr>
        <tr>
          <td>minimum size</td>
          <td>
            {nispero.nisperoConfiguration.workerGroup.min}
          </td>
        </tr>
        <tr>
          <td>desired capacity</td>
          <td>
            {nispero.nisperoConfiguration.workerGroup.size}
          </td>
        </tr>
        <tr>
          <td>maximum size</td>
          <td>
            {nispero.nisperoConfiguration.workerGroup.max}
          </td>
        </tr>
      </tbody>
    </table>
   </div>
  }

  def workersProperties(group: AutoScalingGroup): NodeSeq = {
    //    <tr>
    //      <td>1,001</td>
    //      <td>Lorem</td>
    //    </tr>
    <table class="table table-striped topMargin20">
      <tbody>
        <tr>
          <td class="col-md-6">auto scaling group</td>
          <td class="col-md-6">
            {group.getAutoScalingGroupName}
          </td>
        </tr>
          <tr>
            <td>minimum size</td>
            <td>
              {group.getMinSize}
            </td>
          </tr>
          <tr>
            <td>desired capacity</td>
            <td>
              {group.getDesiredCapacity}
            </td>
          </tr>
          <tr>
            <td>maximum size</td>
            <td>
              {group.getMaxSize}
            </td>
          </tr>
      </tbody>
    </table>
  }

  def nisperonInfo: NodeSeq = {
    <div class="page-header">
      <h1>
        {nisperon.nisperonConfiguration.id}
      </h1>
    </div>
  }
//    <div class="alert alert-success">
//      put something here
//    </div>


  //          <p>
  //            <a class="btn btn-info" href="#" id="refresh">
  //              <i class="icon-refresh icon-white"></i>
  //              Refresh</a>
  //          </p>
  def nisperoInfo(nisperoName: String): NodeSeq = {

    nisperon.nisperos.get(nisperoName) match {
      case None => {
        <div class="alert alert-danger">
          {nisperoName + " doesn't exist"}
        </div>
      }
      case Some(nispero) =>
        val workersGroups = getWorkersAutoScaligGroup(nispero)
        <div class="page-header">
          <h1>
            {nisperon.nisperonConfiguration.id}<small>
            {nispero.nisperoConfiguration.name}
            nispero</small>
          </h1>
        </div>
        <div>
          {
            if (workersGroups.isEmpty) {
              workersPropertiesStatic(nispero)
            } else {
              workersProperties(workersGroups.get(0))
            }
          }
          </div>

          <div class="page-header">
            <h2>input</h2>
          </div>
          <div> {queueStatus(nispero.inputQueue)} </div>
          <div class="page-header">
            <h2>output</h2>
          </div>
          <div> {queueStatus(nispero.outputQueue)} </div>
          <div class="page-header">
            <h2>instances</h2>
          </div>
          <table class="table table-striped topMargin20">
            <tbody id="workerInstances">
              {workerInstances(workersGroups)}
            </tbody>
          </table>
    }
  }


  def queueStatus(queue: MonoidQueueAux): NodeSeq = {
    val res = ProductQueue.flatQueue(queue).flatMap(queueStatusFlatted)
    //println("flat: " + ProductQueue.flatQueue(queue))
    res
  }

  def sqsQueueInfo(queue: MonoidQueueAux): NodeSeq = queue.sqsQueueInfo() match {
    case None => warn("queue " + queue.name + " doesn't exist")
    case Some(queueInfo) => {
      <table class="table table-striped topMargin20">
        <tbody>
          <tr>
            <td class="col-md-6">
              SQS queue messages (approx)
            </td>
            <td class="col-md-6">
              {queueInfo.approx}
            </td>
          </tr>
          <tr>
            <td class="col-md-6">
              SQS messages in flight (approx)
            </td>
            <td class="col-md-6">
              {queueInfo.inFlight}
            </td>
          </tr>
        </tbody>
      </table>
    }
  }

  def queueStatusFlatted(queue: MonoidQueueAux): NodeSeq = {

    <div>
      <h3>{queue.name}</h3>
    {sqsQueueInfo(queue)}
    </div>
    <div>
    {
      try {
        if (queue.isEmpty) {
          xml.NodeSeq.Empty
        } else {
          <a class="btn btn-info showMessages" href="#" data-queue={queue.name}>
            <i class="icon-refresh icon-white"></i>
            Show messages
          </a>
        }
      } catch {
        case e: AmazonS3Exception if "NoSuchBucket".equals(e.getErrorCode) => warn("bucket " + queue.name + " doesn't exist")
        case e: ResourceNotFoundException => warn("table " + queue.name + " doesn't exist")
        case e: Throwable => warn(e.toString)
      }
    }
    </div>
  }


  //todo do deletedMessage lazy
  def listMessages(name: String, lastKey: Option[String] = None, limit: Int = 10): NodeSeq = {
    queues.get(name) match {
      case None => logger.warn("queue " + name + " doesn't exit"); error("queue " + name + " doesn't exit")
      case Some(queue) => {
        val (newLastKey, messages) = queue.listChunk(limit, lastKey)
        logger.info(messages.size + " messages")
        messages.map { id =>
          <tr data-lastkey={newLastKey.getOrElse("")}>
            <td class="col-md-3">
              {id}
            </td>
            <td class="col-md-3">
              <a href={"/queue/" + name + "/message/" + id}>download</a>
            </td>
            <td class="col-md-3">
              <a class="deleteMessage" data-queue={name} data-id={id} href="#">delete</a>
            </td>
            <td class="col-md-3">
              {getLogLink(id)}
            </td>
          </tr>
        }
      }
    }
  }

  def getLogLink(id: String): NodeSeq = {
    //logger.info("getLog(" + id + ")")

    val parent = Tasks.parent(id)
  //  val log = S3Logger.prefix(nisperon.nisperonConfiguration, parent)

    //if(aws.s3.objectExists(log)) {
      <a href={"/log/" + parent}>log</a>
   // } else {
    //  xml.NodeSeq.Empty
   // }
  }




//  def showMesage(queue: String, id: String): Option[String] = {
//    queues.get()
//  }

  def error(message: String): NodeSeq = {
    <div class="alert alert-danger">
      <button type="button" class="close" data-dismiss="alert">&times;</button>
      <strong>Error!</strong> {message}
    </div>
  }

  def info(message: String): NodeSeq = {
    <div class="alert alert-success">
      <button type="button" class="close" data-dismiss="alert">&times;</button>
      {message}
    </div>
  }

  def errorTr(message: String, cols: Int): NodeSeq = {
    <tr class="danger"><td colspan={cols.toString}>{message}</td></tr>
  }

  def warn(message: String): NodeSeq = {
    <div class="alert alert-warning">
      <button type="button" class="close" data-dismiss="alert">&times;</button>
      <strong>Warning!</strong> {message}
    </div>
  }

  def infoTr(message: String, cols: Int, additionalClass: Option[String]): NodeSeq = {
    additionalClass match {
      case None => <tr class="success"><td colspan={cols.toString}>{message}</td></tr>
      case Some(cls) => <tr class={"success " + cls}><td colspan={cols.toString}>{message}</td></tr>
    }
  }

  def workersInfo(nisperoName: String): NodeSeq = {


    nisperon.nisperos.get(nisperoName) match {
      case None => {
//        <div class="alert alert-danger">
//          {nisperoName + " doesn't exist"}
//        </div>
        xml.NodeSeq.Empty
      }
      case Some(nispero) => {
        workerInstances(getWorkersAutoScaligGroup(nispero))
      }
    }
  }

  //  <button class="btn btn-primary btn-lg" data-toggle="modal" data-target="#terminateInstanceModal">
  //    Terminate
  //  </button>
  // <a class="btn btn-danger terminate" href="#" id={inst.getInstanceId}><i class="icon-refresh icon-white"></i>Terminate</a>
  def workerInstances(groups: List[AutoScalingGroup]): NodeSeq = {
    groups.flatMap {
      group =>
        group.getInstances.toList.map {
          inst =>
            <tr>
              <td class="col-md-4">
                <a href={"/instanceLog/" + inst.getInstanceId}>{inst.getInstanceId}</a>
              </td>
              <td class="col-md-4">
                {inst.getLifecycleState}
              </td>
              <td class="col-md-4">
                <a class="btn btn-danger terminate" href="#" id={inst.getInstanceId}>
                  <i class="icon-refresh icon-white"></i>
                  Terminate</a>
                <a class="btn btn-info sshInstance" href="#" data-id={inst.getInstanceId}>
                  <i class="icon-refresh icon-white"></i>
                  Connect</a>
              </td>
            </tr>
        }
    }
  }

  def listErrors(lastKey: Option[(String, String)] = None, limit: Int = 10): NodeSeq = {
    val (newLastKey, failures) = failTable.failsChunk(lastKey, limit)
    logger.info(failures.size + " failures")
    failures.map { failure =>
      <tr data-lasthash={newLastKey.getOrElse(("", ""))._1} data-lastrange={newLastKey.getOrElse(("", ""))._2}>
        <td>
          {failure.taskId}
        </td>
        <td>
          <a href={"/instanceLog/" + failure.instanceId}>{failure.instanceId}</a>
        </td>
        <td>
          {failure.formattedTime()}
        </td>
        <td>
          {failure.message}
        </td>
      </tr>
    }
  }



  def errors(): NodeSeq = {
   // val (lastKey, failures) =

    <h2>Errors</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th class="col-md-3">task</th>
          <th class="col-md-3">instance</th>
          <th class="col-md-3">time</th>
          <th class="col-md-3">message</th>
        </tr>
      </thead>
      <tbody id ="errorsTableBody">
        {listErrors(None)}
      </tbody>
    </table>
    <p><a class="btn btn-info loadMoreErrors" href="#">
      <i class="icon-refresh icon-white"></i>
      Show more
    </a></p>

  }
}
