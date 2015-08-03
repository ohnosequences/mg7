package ohnosequences.nisperon.bundles

import ohnosequences.statika.aws._
import ohnosequences.awstools.s3.ObjectAddress

class NisperonMetadataBuilder(fatMetadata: SbtMetadata with FatJarMetadata) {


  def build(component: String, nisperoId: String, workingDir: String = "/root") = new NisperonMetadata(fatMetadata, component, nisperoId, workingDir)

  val jarAddress: ObjectAddress = getAddress(fatMetadata.artifactUrl)


  def getAddress(url: String): ObjectAddress = {
    val s3url = """s3://(.+)/(.+)""".r
    url match {
      case s3url(bucket, key) => ObjectAddress(bucket, key)
      case _ => throw new Error("wrong fat jar url, check your publish settings")
    }
  }

  def generateId(metadata: SbtMetadata): String = {
    val name = metadata.artifact
    val version = metadata.version.replace(".", "")
    (name + version).toLowerCase.replaceAll("""[^\w]+""", "_")
  }

  val id = generateId(fatMetadata)
}

class NisperonMetadata(fatMetadata: SbtMetadata with FatJarMetadata, val component: String, val nisperoId: String, val workingDir: String = "/root") extends AnyMetadata {
  val organization: String = fatMetadata.organization
  val artifact: String = fatMetadata.artifact
  val version: String = fatMetadata.version
  //val resolvers: Seq[String] = Seq()
  //val privateResolvers: Seq[String] = Seq()

  val jarAddress: ObjectAddress = getAddress(fatMetadata.artifactUrl)


  def getAddress(url: String): ObjectAddress = {
    val s3url = """s3://(.+)/(.+)""".r
    url match {
      case s3url(bucket, key) => ObjectAddress(bucket, key)
      case _ => throw new Error("wrong fat jar url, check your publish settings")
    }
  }

}


