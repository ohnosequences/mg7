Nice.scalaProject

name          := "mg7"
organization  := "ohnosequences"
description   := "metagenomica project"

bucketSuffix  := "era7.com"

scalaVersion := "2.11.8"

resolvers := Seq(
  "Era7 public maven releases"  at s3("releases.era7.com").toHttps(s3region.value.toString),
  "Era7 public maven snapshots" at s3("snapshots.era7.com").toHttps(s3region.value.toString)
) ++ resolvers.value

libraryDependencies ++= Seq(
  // APIs:
  "ohnosequences" %% "flash"      % "0.3.0",
  "ohnosequences" %% "fastarious" % "0.6.0",
  "ohnosequences" %% "blast-api"  % "0.7.0",
  // generic tools:
  "ohnosequences" %% "cosas"        % "0.8.0",
  "ohnosequences" %% "datasets"     % "0.3.0",
  "ohnosequences" %% "loquat"       % "2.0.0-M8-SNAPSHOT",
  "ohnosequences" %% "statika"      % "2.0.0-M5",
  "era7bio"       %% "rnacentraldb" % "0.2.1",
  // bundles:
  "ohnosequences-bundles" %% "flash"      % "0.2.0",
  "ohnosequences-bundles" %% "blast"      % "0.3.0",
  "ohnosequences-bundles" %% "bio4j-dist" % "0.2.0",
  // testing:
  "era7bio"       %% "db-rna16s" % "0.2.0" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test
)

dependencyOverrides ++= Set(
  "commons-logging"            % "commons-logging"     % "1.1.3",
  "commons-codec"              % "commons-codec"       % "1.7",
  "org.apache.httpcomponents"  % "httpclient"          % "4.5.1",
  "org.slf4j"                  % "slf4j-api"           % "1.7.7"
)



// //// Uncomment for testing: ////
// fatArtifactSettings
//
// // copied from bio4j-titan:
// mergeStrategy in assembly ~= { old => {
//     case "log4j.properties"                       => MergeStrategy.filterDistinctLines
//     case PathList("org", "apache", "commons", _*) => MergeStrategy.first
//     case x                                        => old(x)
//   }
// }
//
// enablePlugins(BuildInfoPlugin)
// buildInfoPackage := "generated.metadata"
// buildInfoObject  := name.value
// buildInfoOptions := Seq(BuildInfoOption.Traits("ohnosequences.statika.AnyArtifactMetadata"))
// buildInfoKeys    := Seq[BuildInfoKey](
//   organization,
//   version,
//   "artifact" -> name.value.toLowerCase,
//   "artifactUrl" -> fatArtifactUrl.value
// )
// 
// // For including test code in the fat artifact:
// unmanagedSourceDirectories in Compile += (scalaSource in Test).value / "metagenomica"
