// package ohnosequences.metagenomica.flash
//
// import ohnosequences.loquat._, utils._, dataProcessing._
// import ohnosequences.statika._, bundles._, instructions._
// import ohnosequences.flash.api._
// import ohnosequences.flash.data._
// import ohnosequences.cosas._, typeSets._, types._
// import ohnosequences.datasets._, dataSets._, fileLocations._, illumina._, reads._
// import ohnosequencesBundles.statika.Flash
// // import java.io.File
//
//
// case object data {
//
//   trait AnyFlashData {
//
//   }
//
//
//   class FlashData [
//     RT <: AnyReadsType { type EndType = pairedEndType },
//     R1 <: AnyPairedEnd1Fastq { type DataType = RT },
//     R2 <: AnyPairedEnd2Fastq { type DataType = RT },
//     M <: MergedReads[RT, R1, R2],
//     S <: MergedReadsStats[M]
//   ](val readsType: RT,
//     val reads1: R1,
//     val reads2: R2,
//     val merged: M,
//     val stats: S
//   ) extends AnyFlashData {
//
//     type ReadsType = R1#DataType
//     type Reads1 = R1
//     type Reads2 = R2
//
//     type Merged = M
//     type Stats = S
//   }
// }
