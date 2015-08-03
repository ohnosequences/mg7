Nice.scalaProject

name := "metapasta"

description := "metapasta project"

organization := "ohnosequences"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.0" % "test"

testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck, "-maxSize", "40", "-minSuccessfulTests", "10", "-workers", "1", "-verbosity", "1")

libraryDependencies ++= Seq(
  "ohnosequences" %% "compota" % "0.9.12-RC2",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "ohnosequences" %% "bio4j-ncbi-taxonomy" % "0.1.0"  classifier("")
)

resolvers += Resolver.url("Statika public ivy releases", url("http://releases.statika.ohnosequences.com.s3.amazonaws.com/"))(ivy)

resolvers +=  Resolver.url("era7" + " public ivy releases",  url("http://releases.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

resolvers +=  Resolver.url("era7" + " public ivy snapshots",  url("http://snapshots.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)


dependencyOverrides += "ohnosequences" %% "aws-scala-tools" % "0.13.2"

dependencyOverrides += "ohnosequences" %% "aws-statika" % "1.0.1"

dependencyOverrides += "ohnosequences" %% "amazon-linux-ami" % "0.14.1"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.6"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.1.2"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.2"

dependencyOverrides += "jline" % "jline" % "2.6"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.5"

