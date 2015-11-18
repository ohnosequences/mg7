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
  "ohnosequences" %% "flash"       % "0.2.0-M1",
  "ohnosequences" %% "blast"       % "0.2.0-M1",
  "ohnosequences" %% "fastarious"  % "0.1.0",
  // generic tools:
  "ohnosequences" %% "cosas"       % "0.7.1",
  "ohnosequences" %% "loquat"      % "2.0.0-futures-SNAPSHOT",
  "ohnosequences" %% "datasets"    % "0.2.0-M2",
  "ohnosequences" %% "statika"     % "2.0.0-M4",
  "ohnosequences" %% "aws-statika" % "2.0.0-M5",
  // bundles:
  "ohnosequences-bundles" %% "flash"      % "0.1.0",
  "ohnosequences-bundles" %% "blast"      % "0.2.0",
  "ohnosequences-bundles" %% "bio4j-dist" % "0.1.0-M1",
  // utils:
  "era7" %% "project-utils" % "0.1.0-loquat-testing-SNAPSHOT",
  // testing:
  "org.scalatest" %% "scalatest" % "2.2.5" % Test
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-core"        % "2.3.2",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.3.2",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.3.2",
  "commons-logging"            % "commons-logging"     % "1.1.3",
  "commons-codec"              % "commons-codec"       % "1.7",
  "org.apache.httpcomponents"  % "httpclient"          % "4.5",
  "org.slf4j"                  % "slf4j-api"           % "1.7.7"
)



fatArtifactSettings

// copied from bio4j-titan:
mergeStrategy in assembly ~= { old => {
    case "log4j.properties"                       => MergeStrategy.filterDistinctLines
    case PathList("org", "apache", "commons", _*) => MergeStrategy.first
    case x                                        => old(x)
  }
}

enablePlugins(BuildInfoPlugin)
buildInfoPackage := "generated.metadata"
buildInfoObject  := name.value.split("""\W""").map(_.capitalize).mkString
buildInfoOptions := Seq(BuildInfoOption.Traits("ohnosequences.statika.bundles.AnyArtifactMetadata"))
buildInfoKeys    := Seq[BuildInfoKey](
  organization,
  version,
  "artifact" -> name.value.toLowerCase,
  "artifactUrl" -> fatArtifactUrl.value
)
