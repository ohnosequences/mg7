name          := "mg7"
organization  := "ohnosequences"
description   := "Configurable, scalable 16S metagenomics data analysis"
bucketSuffix  := "era7.com"

crossScalaVersions := Seq("2.11.12", "2.12.6")
scalaVersion := crossScalaVersions.value.max

libraryDependencies ++= Seq(
  // APIs:
  "ohnosequences" %% "ncbitaxonomy" % "0.3.1",
  "ohnosequences" %% "fastarious"   % "0.12.0",
  "ohnosequences" %% "blast-api"    % "0.11.1",
  "ohnosequences" %% "flash-api"    % "0.5.2",
  // generic tools:
  "ohnosequences" %% "loquat" % "2.0.0-RC4-37-g8f6a972",
  "ohnosequences" %% "datasets-illumina" % "0.2.1",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  // bundles:
  "ohnosequences-bundles" %% "flash" % "0.4.0",
  "ohnosequences-bundles" %% "blast" % "0.5.0",
  // testing:
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  // TODO: update db-rna16s
  "ohnosequences" %% "db-rna16s" % "1.1.0" % Test
)

dependencyOverrides ++= Seq(
  "ohnosequences" %% "aws-scala-tools" % "0.21.0", // db-rna16s depends on 0.20.0
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "com.google.guava" % "guava" % "14.0.1",
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
