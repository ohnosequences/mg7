name          := "mg7"
organization  := "ohnosequences"
description   := "Configurable, scalable 16S metagenomics data analysis"
bucketSuffix  := "era7.com"

crossScalaVersions := Seq("2.11.12", "2.12.4")
scalaVersion := crossScalaVersions.value.max

libraryDependencies ++= Seq(
  // APIs:
  "ohnosequences" %% "ncbitaxonomy" % "0.3.0",
  "ohnosequences" %% "fastarious"   % "0.12.0",
  "ohnosequences" %% "blast-api"    % "0.11.1",
  "ohnosequences" %% "flash-api"    % "0.5.1",
  // generic tools:
  "ohnosequences" %% "loquat" % "2.0.0-RC3-3-g2bbedeb",
  "ohnosequences" %% "datasets-illumina" % "0.2.0",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  // bundles:
  "ohnosequences-bundles" %% "flash" % "0.4.0",
  "ohnosequences-bundles" %% "blast" % "0.5.0",
  // testing:
  "org.scalatest" %% "scalatest" % "3.0.4"     % Test
  // TODO: update db-rna16s
  // "ohnosequences" %% "db-rna16s" % "1.0.0-RC1" % Test
)

dependencyOverrides ++= Seq(
  // TODO: remove these overrides and make a new round of deps-updates:
  "ohnosequences" %% "datasets" % "0.5.2",         // update datasets-illumina
  "ohnosequences" %% "aws-scala-tools" % "0.20.0", // update bio4j-dist
  "ohnosequences" %% "cosas" % "0.10.1",           // update blast-api
  "org.slf4j" % "slf4j-api" % "1.7.25"
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
