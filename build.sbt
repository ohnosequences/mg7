Nice.scalaProject

name          := "metagenomica"
organization  := "ohnosequences"
description   := "metagenomica project"

bucketSuffix  := "era7.com"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % Test

libraryDependencies ++= Seq(
  "ohnosequences" %% "flash"      % "0.1.0-SNAPSHOT",
  "ohnosequences" %% "blast"      % "0.1.0-SNAPSHOT",
  "ohnosequences" %% "datasets"   % "0.1.0-SNAPSHOT",
  "ohnosequences" %% "cosas"      % "0.7.0-SNAPSHOT",
  "ohnosequences" %% "loquat"     % "2.0.0-SNAPSHOT",
  "ohnosequences" %% "fastarious" % "0.1.0-SNAPSHOT"
)
