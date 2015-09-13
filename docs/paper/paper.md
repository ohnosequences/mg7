---
title: "MG7: Configurable and scalable 16S metagenomics data analysis -- new methods optimized for massive cloud computing"

authors:
- name: Alexey Alekhin
  affiliation: "_[oh no sequences!](http://ohnosequences.com)_ research group, [Era7 bioinformatics](http://www.era7bioinformatics.com)"
  email: "aalekhin@ohnosequences.com"
  position: 1
- name: Evdokim Kovach
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
  No abstract yet. Will be here.

keywords: "Metagenomics, 16S, Bacterial diversity profile, Bio4j, Graph databases, Cloud computing, NGS, Genomic big data"
---

# Introduction
<!-- TODO needs review -->

Metagenomics data analysis is growing at exponential rate during the last years. The increasing throughput of massively parallel sequencing technologies, the derived decreasing cost,  and the high impact of metagenomics studies, especially in human health (diagnostics, treatments, drug response, prevention), are crucial reasons responsible for this growth of Metagenomics. There is a growing interest in sequencing all kind of microbiomes (gut, mouth, skin, urinary tract, airway, milk, bladder), in different conditions of health and disease, or after different treatments. Metagenomics is also impacting environmental sciences, crop sciences, agrifood sector and biotechnology in general. This new possibilities for exploring the diversity of micro-organisms in the most diverse environments is opening many new research areas but, due to this wide interest, it is expected that the amount of data will be overwhelming in the short time [@stephens2015big].

