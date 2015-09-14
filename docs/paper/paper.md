---
title: "MG7: Configurable and scalable 16S metagenomics data analysis -- new methods optimized for massive cloud computing"

authors:
- name: Alexey Alekhin$^\dagger$
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "aalekhin@ohnosequences.com"
  position: 1
- name: Evdokim Kovach$^\dagger$
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "ekovach@ohnosequences.com"
  position: 2
- name: Marina Manrique
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "mmanrique@era7.com"
  position: 3
- name: Pablo Pareja
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "ppareja@ohnosequences.com"
  position: 4
- name: Eduardo Pareja
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "epareja@era7.com"
  position: 5
- name: Raquel Tobes
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "rtobes@era7.com"
  position: 6

correspondingAuthor:
  name: Eduardo Pareja-Tobes
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "eparejatobes@ohnosequences.com"
  position: 7

abstract: |
  The exponential growth of metagenomics is adding a significant plus of complexity to the big data problem in genomics. In this new scenario impacted by the wide scale and scope of the projects and by the explosion of sequence data to be analyzed is especially opportune the use of new possibilities that cloud computing approaches, new functional and dependently typed programming languages and new database paradigms as graph databases offer. To tackle the challenges of big data analysis in this work we have used these new means to design and develop a new open source methodology for analyzing metagenomics data, MG7. It exploits the new possibilities that cloud computing offers to get a system robust, programmatically configurable, modular, distributed, flexible, scalable and traceable in which the biological databases of reference sequences can be easily updated and / or frequently substituted by new ones or by databases specifically designed for focused projects. MG7 uses parallelization and distributed analysis based on AWS, with on-demand infrastructure as the basic paradigm and allow the definition of complex workflows using a composable system for scaling/parallelizing stateless computations designed for Amazon Web Services (AWS) that counts with a static reproducible specification of dependencies and behavior of the different components. The modelling of the taxonomy tree is done using the new paradigm of graph databases of Bio4j that facilitates the taxonomic assignment tasks and the calculation of the taxa abundance values considering the hierarchic structure of taxonomy tree. MG7 includes the new 16S database 16S-DB7 built with a flexible and sustainable system of updating and project-driven personalization.

  $^\dagger$ The first and second authors contributed equally to this work

keywords: "Metagenomics, 16S, Bacterial diversity profile, Bio4j, Graph databases, Cloud computing, NGS, Genomic big data"
---

# Introduction
<!-- TODO needs review -->

During the past decade, metagenomics data analysis is growing exponentially. Some of the reasons behind this are the increasing throughput of massively parallel sequencing technologies (with the derived decrease in sequencing costs), and the wide impact of metagenomics studies [@oulas2015metagenomics], especially in human health [@bikel2015combining]. We should also mention what could be called the microbiome explosion: all kind of microbiomes (gut, mouth, skin, urinary tract, airway, milk, bladder) are now routinely sequenced in different conditions of health and disease, or after different treatments. The impact of Metagenomics is also being felt in environmental sciences [@ufarte2015discovery], crop sciences, the agrifood sector [@coughlan2015biotechnological] and biotechnology in general [@cowan2015metagenomics, @kodzius2015marine]. These new possibilities for exploring the diversity of micro--organisms in the most varied environments are opening new research areas, and drastically changing the existing ones.

As a consequence, the challenge is thus moving (as in other fields) from data acquisition to data analysis: the amount of data is expected to be overwhelming in a very short time [@stephens2015big].

Genome researchers have raised the alarm over big data in the past [@hayden2015genome], but even a more serious challenge might be faced with the metagenomics boom. If we compare metagenomics data with other genomics data used in clinical genotyping we find a differential feature: the key role of time. Thus, for example, in some longitudinal studies, serial sampling from the same patient [@faust2015metagenomics] along several weeks (or years) is being used for the follow up of some intestinal pathologies, for studying the evolution of the gut microbiome after antibiotic treatment, or for colon cancer early detection [@zeller2014potential, @garrett2015cancer]. This need of sampling across time adds more complexity to metagenomics data storage and demands adapted algorithms to detect state variations across time as well as idiosyncratic commonalities of the microbiome of each individual [@franzosa2015identifying].
In addition to the intra-individual sampling-time dependence, metagenomic clinical test results vary depending on the specific region of extraction of the clinical specimen. This local variability adds complexity to the analysis since different localizations (different tissues, different anatomical regions, healthy or tumour tissues) are required to have a sufficiently complete landscape of the human microbiome. Moreover, re--analysis of old samples using new tools and better reference databases might be also demanded from time to time.

