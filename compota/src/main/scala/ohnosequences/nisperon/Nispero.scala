package ohnosequences.nisperon

import ohnosequences.nisperon.queues.{MonoidQueueAux, MonoidQueue}
import ohnosequences.nisperon.bundles._

import ohnosequences.awstools.s3.ObjectAddress
import org.clapper.avsl.Logger
import ohnosequences.nisperon.logging.FailTable


trait NisperoAux {

  type IQ <: MonoidQueueAux

  type OQ <: MonoidQueueAux

  val inputQueue: IQ

  val outputQueue: OQ

  type I <: InstructionsAux

  val instructions: I

  val nisperoConfiguration: NisperoConfiguration

  type W <: WorkerAux

  val worker: W

  type M <: ManagerAux

  val manager: M

  val aws: AWS

  def installManager()

  def installWorker()

  def runManager()
}

class Nispero[Input, Output, InputQueue <: MonoidQueue[Input], OutputQueue <: MonoidQueue[Output]](
  val aws: AWS,
  val inputQueue: InputQueue,
  val outputQueue: OutputQueue,
  val instructions: Instructions[Input, Output],
 // val addressCreator: AddressCreator,
  val nisperoConfiguration: NisperoConfiguration

) extends NisperoAux {

  type IQ = InputQueue

  type OQ = OutputQueue

  type I = Instructions[Input, Output]

  type W =  Worker[Input, Output, InputQueue, OutputQueue]

  type M = Manager

  val worker = new Worker(aws, inputQueue, outputQueue, instructions, nisperoConfiguration)

  val manager = new Manager(aws,nisperoConfiguration)

  val logger = Logger(this.getClass)

  import ohnosequences.statika._
  import ohnosequences.statika.ami.AMI149f7863


  object instructionsBundle extends InstructionsBundle()

  object workerBundle extends WorkerBundle(instructionsBundle) {

    override def install[D <: AnyDistribution](distribution: D): InstallResults = {
      worker.runInstructions()
      success("worker finished")
    }
  }

  object managerDistribution extends ManagerDistribution(workerBundle) {
    import ohnosequences.statika.{AnyDistribution, InstallResults, success}

    val logger = Logger(this.getClass)

    val metadata = nisperoConfiguration.nisperonConfiguration.metadataBuilder.build("worker", nisperoConfiguration.name, nisperoConfiguration.nisperonConfiguration.workingDir)

    def test() {
      userScript(workerBundle)
    }

    def installWorker() {
      installWithDeps(workerBundle)
    }

    override def install[Dist <: AnyDistribution](distribution: Dist): InstallResults = {

      val failTable = new FailTable(aws, nisperoConfiguration.nisperonConfiguration.errorTable)


      try {
        val workersGroup = nisperoConfiguration.workerGroup

        logger.info("nispero " + nisperoConfiguration.name + ": generating user script")
        val script = userScript(worker)

        logger.info("nispero " + nisperoConfiguration.name + ": launching workers group")
        val workers = workersGroup.autoScalingGroup(
          name = nisperoConfiguration.workersGroupName,
          defaultInstanceSpecs = nisperoConfiguration.nisperonConfiguration.defaultInstanceSpecs,
          amiId = managerDistribution.ami.id,
          userData = script
        )

        aws.as.createAutoScalingGroup(workers)

        logger.info("starting control queue handler")

        manager.runControlQueueHandler()

        //todo tagging
      } catch {
        case t: Throwable =>
          Nisperon.reportFailure(aws, nisperoConfiguration.nisperonConfiguration, "manager", t, true, failTable)

      }



      success("manager finished")
    }

  }

  object nisperoDistribution extends NisperoDistribution(managerDistribution) {
    //  import ohnosequences.statika.{success}

    val logger = Logger(this.getClass)

    val metadata = nisperoConfiguration.nisperonConfiguration.metadataBuilder.build("manager", nisperoConfiguration.name)

    def runManager() {
      val managerGroup = nisperoConfiguration.nisperonConfiguration.managerGroupConfiguration

      logger.info("nispero " + nisperoConfiguration.name + ": generating user script")
      val script = userScript(managerDistribution)

      logger.info("nispero " + nisperoConfiguration.name + ": launching manager group")
      val managerASGroup = managerGroup.autoScalingGroup(
        name = nisperoConfiguration.managerGroupName,
        defaultInstanceSpecs = nisperoConfiguration.nisperonConfiguration.defaultInstanceSpecs,
        amiId = managerDistribution.ami.id,
        userData = script
      )

      aws.as.createAutoScalingGroup(managerASGroup)

      // success("nisperoDistribution finished")

    }

    def installManager() {
      installWithDeps(managerDistribution)
    }
  }

  def installManager() {
    nisperoDistribution.installManager()
  }

  def installWorker() {
    managerDistribution.installWorker()
  }

  def runManager() {
    nisperoDistribution.runManager()
  }






}
