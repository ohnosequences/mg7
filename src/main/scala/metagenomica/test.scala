package ohnosequences.metagenomica

import ohnosequences.metagenomica.configuration._

import ohnosequences.datasets._, dataSets._, fileLocations._, s3Locations._, illumina._, reads._

import ohnosequences.cosas._, typeSets._, types._, records._, properties._
import ohnosequences.cosas.ops.typeSets._

import ohnosequences.loquat._, utils._, configs._, dataMappings._, dataProcessing._

import ohnosequences.statika.bundles._
import ohnosequences.statika.aws._, api._, amazonLinuxAMIs._

import ohnosequences.awstools._, regions.Region._, ec2.InstanceType._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.autoscaling._

import ohnosequences.flash.api._
import ohnosequences.flash.data._

import ohnosequences.blast.api._, outputFields._
import ohnosequences.blast.data._

import era7.project.loquats._

import java.io.File


case object test {

  case object outRec extends BlastOutputRecord(
    qseqid    :&:
    qlen      :&:
    qstart    :&:
    qend      :&:
    sseqid    :&:
    slen      :&:
    sstart    :&:
    send      :&:
    bitscore  :&:
    sgi       :&: □
  )

  // case object blastExprType extends BlastExpressionType(blastn)(outRec)
  // case object blastOutputType extends BlastOutputType(blastExprType, "blastn.blablabla")
  // case object blastOutput extends BlastOutput(blastOutputType, "blast.csv")

  case object testData extends MetagenomicaData(
    readsType = illumina.PairedEnd(bp300, InsertSize(3000)),
    blastOutRec = outRec
  ) {

    def blastExpr(args: ValueOf[blastn.Arguments]): BlastExpression[BlastExprType] = {
      BlastExpression(blastExprType)(
        argumentValues = args,
        optionValues   = blastn.defaults update (
          num_threads(1) :~:
          max_target_seqs(10) :~:
          ohnosequences.blast.api.evalue(0.001)  :~:
          blastn.task(blastn.megablast) :~: ∅
        )
      )
    }

  }

}
