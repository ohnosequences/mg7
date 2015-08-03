package ohnosequences.nisperon

import ohnosequences.nisperon.queues._
import org.clapper.avsl.Logger
import ohnosequences.nisperon.logging.{InstanceLogging, FailTable, S3Logger}

//todo use failed table failed if failed from several machines

abstract class WorkerAux {

  type IQ <: MonoidQueueAux

  type OQ <: MonoidQueueAux

  val inputQueue: IQ

  val outputQueue: OQ

  val instructions: Instructions[inputQueue.MA, outputQueue.MA]

  val nisperoConfiguration: NisperoConfiguration

  val aws: AWS

  val logger = Logger(this.getClass)



  //todo add links to instance logs
  def runInstructions() {

    var start = true

    val failTable = new FailTable(aws, nisperoConfiguration.nisperonConfiguration.errorTable)

    try {
      logger.info("preparing instructions")
      val context: instructions.Context = instructions.prepare()


      try {
        logger.info("initializing queues")
        inputQueue.initRead()
        outputQueue.initWrite()
      } catch {
        case t: Throwable =>
          logger.error("error during initializing queues")
          start = false
          Nisperon.reportFailure(aws, nisperoConfiguration.nisperonConfiguration, "worker", t,  true, failTable, "error during initializing queues")
      }

      var startTime = 0L
      var endTime = 0L


      while (start) {

        var message: Message[inputQueue.MA] = null

        try {

          startTime = System.currentTimeMillis()
          message = inputQueue.read()
          endTime = System.currentTimeMillis()
          logger.info("message read in " + (endTime - startTime))


          logger.info("executing " + instructions + " instructions on " + message.id)
          //todo add check for empty messages
          //todo fix this check for a productqueue
          val prefix = S3Logger.prefix(nisperoConfiguration.nisperonConfiguration, message.id)
          val s3logger = new S3Logger(nisperoConfiguration.name, aws, prefix, nisperoConfiguration.nisperonConfiguration.workingDir)

          startTime = System.currentTimeMillis()
          val output = instructions.solve(message.value(), s3logger, context)
          endTime = System.currentTimeMillis()
          logger.info("executed in " + (endTime - startTime))

          s3logger.close()


          startTime = System.currentTimeMillis()
          outputQueue.put(message.id, nisperoConfiguration.name, output)
          endTime = System.currentTimeMillis()
          logger.info("message written in " + (endTime - startTime))
          deleteMessage(message)


          //todo fix reset
        } catch {
          case t: Throwable => {
            if (message == null) {
              Nisperon.reportFailure(aws, nisperoConfiguration.nisperonConfiguration, "worker", t,  true, failTable, "error during reading from queue")
            } else {
              if (failTable.fails(message.id) > nisperoConfiguration.nisperonConfiguration.errorThreshold) {
                logger.error("message " + message.id + " failed more than " + nisperoConfiguration.nisperonConfiguration.errorThreshold)
                deleteMessage(message)
              }
              Nisperon.reportFailure(aws, nisperoConfiguration.nisperonConfiguration, message.id, t, false, failTable)
            }
            inputQueue.reset()
          }
        }
      }

    } catch {
      case t: Throwable =>
        logger.error("error during preparing instructions")
        start = false
        Nisperon.reportFailure(aws, nisperoConfiguration.nisperonConfiguration, "worker", t, true, failTable, "error during preparing instructions")

    }
  }



  def deleteMessage(message: Message[inputQueue.MA]) {
    var deleted = false
    var attempt = 0
    while (!deleted) {
      try {
        attempt += 1
        message.delete()
        deleted = true
      } catch {
        case t: Throwable => {
          if (attempt < 10) {
            logger.warn("couldn't delete massage " + message.id)
            Thread.sleep(attempt * 100)
          } else {
            throw t
          }
        }
      }
    }
  }
}


class Worker[Input, Output, InputQueue <: MonoidQueue[Input], OutputQueue <: MonoidQueue[Output]]
(val aws: AWS, val inputQueue: InputQueue, val outputQueue: OutputQueue, val instructions: Instructions[Input, Output], val nisperoConfiguration: NisperoConfiguration) extends WorkerAux {
  type IQ = InputQueue

  type OQ = OutputQueue

}
