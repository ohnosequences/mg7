import AssemblyKeys._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._
import AssemblyKeys._

Statika.distributionProject

name := "wordcount"

description := "compota wordcount example"

organization := "ohnosequences"

//resolvers ++= Seq(
//  "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com",
//  "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
//)


libraryDependencies ++= Seq(
  "ohnosequences" % "compota_2.10" % "0.9.1-SNAPSHOT"
)

resolvers +=  Resolver.url("era7" + " public ivy releases",  url("http://releases.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

resolvers +=  Resolver.url("era7" + " public ivy snapshots",  url("http://snapshots.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

resolvers += Resolver.sonatypeRepo("snapshots")

bucketSuffix := "frutero.org"

publishMavenStyle := false

isPrivate := true

//todo fix this name
metadataObject := name.value

dependencyOverrides += "ohnosequences" % "aws-scala-tools_2.10" % "0.7.1-SNAPSHOT"

dependencyOverrides += "ohnosequences" % "aws-statika_2.10" % "1.0.1"

dependencyOverrides += "ohnosequences" % "amazon-linux-ami_2.10" % "0.14.1"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.6"

//dependencyOverrides += "org.scala-lang" % "scala-library" % "2.10.4"

//dependencyOverrides += "org.scala-lang" % "scala-compiler" % "2.10.4"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.1.2"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.2"

dependencyOverrides += "jline" % "jline" % "2.6"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.5"


mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case "log4j.properties" => MergeStrategy.first
  case "about.html" => MergeStrategy.first
  case "mime.types" => MergeStrategy.first
  case "avsl.conf" => MergeStrategy.first
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  // case PathList(_*) => MergeStrategy.first
  case PathList("META-INF", _*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "beanutils", _*) => MergeStrategy.first
  case PathList("org", "fusesource", "hawtjni", "runtime", "Library.class") => MergeStrategy.first
  case PathList("org", "fusesource", "jansi", _*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "collections", _*) => MergeStrategy.first
  case x => old(x)
}
}
