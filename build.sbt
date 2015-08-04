Nice.scalaProject

name          := "metagenomica"
organization  := "ohnosequences"
description   := "metagenomica project"

bucketSuffix  := "era7.com"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % Test

libraryDependencies ++= Seq(
  // "ohnosequences-bundles" %% "flash" % "0.1.0-SNAPSHOT",
  "ohnosequences-bundles" %% "blast" % "0.1.0"
)
