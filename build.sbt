name          := "mg7"
organization  := "ohnosequences"
description   := "Configurable, scalable 16S metagenomics data analysis"

bucketSuffix  := "era7.com"

scalaVersion := "2.11.8"

resolvers := Seq(
  "Era7 public maven releases"  at s3("releases.era7.com").toHttps(s3region.value.toString)
) ++ resolvers.value

libraryDependencies ++= Seq(
  // APIs:
  "ohnosequences" %% "ncbitaxonomy" % "0.2.0",
  "ohnosequences" %% "fastarious"   % "0.8.0",
  "ohnosequences" %% "blast-api"    % "0.8.0",
  "ohnosequences" %% "flash-api"    % "0.4.0",
  // generic tools:
  "ohnosequences" %% "cosas"        % "0.8.0",
  "ohnosequences" %% "loquat"       % "2.0.0-RC1",
  "ohnosequences" %% "statika"      % "2.0.0",
  "ohnosequences" %% "datasets"          % "0.4.1",
  "ohnosequences" %% "datasets-illumina" % "0.1.0",
  // bundles:
  "ohnosequences-bundles" %% "flash" % "0.3.0",
  "ohnosequences-bundles" %% "blast" % "0.4.0"
  // testing:
  // "ohnosequences" %% "db-rna16s" % "0.12.0" % Test
)

dependencyOverrides ++= Set(
  "org.apache.httpcomponents" % "httpclient" % "4.5.1",
  "org.slf4j"                 % "slf4j-api"  % "1.7.7"
)

// NOTE should be reestablished
wartremoverErrors in (Test, compile) := Seq()
wartremoverErrors in (Compile, compile) := Seq()

assemblyMergeStrategy in assembly ~= { old => {
    case "log4j.properties"                       => MergeStrategy.filterDistinctLines
    case PathList("org", "apache", "commons", _*) => MergeStrategy.first
    case x                                        => old(x)
  }
}


// generateStatikaMetadataIn(Compile)
//
// // This turns on fat-jar publishing during release process:
// publishFatArtifact in Release := true

// These settings are only for manual testing:
generateStatikaMetadataIn(Test)

// This includes tests sources in the assembled fat-jar:
fullClasspath in assembly := (fullClasspath in Test).value