Other disciplines such as astronomy or particle physics have faced the big data challenge before. A key difference is the existence of standards for data processing [@stephens2015big]; in metagenomics global standards for converting raw sequence data into processed data are not yet well defined, and there are shortcomings derived from the fact that most bioinformatics methodologies used for metagenomics data analysis were designed for scenarios very different from the current one. These are some of the aspects that have suffered crucial changes and advances with a direct impact in metagenomics data analysis:

1. **Sequence data:** the reads are larger, the sequencing depth and the number of samples of each project are considerably bigger. The first metagenomics studies were very local projects, while nowadays the most fruitful studies are done at a global level (international, continental, national). This kind of global studies has yielded the discovery of clinical biomarkers for diseases of the importance of cancer, obesity or inflammatory bowel diseases and has allowed exploring the biodiversity of varied earth environments.
2. **The genomics explosion:** its effect being felt in this case in the reference sequences. The immense amount of sequences available in public repositories demands new strategies for curation, update and storage of metagenomics reference databases: current models will (already) have problems to face the future avalanche of metagenomic sequence data.
3. **Cloud computing:** the appearance of new models for massive computation and storage such as the so-called cloud, or the widespread adoption of programming methodologies like functional programming, or, more speculatively, dependently typed programming. The immense new possibilities that these advances offer must have a direct impact in metagenomics data analysis.
4. **Open science:** the new social manner to do science, particularly so in genomics, brings its own set of requirements. Metagenomics evolves in a social and global scenario following a science democratization trend in which many small research groups from distant countries share a common big metagenomics project; this global cooperation demands systems allowing for reproducible data analysis, data interoperability, and tools and practices for asynchronous collaboration between different groups.









# Results

## Overview

Considering the current new metagenomics scenario and to tackle the challenges posed by metagenomics big data analysis outlined in the Introduction we have designed a new open source methodology for analyzing metagenomics data. It exploits the new possibilities that cloud computing offers to get a system robust, programmatically configurable, modular, distributed, flexible, scalable and traceable in which the biological databases of reference sequences can be easily updated and/or frequently substituted by new ones or by databases specifically designed for focused projects.

These are some of the more innovative MG7 features:

- Static reproducible specification of dependencies and behavior of the different components using *Statika* and *Datasets*
- Parallelization and distributed analysis based on AWS, with on-demand infrastructure as the basic paradigm
- Definition of complex workflows using *Loquat*, a composable system for scaling/parallelizing stateless computations especially designed for Amazon Web Services (AWS)
- A new approach to data analysis specification, management and specification based on working with it in exactly the same way as for a software project, together with the extensive use of compile-time structures and checks
- Modeling of the taxonomy tree using the new paradigm of graph databases (Bio4j). It facilitates the taxonomic assignment tasks and the calculation of the taxa abundance values considering the hierarchic structure of taxonomy tree (cumulative values)
- Exhaustive per-read taxonomic assignment using two complementary assignment algorithms Lowest Common Ancestor and Best BLAST Hit
- Using a new 16S database of reference sequences (16S-DB7) with a flexible and sustainable system of updating and project-driven customization

## Libraries and resources

