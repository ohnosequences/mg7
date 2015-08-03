package ohnosequences.nisperon


//todo find way to do it normally
case class MetaManagerCommand0(command: String, arg1: String = "", arg2: String = "") {
  def unMarshall(): Option[MetaManagerCommand] = command match {
    case "merge" => Some(MergeQueues(arg1))
    case "undeploy" => Some(Undeploy(arg1, arg2.equals(true.toString)))
    case "deleteResources" => Some(DeleteResources(arg1))
    case "undeployActions" => Some(UndeployActions(arg1, arg2.equals(true.toString)))
    case _ => None
  }
}

abstract class MetaManagerCommand {
  def marshall(): MetaManagerCommand0
}


case class MergeQueues(reason: String) extends MetaManagerCommand {
  override def marshall(): MetaManagerCommand0 = MetaManagerCommand0("merge", reason)
}

case class Undeploy(reason: String, force: Boolean) extends MetaManagerCommand {
  override def marshall(): MetaManagerCommand0 = MetaManagerCommand0("undeploy", reason, force.toString)
}

case class UndeployActions(reason: String, force: Boolean) extends MetaManagerCommand {
  override def marshall(): MetaManagerCommand0 = MetaManagerCommand0("undeployActions", reason, force.toString)
}

case class DeleteResources(reason: String) extends MetaManagerCommand {
  override def marshall(): MetaManagerCommand0 = MetaManagerCommand0("deleteResources", reason)
}


//case class DeleteQueues(reason: String)