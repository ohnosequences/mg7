package ohnosequences.nisperon

import org.clapper.avsl.Logger
import ohnosequences.nisperon.queues.{DefaultQueueMerger, QueueMerger, SQSQueue}
import ohnosequences.nisperon.logging.FailTable
import ohnosequences.nisperon.console.Server


class MetaManager(nisperon: Nisperon) {
  import nisperon._

  val logger = Logger(this.getClass)

  def printURL(domain: String, port: Int = 443): String = port match {
    case 443 => "https://" + domain
    case 80 => "http://" + domain
    case p => domain + ":" + port
  }

  def currentAddress: String = {
    aws.ec2.getCurrentInstance.flatMap {_.getPublicDNS()}.getOrElse("<undefined>")
  }


  //todo remove notification topic
 // todo use sync queue reader!!!
  def run() {

    val failTable = new FailTable(nisperon.aws, nisperon.nisperonConfiguration.errorTable)

    try {
      logger.info("starting console")
      new Thread("console") {
        override def start() {
          val message = new StringBuilder()
          message.append(nisperonConfiguration.id + " started\n")
          message.append("\n")
          message.append("console address: " + printURL(currentAddress) + "\n")
          message.append("user: nispero\n")
          message.append("password: " + nisperonConfiguration.password + "\n")
          nisperon.notification(nisperonConfiguration.id + " started", message.toString())
          new Server(nisperon).start()


        }
      }.start()
    } catch {
      case t: Throwable =>   Nisperon.reportFailure(nisperon.aws, nisperon.nisperonConfiguration, "metamanager", t, true, failTable, "console")
    }

    try {

      logger.info("metamanager started")

      val queueName = nisperon.nisperonConfiguration.metamanagerQueue

      //todo is it needed?
      val controlTopic = nisperon.aws.sns.createTopic(nisperon.nisperonConfiguration.controlTopic)

      val queueWrap = nisperon.aws.sqs.createQueue(queueName)


      val controlQueue = new SQSQueue[List[MetaManagerCommand0]](aws.sqs.sqs, queueName, new JsonSerializer[List[MetaManagerCommand0]]())
      val reader = controlQueue.getSyncReader()

      val writer = controlQueue.getWriter(new ListMonoid[MetaManagerCommand0]())



      val terminationDaemon = new TerminationDaemon(nisperon)
      terminationDaemon.start()

      var stopped = false
      while (!stopped) {
        val m0 = reader.read

        //todo add repeats
        //todo fix this head
        //todo add handling for seve
        m0.value().foreach {
          command0 =>
            logger.info("parsing message " + command0)
            try {
              command0.unMarshall() match {
                case None => {
                  throw new Error("unknown message " + command0)
                }
                case Some(DeleteResources(reason)) => {
                  //1 delete queues:
                  Nisperon.unsafeAction("deleting queues",
                    if (!nisperonConfiguration.removeAllQueues) {
                      nisperon.checkQueues() match {
                        case Right(queues) => {
                          queues.foreach{queue =>
                            Nisperon.unsafeAction("deleting queue " +queue.name, queue.delete(), logger)
                          }
                        }
                        case Left(queue) => logger.info(queue.name + " isn't empty")
                      }
                    } else {
                      nisperon.nisperos.values.foreach { nispero =>
                        Nisperon.unsafeAction("deleting queue " + nispero.inputQueue.name, nispero.inputQueue.delete(), logger)
                        Nisperon.unsafeAction("deleting queue " + nispero.outputQueue.name, nispero.outputQueue.delete(), logger)
                      }
                    }, logger
                  )
                  //2 notification
                  //todo  add stuff here
                  Nisperon.unsafeAction("sending notification",
                  nisperon.notification(nisperon.nisperonConfiguration.id + " terminated", "reason: " + reason), logger)

                  Nisperon.unsafeAction("deleting metamanager queue",
                  queueWrap.delete(), logger)

                  //todo we assume here that all messages were delivered
                  Nisperon.unsafeAction("deleting control topic",
                  controlTopic.delete(), logger)

                  Nisperon.unsafeAction("deleting auto scaling group",
                  aws.as.deleteAutoScalingGroup(nisperonConfiguration.metamanagerGroup), logger)

                  stopped = true

                }

                case Some(Undeploy(reason, true)) => {
                  //delete resources ...
                  nisperon.sendUndeployCommandToManagers(reason)
                  writer.write("undeployActions", List(UndeployActions(reason, force = true).marshall()))
                  writer.flush()

                }
                case Some(Undeploy(reason, false)) => {
                  //delete resources ...
                  nisperon.sendUndeployCommandToManagers(reason)
                  writer.write("merge", List(MergeQueues(reason).marshall()))
                  writer.flush()
                }
                case Some(MergeQueues(reason)) => {
                  logger.info("merging queues")
                  nisperon.mergingQueues.foreach {
                    queue =>
                      queue.merger.merge(QueueMerger.destination(nisperonConfiguration.results, queue))
                  }
                  writer.write("undeployActions", List(UndeployActions(reason, force = false).marshall()))
                  writer.flush()
                }

                case Some(UndeployActions(reason, force)) => {
                  logger.info("running undeploy actions")
                  nisperon.undeployActions(force)
                  writer.write("deleteResources", List(DeleteResources(reason).marshall()))
                  writer.flush()
                }
              }
              m0.delete()
            } catch {
              case t: Throwable => {
                if (failTable.fails(m0.id) > nisperonConfiguration.errorThreshold) {
                  logger.error("message " + m0.id + " failed more than " + nisperonConfiguration.errorThreshold)
                  m0.delete()
                } else {
                  Nisperon.reportFailure(nisperon.aws, nisperon.nisperonConfiguration, "metamanager", t, terminateInstance = false, failTable = failTable)
                }
              }
            }
        }
      }
    } catch {
      case t: Throwable => {
        logger.error("fatal error")
        Nisperon.reportFailure(nisperon.aws, nisperon.nisperonConfiguration, "metamanager", t, terminateInstance = true, failTable = failTable)
      }
    }
  }

}

