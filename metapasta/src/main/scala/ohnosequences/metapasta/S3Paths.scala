package ohnosequences.metapasta

import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.metapasta.reporting.SampleId


object S3Paths {

  def mergedFastq(resultsObject: ObjectAddress, sample: String): ObjectAddress = resultsObject / sample / "reads" / (sample + ".merged.fastq")

  def notMergedFastq(resultsObject: ObjectAddress, sample: String): (ObjectAddress, ObjectAddress) = {
    (resultsObject / sample / "reads" / (sample + ".notMerged1.fastq"),  resultsObject / sample / "reads" / (sample + ".notMerged2.fastq"))
  }

  def mergedNoHitFasta(resultsObject: ObjectAddress, sample: String) : ObjectAddress =    resultsObject / sample / "reads" / (sample + ".noHit.fasta")
  def mergedNoTaxIdFasta(resultsObject: ObjectAddress, sample: String, assignmentType: AssignmentType) : ObjectAddress =    resultsObject / sample / "reads" / (sample +"." + assignmentType + ".noTaxId.fasta")

  def mergedAssignedFasta(resultsObject: ObjectAddress, sample: String, assignmentType: AssignmentType) : ObjectAddress = resultsObject / sample / "reads" / (sample + "." + assignmentType + ".assigned.fasta")
  def mergedNotAssignedFasta(resultsObject: ObjectAddress, sample: String, assignmentType: AssignmentType):ObjectAddress =resultsObject / sample / "reads" / (sample + "." + assignmentType + ".notAssigned.fasta")


  def noHitFastas(readsObject: ObjectAddress, sample: String) : ObjectAddress = readsObject / sample / "noHit"

  def noTaxIdFastas(readsObject: ObjectAddress, sample: String, assignmentType: AssignmentType) : ObjectAddress = readsObject / (sample + "###" + assignmentType) / "noTaxId"
  def notAssignedFastas(readsObject: ObjectAddress, sample: String, assignmentType: AssignmentType) : ObjectAddress = readsObject / (sample + "###" + assignmentType) / "notAssigned"
  def assignedFastas(readsObject: ObjectAddress, sample: String, assignmentType: AssignmentType): ObjectAddress = readsObject / (sample + "###" + assignmentType) / "assigned"



  def noHitFasta(readsObject: ObjectAddress, chunk: ChunkId): ObjectAddress = {
    noHitFastas(readsObject, chunk.sample.id) / (chunk.start + "_" + chunk.end + ".fasta")
  }

  def noTaxIdFasta(readsObject: ObjectAddress, chunk: ChunkId, assignmentType: AssignmentType): ObjectAddress = {
    noTaxIdFastas(readsObject, chunk.sample.id, assignmentType) / (chunk.sample.id + chunk.start + "_" + chunk.end + ".fasta")
  }

  def notAssignedFasta(readsObject: ObjectAddress, chunk: ChunkId, assignmentType: AssignmentType): ObjectAddress = {
    notAssignedFastas(readsObject, chunk.sample.id, assignmentType) / (chunk.sample.id + chunk.start + "_" + chunk.end + ".fasta")
  }
  def assignedFasta(readsObject: ObjectAddress, chunk: ChunkId, assignmentType: AssignmentType): ObjectAddress = {
    assignedFastas(readsObject, chunk.sample.id, assignmentType) / (chunk.sample.id + chunk.start + "_" + chunk.end + ".fasta")
  }

  def treeDot(resultsObject: ObjectAddress, sample: String, assignmentType: AssignmentType) = {
    resultsObject / sample / (sample + "." + assignmentType + ".tree.dot")
  }

  def treePdf(resultsObject: ObjectAddress, sample: String, assignmentType: AssignmentType) = {
    resultsObject / sample / (sample + "." + assignmentType + ".tree.pdf")
  }

  def blastOut(readsObject: ObjectAddress, chunk: ChunkId): ObjectAddress = {
    readsObject / chunk.sample.id / "blast" / (chunk.sample.id + chunk.start + "_" + chunk.end + ".blast")
  }

}
