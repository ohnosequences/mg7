package ohnosequences.nisperon

import ohnosequences.nisperon.queues.{SQSQueue}
import org.clapper.avsl.Logger
import ohnosequences.nisperon.logging.FailTable

case class SNSMessage(Message: String)

case class ManagerCommand(command: String, arg: String)

  abstract class ManagerAux {

  val nisperoConfiguration: NisperoConfiguration

  val aws: AWS

  val logger = Logger(this.getClass)


  def runControlQueueHandler() {
    //it is needed for sns redirected messages
    val controlQueue = new SQSQueue[ManagerCommand](aws.sqs.sqs, nisperoConfiguration.controlQueueName, new JsonSerializer[ManagerCommand]())
    val reader = controlQueue.getSyncReader(true)

    val controlTopic = aws.sns.createTopic(nisperoConfiguration.nisperonConfiguration.controlTopic)
    val controlQueueWrap = aws.sqs.createQueue(controlQueue.name)
    controlTopic.subscribeQueue(controlQueueWrap)

    var stopped = false


    val failTable = new FailTable(aws, nisperoConfiguration.nisperonConfiguration.errorTable)

    while(!stopped) {
      val m0 = reader.read
      val command: ManagerCommand= m0.value()

      command match {
        case ManagerCommand("undeploy", _) => {
          logger.info(nisperoConfiguration.name + " undeployed")
          stopped = true

          Nisperon.unsafeAction(
            "deleting workers group",
            aws.as.deleteAutoScalingGroup(nisperoConfiguration.workersGroupName),
            logger
          )


          Nisperon.unsafeAction(
            "deleting control queue",
            controlQueueWrap.delete(),
            logger
          )

          Nisperon.unsafeAction(
            "deleting manager group",
            aws.as.deleteAutoScalingGroup(nisperoConfiguration.managerGroupName),
            logger
          )

        }
        case _ => {
          Nisperon.reportFailure(aws, nisperoConfiguration.nisperonConfiguration, "manager", new Error("wrong manager command " +command), terminateInstance = false, failTable = failTable)

        }
      }
      reader.reset()
    }
  }

}

class Manager(val aws: AWS, val nisperoConfiguration: NisperoConfiguration) extends ManagerAux {

}
