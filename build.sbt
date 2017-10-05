name          := "mg7"
organization  := "ohnosequences"
description   := "Configurable, scalable 16S metagenomics data analysis"
bucketSuffix  := "era7.com"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  // APIs:
  "ohnosequences" %% "ncbitaxonomy" % "0.2.0",
  "ohnosequences" %% "fastarious"   % "0.8.0",
  "ohnosequences" %% "blast-api"    % "0.8.0",
  "ohnosequences" %% "flash-api"    % "0.4.0",
  // generic tools:
  "ohnosequences" %% "loquat"       % "2.0.0-RC1",
  "ohnosequences" %% "datasets-illumina" % "0.1.0",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  // bundles:
  "ohnosequences-bundles" %% "flash" % "0.3.0",
  "ohnosequences-bundles" %% "blast" % "0.4.0",
  // testing:
  "org.scalatest" %% "scalatest" % "3.0.4"     % Test,
  "ohnosequences" %% "db-rna16s" % "1.0.0-RC1" % Test
)

dependencyOverrides ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.21"
)

assemblyMergeStrategy in assembly ~= { old => {
    case "log4j.properties"                       => MergeStrategy.filterDistinctLines
    case PathList("org", "apache", "commons", _*) => MergeStrategy.first
    case x                                        => old(x)
  }
}

// These settings are only for manual testing:
generateStatikaMetadataIn(Test)
// This includes tests sources in the assembled fat-jar:
fullClasspath in assembly := (fullClasspath in Test).value
