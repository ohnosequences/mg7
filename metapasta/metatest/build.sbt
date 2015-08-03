import AssemblyKeys._

Statika.distributionProject

name := "metatest"

description := ""

organization := "ohnosequences"

libraryDependencies ++= Seq(
  "ohnosequences" % "metapasta_2.10" % "0.8.4-SNAPSHOT"
)

resolvers +=  Resolver.url("era7" + " public ivy releases",  url("http://releases.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

resolvers +=  Resolver.url("era7" + " public ivy snapshots",  url("http://snapshots.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

resolvers += Resolver.sonatypeRepo("snapshots")

metadataObject := name.value

dependencyOverrides += "ohnosequences" % "aws-scala-tools_2.10" % "0.7.1-SNAPSHOT"

dependencyOverrides += "ohnosequences" % "aws-statika_2.10" % "1.0.1"

dependencyOverrides += "ohnosequences" % "amazon-linux-ami_2.10" % "0.14.1"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.6"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.1.2"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.2"

dependencyOverrides += "jline" % "jline" % "2.6"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.5"

