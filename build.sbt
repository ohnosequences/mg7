Nice.scalaProject

name          := "metagenomica"
organization  := "ohnosequences"
description   := "metagenomica project"

bucketSuffix  := "era7.com"

scalaVersion := "2.11.7"

resolvers := Seq(
  "Era7 public maven releases"  at s3("releases.era7.com").toHttps(s3region.value.toString),
  "Era7 public maven snapshots" at s3("snapshots.era7.com").toHttps(s3region.value.toString)
) ++ resolvers.value

libraryDependencies ++= Seq(
  // APIs:
  "ohnosequences"         %% "flash"         % "0.2.0-SNAPSHOT",
  "ohnosequences"         %% "blast"         % "0.2.0-SNAPSHOT",
  "ohnosequences"         %% "fastarious"    % "0.1.0-SNAPSHOT",
  "bio4j"                 %  "bio4j"         % "0.12.0-SNAPSHOT",
  "bio4j"                 %  "bio4j-titan"   % "0.4.0-SNAPSHOT",
  // generic tools:
  "ohnosequences"         %% "cosas"         % "0.7.0",
  "ohnosequences"         %% "loquat"        % "2.0.0-SNAPSHOT",
  "ohnosequences"         %% "datasets"      % "0.2.0-SNAPSHOT",
  "ohnosequences"         %% "statika"       % "2.0.0-new-instructions-SNAPSHOT",
  "ohnosequences"         %% "aws-statika"   % "2.0.0-new-instructions-SNAPSHOT",
  // bundles:
  "ohnosequences-bundles" %% "flash"         % "0.1.0-SNAPSHOT",
  "ohnosequences-bundles" %% "blast"         % "0.2.0-SNAPSHOT",
  "ohnosequences-bundles" %% "compressinglibs" % "0.3.0-SNAPSHOT",
  // utils:
  "era7"                  %% "project-utils" % "0.1.0-SNAPSHOT",
  // testing:
  "org.scalatest"         %% "scalatest"     % "2.2.5" % Test
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-core"        % "2.3.2",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.3.2",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.3.2",
  "commons-logging"            % "commons-logging"     % "1.1.3",
  "commons-codec"              % "commons-codec"       % "1.7",
  "org.apache.httpcomponents"  % "httpclient"          % "4.5",
  "org.slf4j"                  % "slf4j-api"           % "1.7.7"
  // "org.scala-lang.modules"     %% "scala-xml"          % "1.0.4",
  ////
  // "commons-beanutils"          % "commons-beanutils"       % "1.8.3",
  // "commons-beanutils"          % "commons-beanutils-core"  % "1.8.3",
  // "net.sf.opencsv"             % "opencsv"                 % "2.3"
)




fatArtifactSettings

// copied from bio4j-titan:
mergeStrategy in assembly ~= { old => {
    case "log4j.properties"                       => MergeStrategy.filterDistinctLines
    case PathList("org", "apache", "commons", _*) => MergeStrategy.first
    case x                                        => old(x)
  }
}

val metadataObject = Def.setting { name.value.split("""\W""").map(_.capitalize).mkString }

// mvn: "[organisation]/[module]_[scalaVersion]/[revision]/[artifact]-[revision]-[classifier].[ext]"
// ivy: "[organisation]/[module]_[scalaVersion]/[revision]/[type]s/[artifact]-[classifier].[ext]"
val fatUrl = Def.setting {
  val isMvn = publishMavenStyle.value
  val scalaV = "_"+scalaBinaryVersion.value
  val module = moduleName.value + scalaV
  val artifact =
    (if (isMvn) "" else "jars/") +
    module +
    (if (isMvn) "-"+version.value else "") +
    "-fat" +
    ".jar"

  Seq(
    publishS3Resolver.value.url,
    organization.value,
    module,
    version.value,
    artifact
  ).mkString("/")
}

val generateMetadata = Def.task {

  val text = s"""
    |package generated.metadata
    |
    |import ohnosequences.statika.bundles._
    |
    |case object ${metadataObject.value} extends AnyArtifactMetadata {
    |  val organization: String = "${organization.value}"
    |  val artifact:     String = "${name.value.toLowerCase}"
    |  val version:      String = "${version.value}"
    |  val artifactUrl:  String = "${fatUrl.value}"
    |}
    |""".stripMargin

  val file = (sourceManaged in Compile).value / "statika" / "metadata.scala"
  IO.write(file, text)
  Seq(file)
}

sourceGenerators in Compile += generateMetadata.taskValue
