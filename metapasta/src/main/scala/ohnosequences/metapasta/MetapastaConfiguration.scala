package ohnosequences.metapasta

import ohnosequences.nisperon.bundles.NisperonMetadataBuilder
import ohnosequences.nisperon.{NisperonConfiguration, SingleGroup, GroupConfiguration, Group}
import ohnosequences.awstools.ec2.{InstanceSpecs, InstanceType}
import ohnosequences.awstools.autoscaling.{SpotAuto, OnDemand}
import ohnosequences.metapasta.databases._
import ohnosequences.metapasta.reporting.{SampleTag}


//todo extract mapping configuration

//sealed abstract class MappingInstructions {}
//-r10 -q95 -a0 -b95
//case class Last(template: String = """./lastal nt.last/$name$ $input$ -s2 -T0 -e70 -Q$format$ -f0 -o $output$""", fasta: Boolean = false) extends MappingInstructions
//case class Blast(template: String, xmlOutput: Boolean) extends MappingInstructions



case class AssignmentConfiguration(bitscoreThreshold: Int, p: Double = 0.8)


trait  MetapastaConfiguration {
   val metadataBuilder: NisperonMetadataBuilder
   val mappingWorkers: GroupConfiguration
   val managerGroupConfiguration: GroupConfiguration
   val metamanagerGroupConfiguration: GroupConfiguration
   val uploadWorkers: Option[Int]
   val email: String
   val password: String
   val samples: List[PairedSample]
   val tagging: Map[PairedSample, List[SampleTag]]
   val chunksSize: Int
   val chunksThreshold: Option[Int]
   val logging: Boolean
   val removeAllQueues: Boolean
   val timeout: Int
   val mergeQueueThroughput: MergeQueueThroughput
   val generateDot: Boolean
   val assignmentConfiguration: AssignmentConfiguration
   val defaultInstanceSpecs: InstanceSpecs
   val flashTemplate: String
}


abstract class MergeQueueThroughput

case class Fixed(n: Int) extends MergeQueueThroughput
case class SampleBased(ration: Double, max: Int = 100) extends MergeQueueThroughput

case class BlastConfiguration(
                               metadataBuilder: NisperonMetadataBuilder,
                               mappingWorkers: GroupConfiguration = Group(size = 1, max = 20, instanceType = InstanceType.t1_micro, purchaseModel = OnDemand),
                               uploadWorkers: Option[Int],
                               email: String,
                               samples: List[PairedSample],
                               tagging: Map[PairedSample, List[SampleTag]] = Map[PairedSample, List[SampleTag]](),
                               chunksSize: Int = 20000,
                               chunksThreshold: Option[Int] = None,
                               blastTemplate: String = """blastn -task megablast -db $db$ -query $input$ -out $output$ -max_target_seqs 1 -num_threads 1 -outfmt $out_format$ -show_gis""",
                               xmlOutput: Boolean = false,
                               password: String,
                               databaseFactory: DatabaseFactory[BlastDatabase16S] = Blast16SFactory,
                               logging: Boolean = true,
                               removeAllQueues: Boolean = true,
                               timeout: Int = 360000,
                               mergeQueueThroughput: MergeQueueThroughput = SampleBased(1),
                               generateDot: Boolean = true,
                               assignmentConfiguration: AssignmentConfiguration = AssignmentConfiguration(400, 0.8),
                               managerGroupConfiguration: GroupConfiguration = SingleGroup(InstanceType.t1_micro, SpotAuto),
                               metamanagerGroupConfiguration: GroupConfiguration = SingleGroup(InstanceType.m1_medium, SpotAuto),
                               defaultInstanceSpecs: InstanceSpecs = NisperonConfiguration.defaultInstanceSpecs,
                               flashTemplate: String = "flash 1.fastq 2.fastq"
                               ) extends MetapastaConfiguration {
}


case class LastConfiguration(
                               metadataBuilder: NisperonMetadataBuilder,
                               mappingWorkers: GroupConfiguration = Group(size = 1, max = 20, instanceType = InstanceType.m1_medium, purchaseModel = OnDemand),
                               uploadWorkers: Option[Int],
                               email: String,
                               samples: List[PairedSample],
                               tagging: Map[PairedSample, List[SampleTag]] = Map[PairedSample, List[SampleTag]](),
                               chunksSize: Int = 2000000,
                               lastTemplate: String = """./lastal $db$ $input$ -s2 -m100 -T0 -e70 -Q$format$ -f0 -o $output$""",
                               useFasta: Boolean = true,
                               chunksThreshold: Option[Int] = None,
                               databaseFactory: DatabaseFactory[LastDatabase16S] = Last16SFactory,
                               logging: Boolean = true,
                               password: String,
                               removeAllQueues: Boolean = true,
                               timeout: Int = 360000,
                               mergeQueueThroughput: MergeQueueThroughput = SampleBased(1),
                               generateDot: Boolean = true,
                               assignmentConfiguration: AssignmentConfiguration,
                               managerGroupConfiguration: GroupConfiguration = SingleGroup(InstanceType.t1_micro, SpotAuto),
                               metamanagerGroupConfiguration: GroupConfiguration = SingleGroup(InstanceType.m1_medium, SpotAuto),
                               defaultInstanceSpecs: InstanceSpecs = NisperonConfiguration.defaultInstanceSpecs,
                               flashTemplate: String = "flash 1.fastq 2.fastq"
                              ) extends MetapastaConfiguration {
}


//mappingWorkers = Group(size = 1, max = 20, instanceType = InstanceType.T1Micro, purchaseModel = SpotAuto)