Genome researchers have raised the alarm over big data in the past [nature news add ref](http://www.nature.com/news/genome-researchers-raise-alarm-over-big-data-1.17912) but even a more serious challenge might be faced with the metagenomics boom/ upswing. If we compare metagenomics data with other genomics data used in clinical genotyping we find a differential feature: the key role of time. Thus, for example, in some longitudinal studies, serial sampling of the same patient along several weeks (or years) is being used for the follow up of some intestinal pathologies, for studying the evolution of gut microbiome after antibiotic treatment, or for colon cancer early detection [@zeller2014potential]. This need of sampling across time adds more complexity to metagenomics data storage and demands adapted algorithms to detect state variations across time as well as idiosyncratic commonalities of the microbiome of each individual [@franzosa2015identifying].
In addition to the intra-individual sampling-time dependence, metagenomic clinical test results vary depending on the specific region of extraction of the clinical specimen. This local variability adds complexity to the analysis since different localizations (different tissues, different anatomical regions, healthy or tumour tissues) are required to have a sufficiently complete landscape of the human microbiome. Moreover, reanalysis of old samples using new tools and better reference databases might be also demanded from time to time.

During the last years other sciences as astronomy or particle physics are facing the big data challenge but, at least, these science have standards for data processing [@stephens2015big]. Global standards for converting raw sequence data into processed data are not yet well defined in metagenomics and there are shortcomings derived from the fact that many bioinformatics methodologies currently used for metagenomics data analysis were designed for a scenario very different that the current one. These are some of the aspects that have suffered crucial changes and advances with a direct impact in metagenomics data analysis. i. The first aspect is related to the sequences to be analyzed: the reads are larger, the sequencing depth and the number of samples of each project are considerably bigger. The first metagenomics studies were very local projects, while nowadays the most fruitful studies are done at a global level (international, continental, national). This kind of global studies has yielded the discovery of clinical biomarkers for diseases of the importance of cancer, obesity or inflammatory bowel diseases and has allowed exploring the biodiversity in many earth environments ii. The second aspect derives from the impressive genomics explosion, its effect being felt in this case in the reference sequences. The immense amount of sequences available in public repositories demands new approaches in curation, update and storage for metagenomics reference databases: current models will or already have problems to face the future avalanche of metagenomic sequences. iii. The third aspect to consider for metagenomics data analysis is related to the appearance of new models for massive computation and storage and to the new programming methodologies (Scala, ...) and new cloud models and resources. The immense new possibilities that these advances offer must have a direct impact in the metagenomics data analysis. iv. And finally the new social manner to do science, and especially genomic science is the fourth aspect to consider. Metagenomics evolves in a social and global scenario following a science democratization trend in which many small research groups from distant countries share a common big metagenomics project. This global cooperation demands systems allowing following exactly the same pipelines using equivalent cloud resources to modularly execute the analysis in an asynchronous way of working between different groups.
This definitively new scenario demands new methods and tools to handle the current and future volume of metagenomic data with the sufficient speed of analysis.
Considering all these aspects we have designed a new open source methodology for analyzing metagenomics data that exploits the new possibilities that cloud computing offers to get a system robust, programmatically configurable, modular, distributed, flexible, scalable and traceable in which the biological databases of reference sequences can be easily updated and/or frequently substituted by new ones or by databases specifically designed for focused projects.

# Results

## Overview

To tackle the challenges posed by metagenomics big data analysis outlined in the Introduction,

<!-- TODO order this -->
­- AWS resources in Scala (??)
- A new approach to data analysis specification, management and specification based on working with it in exactly the same way as for a software project, together with the extensive use of compile-time structures and checks.
- Parallelization and distributed analysis based on AWS, with on-demand infrastructure as the basic paradigm
­- fully automated processes, data and cloud resources management.
- Static reproducible specification of dependencies and behavior of the different components using *Statika* and *Datasets*
- Definition of complex pipelines using *Loquat* a composable system for scaling/parallelizing stateless computations especially designed for Amazon Web Services (AWS)
- Modeling of the taxonomy tree using the new paradigm of graph databases (Bio4j). It facilitates the taxonomic assignment tasks and the calculation of the taxa abundance values considering the hierarchic structure of taxonomy tree (cumulative values).
- per-read assignment (??)

## 16S Reference Database Construction

Our 16S Reference Database is a curated subset of sequences from NCBI nucleotide database **nt**. The sequences included were selected by similarity with the bacterial and archaeal reference sequences downloaded from the **RDP database** [@cole2013ribosomal]. RDP unaligned sequences were used to capture new 16S sequences from **nt** using BLAST similarity search strategies and then, performing additional curation steps to remove sequences with poor taxonomic assignments to taxonomic nodes close to the root of the taxonomy tree.
All the nucleotide sequences included in **nt** database has a taxonomic assignment provided by the **Genbank** sequence submitter. NCBI provides a table (available at ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/) to do the mapping of any Genbank Identifier (GI) to its Taxonomy Identifier (TaxID). Thus, we are based on a crowdsourced submitter-maintained taxonomic annotation system for reference sequences. It supposes a sustainable system able to face the expected number of reference sequences that will populate the public global nucleotide databases in the near future. Another advantageous point is that we are based on NCBI taxonomy, the *de facto* standard taxonomic classification for biomolecular data  [@cochrane20102010]. NCBI taxonomy is, undoubtedly, the most used taxonomy all over the world and the most similar to the official taxonomies of each specific field. This is a crucial point because all the type-culture and tissue databanks follow this official taxonomical classification and, in addition, all the knowledge accumulated during last decades is referred to this taxonomy. In addition NCBI provides a direct connection between taxonomical formal names and the physical specimens that serve as exemplars for the species [@federhen2014type].

Certainly, if metagenomics results are easily integrated with the theoretical and experimental knowledge of each specific area, the impact of metagenomics will be higher that if metagenomics progresses as a disconnected research branch. Considering that metagenomics data interoperability, which is especially critical in clinical environments, requires a stable taxonomy to be used as reference, we decided to rely on the most widely used taxonomy: the NCBI taxonomy. In addition, the biggest global sequence database GenBank follows this taxonomy to register the origin of all their submitted sequences.
Our 16S database building strategy allows the substitution of the 16S database by any other subset of **nt**, even by the complete **nt** database if it would be needed, for example, for analyzing shotgun metagenomics data. This possibility of changing the reference database provides flexibility to the system enabling it for easy updating and project-driven personalization.

## Bio4j and Graph Databases

## MG7 Pipeline Description

## Taxonomic Assignment Algorithms

### Lowest Common Ancestor based Taxonomic Assignment

For each read:

­1. Select only one BLASTN alignment (HSP) per reference sequence (the HSP with lowest e value)
2. Filter all the HSPs with bitscore below a defined BLASTN bitscore threshold s_0
3. Find the best bitscore value S in the set of BLASTN HSPs corresponding to hits of that read
4. Filter all the alignments with bitscore below p * S (where p is a fixed by the user coefficient to define the bitscore required, e.g. if p=0.9 and S=700 the required bitscore threshold would be 630)
5. Select all the taxonomic nodes to which map the reference sequences involved in the selected HSPs:
    - If all the selected taxonomic nodes forms a line in the taxonomy tree (are located in a not branched lineage to the tree root) we should choose the most specific taxID as the final assignment for that read
    - If not, we should search for the (sensu stricto) Lowest Common Ancestor (LCA) of all the selected taxonomic nodes (See Figure X)

In this approach the value used for evaluating the similarity is the bitscore that is a value that increases when similarity is higher and depends a lot on the length of the HSP

### Best BLAST hit taxonomic assignment

We have maintained the simpler method of Best BLAST Hit (BBH) taxonomic assignment because, in some cases, it can provide information about the sequences that can be more useful than the obtained using LCA algorithm. Using LCA algorithm when some reference sequences with BLAST alignments over the required thresholds map to a not sufficiently specific taxID, the read can be assigned to an unspecific taxon near to the root. If the BBH reference sequence maps to a more specific taxa this method, in that case, gives us useful information.

## Using MG7 with some example data-sets

<!-- ?? -->
We selected the datasets described in [Kennedy-2014] (??)

## MG7 availability

MG7 is open source, available at https://github.com/ohnosequences/mg7 under an [AGPLv3](http://www.gnu.org/licenses/agpl-3.0.en.html) license.

# Discussion
<!-- From instructions: This section may be divided by subheadings. Discussions should cover the key findings of the study: discuss any prior art related to the subject so to place the novelty of the discovery in the appropriate context; discuss the potential short-comings and limitations on their interpretations; discuss their integration into the current understanding of the problem and how this advances the current views; speculate on the future direction of the research and freely postulate theories that could be tested in the future. -->

## What MG7 brings

We could summarize the most innovative ideas and developments in MG7:

<!-- TODO this needs a couple of points on the bio side. My idea is to have section for each. -->
1. Treat data analysis as a software project. This makes for radical improvements in *reproducibility*, *reuse*, *versioning*, *safety*, *automation* and *expressiveness*
2. input and output data, their locations and type are expressible and checked at compile-time using our Scala library *datasets*
­3. management of dependencies and machine configurations using our Scala library *Statika*
4. automation of AWS cloud resources and processes, including distribution and parallelization through the use of *Loquat*
5. taxonomic data and related operations are treated natively as what they are: graphs, through the use of *Bio4j*
6. MG7 provides a sustainable model for taxonomic assignment, appropriate to face the challenging amount of data that high throughput sequencing technologies generate

We will expand on each item in the following sections.

## A new approach to data analysis

MG7 proposes to define and work with a particular data analysis task as a software project, using Scala. The idea is that *everything*: data description, their location, configuration parameters, the infrastructure used, ... should be expressed as Scala code, and treated in the same way as any (well-managed) software project. This includes, among other things, using version control systems (`git` in our case), writing tests, making stable releases following [semantic versioning](http://semver.org/) or publishing artifacts to a repository.

What we see as key advantages of this approach (when coupled with compile-time specification and checking), are

- **Reproducibility** the same analysis can be run again with exactly the same configuration in a trivial way.
- **Versioning** as in any software project, there can be different versions, stable releases, etc.
- **Reuse** we can build standard configurations on top of this and reuse them for subsequent data analysis. A particular data analysis *task* can be used as a *library* in further analysis.
- **Decoupling** We can start working on the analysis specification, without any need for available data in a much easier way.
- **Documentation** We can take advantage of all the effort put into software documentation tools and practices, such as in our case Scaladoc or literate programming. As documentation, analysis processes and data specification live together in the files, it is much easier to keep coherence between them.
- **Expresiveness and safety** For example in our case we can choose only from valid Illumina read types, and then build a default FLASH command based on that. The output locations, being declared statically, are also available for use in further analysis.

## Inputs, outputs, data: compile-time, expressive, composable

## Tools, data, dependencies and machine configurations

## Parallel cloud execution ??
<!-- The Loquat thing -->

## Taxonomy and Bio4j

The hierarchic structure of the taxonomy of the living organisms is a tree, and, hence, is also a graph in which each node, with the exception of the root node, has a unique parent node. It led us to model the taxonomy tree as a graph using the graph database paradigm. Previously we developed Bio4j **[Pareja-Tobes-2015]**, a platform for the integration of semantically rich biological data using typed graph models. It integrates most publicly available data linked with sequences into a set of interdependent graphs to be used for bioinformatics analysis and especially for biological data.

## Future-proof

## MG7 Future developments

Other possible uses of the general schema: statika, loquat, ...

### Shotgun metagenomics

It is certainly possible to adapt MG7 to work with shotgun metagenomics data. Simply changing the reference database to include whole genome sequence data could yield interesting results. This could also be refined by restricting reference sequences according to all sort of criteria, like biological function or taxonomy. Bio4j would be an invaluable tool here, thanks to  its ability to express express complex predicates on sequences using all the information linked with them (GO annotations, UniProt data, NCBI taxonomy, etc).

### Comparison of groups of samples

### Interactive visualizations using the output files of MG7 (Biographika project)

# Materials and Methods
<!-- As far as my understanding of this section goes, here we should put a list-like enumeration of what we use. I'm also adding descriptions, that could go somewhere else if needed. -->

## Amazon Web Services

<!-- TODO describe this minimally: EC2, SQS, S3 -->

## Scala

[Scala](http://www.scala-lang.org/) is a hybrid object-functional programming language which runs on Java Virtual Machine. It has support for type-level programming, type-dependent types (through type members) and singleton types, which permits a restricted form of dependent types where types can depend essentially on values determined at compile time (through their corresponding singleton types). Conversely, through implicits one can retrieve the value corresponding to a singleton type.

The other key feature for us is Java interoperability, which let us build on the vast number of existing Java libraries; we take advantage of this when using Bio4j as an API for the NCBI taxonomy.

MG7 itself and all the libraries used are written in Scala `2.11`.

## Statika

MG7 uses [ohnosequences/statika 2.0.0](https://github.com/statika/statika/releases/tag/v0.2.0) for specifying the configuration and behavior of EC2 instances.
<!-- TODO expand? -->
[Statika](https://github.com/ohnosequences/statika) is a Scala library developed by the first and last authors which serves as a way of defining and composing machine behaviors statically. The main component are **bundles**. Each bundle declares a sequence of computations (its behavior) which will be executed in an **environment**. A bundle can *depend* on other bundles, and when being executed by an environment, its DAG of dependencies is linearized and run in sequence. In our use, bundles correspond to what an EC2 instance should do and an environment to an image (AMI: **A**mazon **M**achine **I**mage) which prepares the basic configuration, downloads the Scala code and runs it.

MG7 uses [ohnosequences/statika 2.0.0](https://github.com/statika/statika/releases/tag/v0.2.0).

## Datasets
<!-- TODO
  reference to record types, tagged types etc

  - A calculus of tagged types, with applications to process languages
  - Extensible records with scoped labels

  This should add a reference to some of that, and explain that it is based on (essentially) an embedding of extensible record types in Scala. Maybe mention ohnosequences/cosas, our super-fancy library.
-->
[Datasets](https://github.com/ohnosequences/datasets) is a Scala library developed by the first and last authors to declare datasets and their locations. **Data** is represented as type-indexed fields: Keys are modeled as singleton types, and values correspond to what could be called a denotation of the key: a value of type `Location` tagged with the key type. Then a **Dataset** is essentially a collection of data, which are guaranteed statically to be different through type-level predicates, making use of the value ↔ type correspondence which can be established through singleton types and implicits. A dataset location is then just a list of locations formed by locations of each data member of that dataset. All this is based on what could be described as an embedding in Scala of an extensible record system with concatenation on disjoint labels, in the spirit of [@harper1990extensible, @harper1991record]. For that *Datasets* uses [ohnosequences/cosas 0.7.0](https://github.com/ohnosequences/cosas/releases/tag/v0.7.0).

Data keys can further have a reference to a **data type**, which, as the name hints at, can help in providing information about the type of data we are working with. For example, when declaring Illumina reads as a data, a data type containing information about the read length, insert size or end type (single or paired) is used.

A **location** can be, for example, an S3 object or a local file; by leaving the location type used to denote particular data free we can work with different "physical" representations, while keeping track of to which logical data they are a representation of. Thus, a process can generate locally a `.fastq` file representing the merged reads, while another can put it in S3 with the fact that they all correspond to the "same" merged reads is always present, as the data that those "physical" representations denote.

MG7 uses [ohnosequences/datasets 0.2.0](https://github.com/ohnosequences/datasets/releases/tag/v0.2.0).

## Loquat

[Loquat](https://github.com/ohnosequences/loquat) is a library developed by the first, second and last authors designed for the execution of embarrassingly parallel tasks using S3, SQS and EC2.

A **loquat** executes a process with explicit input and output datasets (declared using the *Datasets* library described above). Workers (EC2 instances) read from an SQS queue the S3 locations for both input and output data; then they download the input to local files, and pass these file locations to the process to be executed. The output is then put in the corresponding S3 locations.

A manager instance is used to monitor workers, provide initial data to be put in the SQS queue and optionally release resources depending on a set of configurable conditions.

Both worker and manager instances are Statika bundles. In the case of the worker, it can declare any dependencies needed to perform its task: other tools, libraries, or data.

All configuration such as the number of workers or the instance types is declared statically, the specification of a loquat being ultimately a Scala object. There are deploy and resource management methods, making it easy to use an existing loquat either as a library or from (for example) a Scala REPL.

The input and output (and their locations) being defined statically has several critical advantages. First, composing different loquats is easy and safe; just use the output types and locations of the first one as input for the second one. Second, data and their types help in not mixing different resources when implementing a process, while serving as a safe and convenient mechanism for writing generic processing tasks. For example, merging paired-end Illumina reads generically is easy as the data type includes the relevant information (insert size, read length, etc) to pass to a tool such as FLASH.

MG7 uses [ohnosequences/loquat 2.0.0](https://github.com/ohnosequences/loquat/releases/tag/v2.0.0).

## Type-safe EDSLs for BLAST and FLASH

<!-- TODO cite BLAST and FLASH -->
We developed our own Scala-based type-safe EDSLs (Embedded Domain Specific Language) for [FLASH](https://github.com/ohnosequences/flash) and [BLAST](https://github.com/ohnosequences/blast) expressions and their execution.

### BLAST EDSL

In the case of BLAST we use a model for expressions where we can guarantee for each BLAST command expression at compile time

- all required arguments are provided
- only valid options are provided
- correct types for each option value
- valid output record specification

Generic type-safe parsers returning an heterogeneous record of BLAST output fields are also available, together with output data defined using *Datasets* which have a reference to the exact BLAST command options which yielded that output. This let us provide generic parsers for BLAST output which are guaranteed to be correct, for example.

MG7 uses [ohnosequences/blast 0.2.0](https://github.com/ohnosequences/blast/releases/tag/v0.2.0).

### FLASH EDSL

In the same spirit as for BLAST, we implemented a type-safe EDSL for FLASH expressions and their execution, sporting features equivalent to those outlined for the BLAST EDSL.

MG7 uses [ohnosequences/flash 0.1.0](https://github.com/ohnosequences/flash/releases/tag/v0.1.0).

## Bio4j

[Bio4j](https://github.com/bio4j/bio4j) is a data platform integrating data from different resources such as UniProt or GO in a graph data paradigm. We use the module containing the NCBI Taxonomy, and the use their Java API from Scala in the assignment phase.

MG7 uses [bio4j/bio4j 0.12.0-RC3](https://github.com/bio4j/bio4j/releases/tag/v0.12.0-RC3) and [bio4j/bio4j-titan 0.4.0-RC2](https://github.com/bio4j/bio4j-titan/releases/tag/v0.4.0-RC2).

# Acknowledgements

INTERCROSSING (Grant 289974)
