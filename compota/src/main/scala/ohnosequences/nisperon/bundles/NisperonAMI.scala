package ohnosequences.nisperon.bundles

import ohnosequences.statika._
import ohnosequences.statika.aws._
import ohnosequences.awstools.s3.ObjectAddress

//case class NisperonMetadata(s3Bucket: String)

//todo add autotermination
object NisperonAMI extends AMI[NisperonMetadata]("ami-5256b825", "2013.09") {

 // type Metadata = NisperonMetadata

  def userScript(
                  metadata: NisperonMetadata
                  , distName: String
                  , bundleName: String
                  , creds: AWSCredentials = RoleCredentials
                  ): String = {

//
//    echo " -- Installing git -- "
//    echo
//    yum install git -y
//
//    echo
//    echo " -- Installing s3cmd -- "
//    echo
//    git clone https://github.com/s3tools/s3cmd.git
//    cd s3cmd/
//    python setup.py install
//      echo "[default]" > /root/.s3cfg
//
//    cd /root
//
//    s3cmd --config /root/.s3cfg get s3://$bucket$/$key$
// java -jar /root/$jarFile$ $component$ $name$


//    aws s3 cp s3://$bucket$/$key$ /root/$jarFile$ --region eu-west-1
//    aws s3 cp s3://releases.era7.com/ohnosequences/bio4j-ncbi-taxonomy_2.10/0.1.0/jars/bio4j-ncbi-taxonomy_2.10-fat.jar /root/bio4j.jar --region eu-west-1
//    java -cp /root/$jarFile$:/root/bio4j.jar ohnosequences.metapasta.Metapasta $component$ $name$

    val raw = """
                |#!/bin/sh
                |cd /root
                |exec &> log.txt
                |yum install java-1.7.0-openjdk.x86_64 -y
                |chmod a+r log.txt
                |alternatives --install /usr/bin/java java /usr/lib/jvm/jre-1.7.0-openjdk.x86_64/bin/java 20000
                |alternatives --auto java
                |
                |cd $workingDir$
                |aws s3 cp s3://$bucket$/$key$ /root/$jarFile$ --region eu-west-1
                |java -jar /root/$jarFile$ $component$ $name$
                |
              """.stripMargin
      .replace("$bucket$", metadata.jarAddress.bucket)
      .replace("$key$", metadata.jarAddress.key)
      .replace("$jarFile$", getFileName(metadata.jarAddress.key))
      .replace("$component$", metadata.component)
      .replace("$name$", metadata.nisperoId)
      .replace("$workingDir$", metadata.workingDir)

    fixLineEndings(raw)
  }

  def fixLineEndings(s: String): String = s.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n")

  def getFileName(s: String) = s.substring(s.lastIndexOf("/") + 1)

}
