package ohnosequences.nisperon.logging

import ohnosequences.awstools.s3.ObjectAddress
import ohnosequences.nisperon.{NisperonConfiguration, AWS}
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

//todo add verbose level
//todo add upload manager here
class S3Logger(name: String, aws: AWS, prefix: ObjectAddress, workingDir: String) extends Logger {
  val buffer = new StringBuilder
  def uploadFile(file: File, zeroDir: File = new File(workingDir)) {
    val path = file.getAbsolutePath.replace(zeroDir.getAbsolutePath, "")
    aws.s3.putObject(prefix / path, file)

  }

  def pref(): String = {
    format.format(new Date()) + " " + name + ": "
  }

  val format = new SimpleDateFormat("HH:mm:ss.SSS")

  def warn(s: String) {
    val ss = pref() + s + System.lineSeparator()
    println(ss)
    buffer.append(ss)
  }

  def error(s: String) {
    val ss = pref() + " ERROR: " + s + System.lineSeparator()
    println(ss)
    buffer.append(ss)
  }

  def info(s: String) {
    buffer.append(pref() + s + System.lineSeparator())
  }

  def close() {
    val r = buffer.toString()
    if(!r.isEmpty) {
      aws.s3.putWholeObject(S3Logger.log(prefix), buffer.toString())
    }
  }

}

object S3Logger {
  def prefix(nisperonConfiguration: NisperonConfiguration, id: String): ObjectAddress = {
    ObjectAddress(nisperonConfiguration.bucket, id)
  }

  def log(prefix: ObjectAddress) = prefix / "log.txt"
}
