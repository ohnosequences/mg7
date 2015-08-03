Nice.scalaProject

name := "compota"

description := "compota (ex nisperon)"

organization := "ohnosequences"

isPrivate := false

libraryDependencies ++= Seq(
  "commons-io"     % "commons-io" % "2.1",
  "com.novocode"   % "junit-interface" % "0.10" % "test",
  "org.clapper"   %% "avsl" % "1.0.1",
  "org.json4s"    %% "json4s-native" % "3.2.5",
  "ohnosequences" %% "aws-scala-tools" % "0.13.2",
  "ohnosequences" %% "statika" % "1.0.0",
  "ohnosequences" %% "aws-statika" % "1.0.1",
  "ohnosequences" %% "amazon-linux-ami" % "0.14.1",
  "net.databinder" %% "unfiltered-filter" % "0.7.1",
  "net.databinder" %% "unfiltered-netty" % "0.7.1",
  "net.databinder" %% "unfiltered-netty-server" % "0.7.1",
  "com.novocode" % "junit-interface" % "0.10" % "test"
)

resolvers ++= Seq(
  "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com",
  "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
)

resolvers += Resolver.url("Statika public ivy releases", url("http://releases.statika.ohnosequences.com.s3.amazonaws.com/"))(ivy)

resolvers +=  Resolver.url("era7" + " public ivy releases",  url("http://releases.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

resolvers +=  Resolver.url("era7" + " public ivy snapshots",  url("http://snapshots.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)


dependencyOverrides += "ohnosequences" % "aws-scala-tools_2.10" % "0.13.2"

dependencyOverrides += "ohnosequences" % "aws-statika_2.10" % "1.0.1"

dependencyOverrides += "ohnosequences" % "amazon-linux-ami_2.10" % "0.14.1"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.6"

dependencyOverrides += "org.scala-lang" % "scala-library" % "2.10.4"

dependencyOverrides += "org.scala-lang" % "scala-compiler" % "2.10.4"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.1.2"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.2"

dependencyOverrides += "jline" % "jline" % "2.6"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.5"

//test 5

