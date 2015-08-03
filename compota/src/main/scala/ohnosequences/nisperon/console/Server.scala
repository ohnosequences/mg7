package ohnosequences.nisperon.console

import unfiltered.netty.Https
import unfiltered.response._
import unfiltered.request.{BasicAuth, Path, Seg, GET}
import unfiltered.netty.cycle.{Plan, SynchronousExecution}
import unfiltered.netty.{Secured, ServerErrorResponse}
import java.io.{FileInputStream, ByteArrayInputStream, File}
import unfiltered.Cycle
import unfiltered.response.ResponseString
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest
import collection.JavaConversions._
import ohnosequences.nisperon.Nisperon
import org.clapper.avsl.Logger
import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.nisperon.logging.S3Logger
import ohnosequences.nisperon.logging.InstanceLogging
import ohnosequences.nisperon.queues.S3QueueAbstract


trait Users {
  def auth(u: String, p: String): Boolean
}

case class Auth(users: Users) {
  def apply[A, B](intent: Cycle.Intent[A, B]) =
    Cycle.Intent[A, B] {
      case req@BasicAuth(user, pass) if users.auth(user, pass) =>
        println(req.uri)
        Cycle.Intent.complete(intent)(req)
      case _ =>
        Unauthorized ~> WWWAuthenticate( """Basic realm="/"""")
    }
}


class ConsolePlan(name: String, users: Users, console: Console) extends Plan
with Secured // also catches netty Ssl errors
with SynchronousExecution
with ServerErrorResponse {
  val aws =  console.nisperon.aws
  val as =  console.nisperon.aws.as.as
  val logger = Logger(this.getClass)

  def intent = Auth(users) {
    case GET(Path("/")) => {


      val mainPage = main().mkString
        .replace("@main", console.nisperonInfo.toString())
        .replace("@sidebar", console.sideBar().toString())
        .replace("$name$", console.nisperon.nisperonConfiguration.id)

      HtmlCustom(mainPage)
    }

    case GET(Path("/undeploy")) => {
      console.nisperon.sendUndeployCommand("adhoc", force = true)
      ResponseString("undeploy message was sent")
    }

    case GET(Path("/errors")) => {


      val mainPage = main().mkString
        .replace("@main", console.errors().toString())
        .replace("@sidebar", console.sideBar().toString())
        .replace("$name$", console.nisperon.nisperonConfiguration.id)

      HtmlCustom(mainPage)
    }

    case GET(Path(Seg("failures" :: Nil))) => {
      try {
        ResponseString(console.listErrors().toString())
      } catch {
        //todo fix this raw reporting
        case t: Throwable => ResponseString(console.error(t.toString).toString())
      }
    }

    case GET(Path(Seg("failures" :: lastHash :: lastRange :: Nil))) => {
      try {

        ResponseString(console.listErrors(Some((lastHash, lastRange))).toString())
      } catch {
        //todo fix this raw reporting
        case t: Throwable => ResponseString(console.error(t.toString).toString())
      }
    }

    case GET(Path("/shutdown")) => {
      console.server.shutdown()
      ResponseString("ok")
    }

    case GET(Path(Seg("queue" :: queueName ::  "messages" :: Nil))) => {
      try {
        ResponseString(console.listMessages(queueName).toString())
      } catch {
        //t.printStackTrace()
        case t: Throwable =>t.printStackTrace(); ResponseString(console.error(t.toString).toString())
      }
    }



    case GET(Path(Seg("queue" :: queueName ::  "messages" :: lastKey :: Nil))) => {
      try {
        ResponseString(console.listMessages(queueName, Some(lastKey)).toString())
      } catch {
       // t.printStackTrace()
        case t: Throwable => t.printStackTrace(); ResponseString(console.error(t.toString).toString())
      }
    }

    case GET(Path(Seg("instanceLog" :: instanceId :: Nil))) => {
      val log = InstanceLogging.getLocation(console.nisperon.nisperonConfiguration, instanceId)
      Redirect(aws.s3.generateTemporaryURL(log, 60 * 5))
    }

    case GET(Path(Seg("log" :: id :: Nil))) => {
      val prefix = S3Logger.prefix(console.nisperon.nisperonConfiguration, id)
      Redirect(aws.s3.generateTemporaryURL(S3Logger.log(prefix), 60 * 5))
    }

    case GET(Path(Seg("queue" :: queueName ::  "deleteMessage" :: id :: Nil))) => {
      console.queues.get(queueName) match {
        case None => ResponseString(console.errorTr("queue " + queueName + " doesn't exist", 4).toString())
        case Some(queue) => {
          try {
            queue.delete(id)
            ResponseString(console.infoTr("deleted", 4, Some("deletedMessage")).toString())
          } catch {
            case t: Throwable => ResponseString(console.errorTr(t.toString, 4).toString())
          }
        }
      }
    }

    case GET(Path(Seg("queue" :: queueName ::  "message" :: id :: Nil))) => {
      try {

        console.queues.get(queueName) match {
          case None => NotFound
          case Some(queue) if queue.isInstanceOf[S3QueueAbstract[_]] => {
            Redirect(aws.s3.generateTemporaryURL(ObjectAddress(queueName, id), 60 * 5))
          }
          case Some(queue) => queue.readRaw(id) match {
            case None => NotFound
            case Some(s) => ResponseString(s)
          }
        }
       // ResponseString(console.listMessages(queueName, Some(lastKey)).toString())
      } catch {
        case t: Throwable => {
          logger.warn(t.toString)
          t.printStackTrace()
          ResponseString(console.error(t.toString).toString())
        }
      }
    }

    case GET(Path(Seg("terminate" :: id ::  Nil))) => {
      try {
        aws.ec2.terminateInstance(id)
        ResponseString("""<div class="alert alert-success">terminated</div>""")
      } catch {
        case t: Throwable => ResponseString(console.error(t.toString).toString())
      }
    }

    case GET(Path(Seg("ssh" :: id ::  Nil))) => {
      val ssh: Option[String] = try {
        aws.ec2.getInstanceById(id).flatMap(_.getSSHCommand())
      } catch {
        case t: Throwable => None
      }

      ssh match {
        case Some(c) => ResponseString(console.info(c).toString())
        case None => ResponseString(console.error("couldn't get ssh command").toString())
      }
    }

    case GET(Path(Seg("nispero" :: nispero :: "workerInstances" ::  Nil))) => {
      ResponseString(console.workersInfo(nispero).toString())
    }


    case GET(Path(Seg("nispero" :: nispero :: Nil))) => {
      // val main =  getClass.getResourceAsStream("/console/main.html")


      val mainPage = main().mkString
        .replace("@main", console.nisperoInfo(nispero).toString())
        .replace("@sidebar", console.sideBar().toString())
        .replace("$name$", console.nisperon.nisperonConfiguration.id)
        .replace("$nispero$", nispero)

      HtmlCustom(mainPage)
    }

    case GET(Path("/main.css")) => {
      val main = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/console/main.css")).mkString
      CssContent ~> ResponseString(main)
    }
  }

  def main(): String = {
    scala.io.Source.fromInputStream(getClass.getResourceAsStream("/console/main.html")).mkString
  }

}

case class HtmlCustom(s: String) extends ComposeResponse(HtmlContent ~> ResponseString(s))

//todo add page with task: log, parent, failures ...
class Server(nisperon: Nisperon) {
  val password = nisperon.nisperonConfiguration.password

  object users extends Users {
    override def auth(u: String, p: String): Boolean = u.equals("nispero") && p.equals(password)
  }

  val console = new Console(nisperon, Server.this)



  def shutdown() {
    Runtime.getRuntime().halt(0)
  }


  def start() {

    import scala.sys.process._
    val keyConf = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/console/keytoolConf.txt")).mkString.replace("$password$", password)
   // println(keyConf)
    val is = new ByteArrayInputStream(keyConf.getBytes("UTF-8"))
    try {
      ("keytool -keystore keystore -alias netty  -genkey -keyalg RSA -storepass $password$".replace("$password$", password) #< is).!
    } catch {
      case t: Throwable =>
        ("keytool7 -keystore keystore -alias netty  -genkey -keyalg RSA -storepass $password$".replace("$password$", password) #< is).!
    }

    println("server started")
    System.setProperty("netty.ssl.keyStore", "keystore")
    System.setProperty("netty.ssl.keyStorePassword", password)
    try {
      Https(443).handler(new ConsolePlan(nisperon.nisperonConfiguration.id, users, console)).start()
    } catch {
      case t: Throwable => {
        println("trying to bind to localhost")
        Https(443, "localhost").handler(new ConsolePlan(nisperon.nisperonConfiguration.id, users, console)).start()
      }
    }


  }

}
