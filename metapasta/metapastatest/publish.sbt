import ohnosequences.sbt._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._
import AssemblyKeys._

publishMavenStyle := false

isPrivate := true

releaseSettings

bucketSuffix := "frutero.org"

releaseProcess <<= thisProjectRef apply { ref =>
  Seq[ReleaseStep](
    inquireVersions,
    setReleaseVersion,
    setNextVersion,
    publishArtifacts
  )
}

nextVersion := { ver => sbtrelease.Version(ver).map(_.bumpMinor.string).getOrElse(versionFormatError) }

addCommandAlias("metapasta-publish", ";reload; release with-defaults")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case "log4j.properties" => MergeStrategy.first
  case "about.html" => MergeStrategy.first
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

excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {_.data.getName == "bio4j-scala-distribution_2.10-fat.jar"}
}

excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {_.data.getName == "bio4j-ncbi-taxonomy_2.10-fat.jar"}
}