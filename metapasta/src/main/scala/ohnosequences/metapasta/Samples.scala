package ohnosequences.metapasta

import ohnosequences.awstools.s3.{S3, ObjectAddress}

case class PairedSample(name: String, fastq1: ObjectAddress, fastq2: ObjectAddress)


class S3Splitter(s3: S3, address: ObjectAddress, chunksSize: Long) {

  def objectSize(): Long = {
    s3.s3.getObjectMetadata(address.bucket, address.key).getContentLength
  }

  def chunks(): List[(Long, Long)] = {

    val size = objectSize()

    val starts = 0L until size by chunksSize
    val ends = starts.map { start =>
      math.min(start + chunksSize - 1, size - 1)
    }

    starts.zip(ends).toList
  }

}

case class MergedSampleChunk(fastq: ObjectAddress, sample: String, range: (Long, Long)) {
  def chunkId = range._1 + "-" + sample
}
