
```scala
package ohnosequences.mg7.bundles

import ohnosequences.statika._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


sealed class GIsBundle(name: String) extends Bundle() {
  val bucket = "resources.ohnosequences.com"
  val key = s"16s/${name}"

  val destination: File = File(name)
  val location: File = destination

  def instructions: AnyInstructions = {

    LazyTry {
      println(s"""Dowloading
        |from: s3://${bucket}/${key}
        |to: ${destination.path}
        |""".stripMargin)

      // val transferManager = new TransferManager(new ProfileCredentialsProvider("default"))
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())
      val transfer = transferManager.download(bucket, key, destination.toJava)
      transfer.waitForCompletion
    } -&-
    say(s"GIs database ${name} was dowloaded and unpacked to ${location.path}")
  }
}
```

This is what will be used in the assignment loquat

```scala
case object filteredGIs extends GIsBundle("gi_taxid_filtered.csv")


case object filtering {

  import ohnosequences.statika.aws._
  import ohnosequences.awstools.ec2._
  import ohnosequences.awstools.regions.Region._

  case object originalGIs extends GIsBundle("gi_taxid_nucl.dmp")

  // this is applied only once:
  case object filterGIs extends Bundle(originalGIs) {

    val bucket = "resources.ohnosequences.com"

    val referenceFileName = "reference.gis"
    lazy val referenceFile = File(referenceFileName)

    val filteredFileName = "filtered.gis"
    lazy val filteredFile = File(filteredFileName)

    def key(name: String) = s"16s/${name}"


    def instructions: AnyInstructions = {

      lazy val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

      // downloading reference GIs file
      LazyTry {
        println(s"""Dowloading
          |from: s3://${bucket}/${key(referenceFileName)}
          |to: ${referenceFile.path}
          |""".stripMargin)

        transferManager.download(bucket, key(referenceFileName), referenceFile.toJava).waitForCompletion
      } -&-
      LazyTry {
        val refGIs: Set[String] = io.Source.fromFile( referenceFile.toJava ).getLines.toSet

        import com.github.tototoshi.csv._
        val csvReader = CSVReader.open(originalGIs.location.toJava)(new TSVFormat {})
        val csvWriter = CSVWriter.open(filteredFile.toJava, append = true)(new TSVFormat {})

        // iterating over huge GIs file and filtering it
        csvReader foreach { row =>
          if ( refGIs.contains(row(0)) ) {
            csvWriter.writeRow(row)
            // println("added: " + row.mkString("\t"))
          }
        }

        csvReader.close
        csvWriter.close
      } -&-
      LazyTry {
        println(s"""Uploading
          |from: ${filteredFile.path}
          |to: s3://${bucket}/${key(filteredFileName)}
          |""".stripMargin)

        transferManager.upload(bucket, key(filteredFileName), filteredFile.toJava).waitForCompletion
      }
    }
  }

  case object filterGIsCompat extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore)),
    filterGIs,
    generated.metadata.mg7
  )

}

```




[main/scala/metagenomica/bio4j/taxonomyTree.scala]: ../bio4j/taxonomyTree.scala.md
[main/scala/metagenomica/bio4j/titanTaxonomyTree.scala]: ../bio4j/titanTaxonomyTree.scala.md
[main/scala/metagenomica/bundles/bio4jTaxonomy.scala]: bio4jTaxonomy.scala.md
[main/scala/metagenomica/bundles/blast.scala]: blast.scala.md
[main/scala/metagenomica/bundles/blast16s.scala]: blast16s.scala.md
[main/scala/metagenomica/bundles/flash.scala]: flash.scala.md
[main/scala/metagenomica/bundles/gis.scala]: gis.scala.md
[main/scala/metagenomica/data.scala]: ../data.scala.md
[main/scala/metagenomica/dataflows/standard.scala]: ../dataflows/standard.scala.md
[main/scala/metagenomica/loquats/1.flash.scala]: ../loquats/1.flash.scala.md
[main/scala/metagenomica/loquats/2.split.scala]: ../loquats/2.split.scala.md
[main/scala/metagenomica/loquats/3.blast.scala]: ../loquats/3.blast.scala.md
[main/scala/metagenomica/loquats/4.merge.scala]: ../loquats/4.merge.scala.md
[main/scala/metagenomica/loquats/5.assignment.scala]: ../loquats/5.assignment.scala.md
[main/scala/metagenomica/loquats/6.counting.scala]: ../loquats/6.counting.scala.md
[main/scala/metagenomica/package.scala]: ../package.scala.md
[main/scala/metagenomica/parameters.scala]: ../parameters.scala.md
[test/scala/bundles.scala]: ../../../../test/scala/bundles.scala.md
[test/scala/lca.scala]: ../../../../test/scala/lca.scala.md
[test/scala/metagenomica/pipeline.scala]: ../../../../test/scala/metagenomica/pipeline.scala.md