In this section we describe the resources and libraries developed by the authors on top of which MG7 is built. All MG7 code is written in [Scala](http://www.scala-lang.org/), a hybrid object-functional programming language.
Scala was chosen based on the possibility of using certain advanced programming styles, and Java interoperability, which let us build on the vast number of existing Java libraries; we take advantage of this when using Bio4j as an API for the NCBI taxonomy. It has support for type-level programming, type-dependent types (through type members) and singleton types, which permits a restricted form of dependent types where types can depend essentially on values determined at compile time (through their corresponding singleton types). Conversely, through implicits one can retrieve the value corresponding to a singleton type.

### _Statika_: machine configuration and behavior

[Statika](https://github.com/ohnosequences/statika) is a Scala library developed by the first and last authors which serves as a way of defining and composing machine behaviors statically. The main component are **bundles**. Each bundle declares a sequence of computations (its behavior) which will be executed in an **environment**. A bundle can *depend* on other bundles, and when being executed by an environment, its DAG (Directed Acyclic Graph) of dependencies is linearized and run in sequence. In our use, bundles correspond to what an EC2 instance should do and an environment to an AMI (Amazon Machine Image) which prepares the basic configuration, downloads the Scala code and runs it.

### _Datasets_: a mini-language for data

[Datasets](https://github.com/ohnosequences/datasets) is a Scala library developed by the first and last authors with the goal of being a Scala-embedded mini-language for datasets and their locations. **Data** is represented as type-indexed fields: Keys are modeled as singleton types, and values correspond to what could be called a denotation of the key: a value of type `Location` tagged with the key type. Then a **Dataset** is essentially a collection of data, which are guaranteed statically to be different through type-level predicates, making use of the value ↔ type correspondence which can be established through singleton types and implicits. A dataset location is then just a list of locations formed by locations of each data member of that dataset. All this is based on what could be described as an embedding in Scala of an extensible record system with concatenation on disjoint labels, in the spirit of [@harper1990extensible, @harper1991record]. For that *Datasets* uses [ohnosequences/cosas](https://github.com/ohnosequences/cosas/).

Data keys can further have a reference to a **data type**, which, as the name hints at, can help in providing information about the type of data we are working with. For example, when declaring Illumina reads as a data, a data type containing information about the read length, insert size or end type (single or paired) is used.

A **location** can be, for example, an S3 object or a local file; by leaving the location type used to denote particular data free we can work with different "physical" representations, while keeping track of to which logical data they are a representation of. Thus, a process can generate locally a `.fastq` file representing the merged reads, while another can put it in S3 with the fact that they all correspond to the "same" merged reads is always present, as the data that those "physical" representations denote.

### _Loquat_: Parallel data processing with AWS

[Loquat](https://github.com/ohnosequences/loquat) is a library developed by the first, second and last authors designed for the execution of embarrassingly parallel tasks using S3, SQS and EC2 Amazon services.

A *loquat* executes a process with explicit input and output datasets (declared using the *Datasets* library described above). Workers (EC2 instances) read from an SQS queue the S3 locations for both input and output data; then they download the input to local files, and pass these file locations to the process to be executed. The output is then put in the corresponding S3 locations.

A manager instance is used to monitor workers, provide initial data to be put in the SQS queue and optionally release resources depending on a set of configurable conditions.

Both worker and manager instances are *Statika* bundles. The worker can declare any dependencies needed to perform its task: other tools, libraries, or data.

All configuration such as the number of workers or the instance types is declared statically, the specification of a loquat being ultimately a Scala object. Deploy and resource management methods make easy to use an existing loquat either as a library or from (for example) a Scala REPL.

The input and output (and their locations) being defined statically has several critical advantages. First, composing different loquats is easy and safe; just use the output types and locations of the first one as input for the second one. Second, data and their types help in not mixing different resources when implementing a process, while serving as a safe and convenient mechanism for writing generic processing tasks. For example, merging paired-end Illumina reads generically is easy as the data type includes the relevant information (insert size, read length, etc) to pass to a tool such as FLASH.

### Type-safe eDSLs for BLAST and FLASH

<!-- TODO cite BLAST and FLASH -->
We developed our own Scala-based type-safe eDSLs (**e**mbedded **D**omain **S**pecific **L**anguage) for [FLASH](https://github.com/ohnosequences/flash) and [BLAST](https://github.com/ohnosequences/blast) expressions and their execution.

In the case of BLAST we use a model for expressions where we can guarantee for each BLAST command expression at compile time

- all required arguments are provided
- only valid options are provided
- correct types for each option value
- valid output record specification

Generic type-safe parsers returning a heterogeneous record of BLAST output fields are also available, together with output data defined using *Datasets* which have a reference to the exact BLAST command options which yielded that output. This let us provide generic parsers for BLAST output which are guaranteed to be correct.

In the same spirit as for BLAST, we implemented a type-safe eDSL for FLASH expressions and their execution, supporting features equivalent to those outlined for the BLAST eDSL.

### Bio4j and Graph Databases

[Bio4j @pareja2015bio4j] is a data platform integrating data from different resources such as UniProt or GO in a graph data paradigm. In the assignment phase we use a subgraph containing the NCBI Taxonomy, wrapping in Scala its Java API in a tree algebraic data type.

### 16S Reference Database Construction

Our 16S Reference Database is a curated subset of sequences from NCBI nucleotide database **nt**. The sequences included were selected by similarity with the bacterial and archaeal reference sequences downloaded from the **RDP database** [@cole2013ribosomal]. RDP unaligned sequences were used to capture new 16S sequences from **nt** using BLAST similarity search strategies and then, performing additional curation steps to remove sequences with poor taxonomic assignments to taxonomic nodes close to the root of the taxonomy tree.
All the nucleotide sequences included in **nt** database has a taxonomic assignment provided by the **Genbank** sequence submitter. NCBI provides a table (available at ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/) to do the mapping of any Genbank Identifier (GI) to its Taxonomy Identifier (TaxID). Thus, we are based on a crowdsourced submitter-maintained taxonomic annotation system for reference sequences. It supposes a sustainable system able to face the expected number of reference sequences that will populate the public global nucleotide databases in the near future. Another advantageous point is that we are based on NCBI taxonomy, the *de facto* standard taxonomic classification for biomolecular data  [@cochrane20102010]. NCBI taxonomy is, undoubtedly, the most used taxonomy all over the world and the most similar to the official taxonomies of each specific field. This is a crucial point because all the type-culture and tissue databanks follow this official taxonomical classification and, in addition, all the knowledge accumulated during last decades is referred to this taxonomy. In addition NCBI provides a direct connection between taxonomical formal names and the physical specimens that serve as exemplars for the species [@federhen2014type].

Certainly, if metagenomics results are easily integrated with the theoretical and experimental knowledge of each specific area, the impact of metagenomics will be higher that if metagenomics progresses as a disconnected research branch. Considering that metagenomics data interoperability, which is especially critical in clinical environments, requires a stable taxonomy to be used as reference, we decided to rely on the most widely used taxonomy: the NCBI taxonomy. In addition, the biggest global sequence database GenBank follows this taxonomy to register the origin of all their submitted sequences.
Our 16S database building strategy allows the substitution of the 16S database by any other subset of **nt**, even by the complete **nt** database if it would be needed, for example, for analyzing shotgun metagenomics data. This possibility of changing the reference database provides flexibility to the system enabling it for easy updating and project-driven personalization.

## Workflow Description

The MG7 analysis workflow is summarized in <!-- TODO number -->Figure X. The input files for MG7 are the FASTQ files resulting from a paired-end NGS sequencing experiment.

### Joining reads of each pair using FLASH

In the first step the paired-end reads, designed with an insert size that yields pairs of reads with an overlapping region between them, are assembled using FLASH [@magovc2011flash]. FLASH is designed to merge pairs of reads when the original DNA fragments are shorter than twice the length of reads. Thus, the sequence obtained after joining the 2 reads of each pair is larger and has better quality since the sequence at the ends of the reads is refined merging both ends in the assembly. To have a larger and improved sequence is crucial to do more precise the inference of the bacterial origin based on similarity with reference sequences.

### Parallelized BLASTN of each read against the 16S-DB7

The second step is to search for similar 16S sequences in our 16S-DB7 database. The taxonomic assignment for each read is based on BLASTN of each read against the 16S database. Assignment based on direct similarity of each read one by one compared against a sufficiently wide database is a very exhaustive method for assignment [@segata2013computational]<!-- TODO missing ref for @morgan2012chapter --> . Some methods of assignment compare the sequences only against the available complete bacterial genomes or avoid computational cost clustering or binning the sequences first, and then doing the assignments only for the representative sequence of each cluster. MG7 carries out an exhaustive comparison of all the reads under analysis and it does not applies any binning strategy. Every read is specifically compared with all the sequences of the 16S database. We select the best BLAST hits (10 hits by default) obtained for each read to do the taxonomic assignment.

### Taxonomic Assignment Algorithms
All the reads are assigned under two different algorithms of assignment: i. Lowest Common Ancestor based taxonomic assignment (LCA) and ii. Best BLAST Hit based taxonomic assignment (BBH). Figure X displays schematically the LCA algorithm applied sensu stricto (left panel) and the called ‘in line’ exception (right panel) designed in order to gain specificity in the assignments in the cases in which the topology of the taxonomical nodes corresponding to the BLAST hits support sufficiently the assignment to the most specific taxon.

#### Lowest Common Ancestor based Taxonomic Assignment

For each read, first, we select the BEST BLAST HITs (by default 10 Hits) over a threshold of similarity (by default $evalue \leq e^{-15}$) filtering those hits that are not sufficiently good comparing them with the best one. We select the best HSP (High Similarity Pair) per reference sequence and then choose the best HSP (that with lowest e value) between all the selected ones. The bitscore of this best HSP (called S) is used as reference to filter the rest of HSPs. All the HSPs with bitscore below p x S are filtered. p is a coefficient fixed by the user to define the bitscore required, e.g. if p=0.9 and S=700 the required bitscore threshold would be 630.
Once we have the definitive HSPs selected, we obtain their corresponding taxonomic nodes using the taxonomic assignments that NCBI provides for all the nt database sequences. Now we have to analyze the topological distribution of these nodes in the taxonomy tree: i. If all the nodes forms a line in the taxonomy tree (are located in a not branched lineage to the tree root) we should choose the most specific taxID as the final assignment for that read. We call to this kind of assignment the ‘in line’ exception (see Figure X right panel). ii. If not, we should search for the *sensu stricto* Lowest Common Ancestor (LCA) of all the selected taxonomic nodes (See Figure X left panel). In this approach we decided to use the bitscore for evaluating the similarity because it is a value that increases when similarity is higher and depends a lot on the length of the HSP.
Some reads could not find sequences with enough similarity in the database and then they would be classified as reads with no hits.
Advanced metagenomics analysis approaches [@huson2013microbial] have adopted LCA assignment algorithms because it provides fine and trusted taxonomical assignment.

#### Best BLAST hit taxonomic assignment

We decided to maintain the simpler method of Best BLAST Hit (BBH) for taxonomic assignment because, in some cases, it can provide information about the sequences that adds information to that obtained using LCA algorithm. Using LCA algorithm, when some reference sequences with BLAST alignments over the required thresholds map to a not sufficiently specific taxID, the read can be assigned to an unspecific taxon near to the root of the taxonomy tree. If the BBH reference sequence maps to more specific taxa, this method, in that case, gives us useful information.

### Output for LCA and BBH assignments

MG7 provides independent results for the 2 different approaches, LCA and BBH. The output files include, for each taxonomy node (with some read assigned), abundance values for direct assignment and cumulative assignment. The abundances are provided in counts (absolute values) and in percentage normalized to the number of reads of each sample. Direct assignments are calculated counting reads specifically assigned to a taxonomic node, not including the reads assigned to the descendant nodes in the taxonomy tree. Cumulative assignments are calculated including the direct assignments and also the assignments of the descendant nodes. For each sample MG7 provides 8 kinds of abundance values: LCA direct counts, LCA cumu. counts, LCA direct %, LCA cumu. %, BBH direct counts, BBH cumu. counts, BBH direct %, BBH cumu. %.

## Data analysis as a software project

**IDEAS**

- the user needs to write code, a repo would be nice, AWS account
- there's some sort of conf required for the AWS account (add users, roles, permissions, whatever)
- Why this is a good thing (or should this just be in Discussion)
- something else?

## Availability

MG7 is open source, available at https://github.com/ohnosequences/mg7 under an [AGPLv3](http://www.gnu.org/licenses/agpl-3.0.en.html) license.
















# Discussion
<!-- From instructions: This section may be divided by subheadings. Discussions should cover the key findings of the study: discuss any prior art related to the subject so to place the novelty of the discovery in the appropriate context; discuss the potential short-comings and limitations on their interpretations; discuss their integration into the current understanding of the problem and how this advances the current views; speculate on the future direction of the research and freely postulate theories that could be tested in the future. -->

## What MG7 brings

We could summarize the most innovative ideas and developments in MG7:

<!-- TODO this needs a couple of points on the bio side. My idea is to have section for each. -->
1. Treat data analysis as a software project. This makes for radical improvements in *reproducibility*, *reuse*, *versioning*, *safety*, *automation* and *expressiveness*
2. input and output data, their locations and type are expressible and checked at compile-time using *Datasets*
­3. management of dependencies and machine configurations using *Statika*
4. automation of AWS cloud resources and processes, including distribution and parallelization through the use of *Loquat*
5. taxonomic data and related operations are treated natively as what they are: graphs, through the use of *Bio4j*
6. MG7 provides a sustainable model for taxonomic assignment, appropriate to face the challenging amount of data that high throughput sequencing technologies generate

We will expand on each item in the following sections.

## A new approach to data analysis

MG7 proposes to define and work with a particular data analysis task as a software project, using Scala. The idea is that *everything*: data description, their location, configuration parameters, the infrastructure used, <!-- TODO --> ... should be expressed as Scala code, and treated in the same way as any (well-managed) software project. This includes, among other things, using version control systems (`git` in our case), writing tests, making stable releases following [semantic versioning](http://semver.org/) or publishing artifacts to a repository.

What we see as key advantages of this approach (when coupled with compile-time specification and checking), are

- **Reproducibility** the same analysis can be run again with exactly the same configuration in a trivial way.
- **Versioning** as in any software project, there can be different versions, stable releases, etc.
- **Reuse** we can build standard configurations on top of this and reuse them for subsequent data analysis. A particular data analysis *task* can be used as a *library* in further analysis.
- **Decoupling** We can start working on the analysis specification, without any need for available data in a much easier way.
- **Documentation** We can take advantage of all the effort put into software documentation tools and practices, such as in our case Scaladoc or literate programming. As documentation, analysis processes and data specification live together in the files, it is much easier to keep coherence between them.
- **Expresiveness and safety** For example in our case we can choose only from valid Illumina read types, and then build a default FLASH command based on that. The output locations, being declared statically, are also available for use in further analysis.

## Inputs, outputs, data: compile-time, expressive, composable
<!-- TODO some generic stuff here -->

## Tools, data, dependencies and machine configurations

## Parallel cloud execution ??
<!-- The Loquat thing -->

## Taxonomy and Bio4j

The hierarchic structure of the taxonomy of the living organisms is a tree, and, hence, is also a graph in which each node, with the exception of the root node, has a unique parent node. It led us to model the taxonomy tree as a graph using the graph database paradigm. Previously we developed Bio4j **[Pareja-Tobes-2015]**, a platform for the integration of semantically rich biological data using typed graph models. It integrates most publicly available data linked with sequences into a set of interdependent graphs to be used for bioinformatics analysis and especially for biological data.

## Future-proof

## MG7 Future developments

### Shotgun metagenomics

It is certainly possible to adapt MG7 to work with shotgun metagenomics data. Simply changing the reference database to include whole genome sequence data could yield interesting results. This could also be refined by restricting reference sequences according to all sort of criteria, like biological function or taxonomy. Bio4j would be an invaluable tool here, thanks to  its ability to express express complex predicates on sequences using all the information linked with them (GO annotations, UniProt data, NCBI taxonomy, etc).

### Comparison of groups of samples

### Interactive visualizations using the output files of MG7 (Biographika project)
















# Materials and Methods
<!-- As far as my understanding of this section goes, here we should put a list-like enumeration of what we use. I'm also adding descriptions, that could go somewhere else if needed. -->

## Amazon Web Services

<!-- TODO describe this minimally: EC2, SQS, S3 -->
We use EC2, S3 and SQS<!-- TODO no release here. Any other service? --> through a Scala wrapper of the official [AWS Java SDK](https://aws.amazon.com/sdk-for-java/), [ohnosequences/aws-scala-tools `0.13.2`](https://github.com/ohnosequences/aws-scala-tools/releases/tag/v0.13.2). This uses version `1.9.25` of the AWS Java SDK.

## Scala

MG7 itself and all the libraries used are written in Scala `2.11`.

## Statika

MG7 uses [ohnosequences/statika `2.0.0`](https://github.com/statika/statika/releases/tag/v0.2.0) for specifying the configuration and behavior of EC2 instances.

## Datasets

MG7 uses [ohnosequences/datasets `0.2.0`](https://github.com/ohnosequences/datasets/releases/tag/v0.2.0) for specifying input and output data, their type and their location.

## Loquat

MG7 uses [ohnosequences/loquat `2.0.0`](https://github.com/ohnosequences/loquat/releases/tag/v2.0.0) for the specification of data processing tasks and their execution using AWS resources.

## BLAST eDSL

MG7 uses [ohnosequences/blast `0.2.0`](https://github.com/ohnosequences/blast/releases/tag/v0.2.0). The BLAST version used is `2.2.31+`

## FLASH eDSL

MG7 uses [ohnosequences/flash `0.1.0`](https://github.com/ohnosequences/flash/releases/tag/v0.1.0). The FLASH version used is `1.2.11`

## Bio4j

MG7 uses [bio4j/bio4j 0.12.0-RC3](https://github.com/bio4j/bio4j/releases/tag/v0.12.0-RC3) and [bio4j/bio4j-titan 0.4.0-RC2](https://github.com/bio4j/bio4j-titan/releases/tag/v0.4.0-RC2) as an API for the NCBI taxonomy.












# Acknowledgements

*Funding:* The two first authors are funded by INTERCROSSING (Grant 289974).
