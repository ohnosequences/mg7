---
keywords:[Metagenomics, 16S, Bacterial diversity profile, Bio4j, Graph databases, Cloud computing, NGS, Genomic big data]
---

# 1. Introduction
<!-- TODO needs review -->
Metagenomics data analysis is growing exponentially during the last few years. Genome researchers have raised the alarm over big data in the past (http://www.nature.com/news/genome-researchers-raise-alarm-over-big-data-1.17912)

There is a growing interest in sequencing all kind of microbiomes (gut, mouth, skin, urinary tract, airway, milk, bladder), in different conditions of health and disease, or after different treatments. The amount of data will be overwhelming in the short time. Old datasets will need to be reanalyzed against new databases from time to time.

If we compare metagenomics data with other genomics data used in clinical genotyping, we find a differential feature: the key role of time. Thus, for example, temporal metagenomics sampling from the same patient along several weeks (or years) is used for the follow up of some intestinal pathologies, for studying the evolution of gut microbiome after antibiotic treatment, or for colon cancer early detection [@Zeller-2014]. This need of sampling across time adds more complexity to metagenomics data storage and demands adapted algorithms to detect state variations across time as well as idiosyncratic commonalities of the microbiome of each individual [@Franzosa-2015].

<!--  
  TODO: what's this

  “genomics does not yet have standards for converting raw sequence data into processed data”
  “The world has a limited capacity for data collection and analysis, and it should be used well. Because of the accessibility of sequencing, the explosive growth of the community has occurred in a largely decentralized fashion, which can't easily address questions like this,"
  “Genomics poses some of the same challenges as astronomy, atmospheric science, crop science, particle physics and whatever big-data domain you want to think about”
  “Some major change is going to need to happen to handle the volume of data and speed of analysis that will be required.”
-->

New methods and tools are required to handle the volume of data with the sufficient speed of analysis. In this high growth of metagenomics are involved:

- The importance of metagenomics studies, especially in human health (diagnostics, treatments, drug response, prevention), environmental sciences and biotechnology
- The increasing throughput (and the derived decreasing cost) of massively parallel sequencing technologies

­Many bioinformatics methodologies currently used for metagenomics data analysis were designed for a very different scenario from the current one. These are some of the aspects that have suffered crucial changes and advances with a direct impact in metagenomics data analysis:

i. The first aspect is related to the sequences to be analyzed: the reads are larger, the sequencing depth and the number of samples of each project are considerably bigger. The first metagenomics studies were very local projects, while nowadays the most fruitful studies are done at a global level (international, continental, national). This kind of global studies has yielded the discover of clinical biomarkers for diseases of the importance of cancer, obesity or inflammatory bowel diseases.
ii. The second aspect derives from the impressive genomics explosion, its effect being felt in this case in the reference sequences. The immense amount of sequences available in public repositories demands new approaches in curation, update and storage for metagenomics reference databases: current models will or already have problems to face the future avalanche of metagenomic sequences.
iii. The appearance of new models for massive computation and storage, new programming methodologies and new cloud models and resources are the third aspect to consider for metagenomics data analysis.
iv. And finally the new social manner to do science, and especially genomic science is the fourth aspect to consider. Metagenomics evolves in a social and global scenario following a science democratization trend in which many small research groups from distant countries share a common big project. This global cooperation demands systems allowing following exactly the same pipelines using equivalent cloud resources to modularly execute the analysis in an asynchronous way of working between different groups.

Considering all these aspects we have designed a new open source methodology for analyzing metagenomics data that use the new possibilities that cloud computing offers to get a system robust, programmatically configurable, modular, distributed, flexible, scalable and traceable in which the reference biological databases can be easily updated and/or frequently substituted by new ones.

# 2. Materials and Methods
<!-- As far as my understanding of this section goes, here we should put a list-like enumeration of what we use. I'm also adding descriptions, that could go somewhere else if needed. -->

## 2.x Scala

[Scala](http://www.scala-lang.org/) is a hybrid object-functional programming language which runs on Java Virtual Machine. It has support for type-level programming, type-dependent types (through type members) and singleton types, which permits a restricted form of dependent types where types can depend essentially on values determined at compile time (through their corresponding singleton types). Conversely, through implicits one can retrieve the value corresponding to a singleton type.

The other key feature for us is Java interoperability, which let us build on the vast number of existing Java libraries; we take advantage of this when using Bio4j as an API for the NCBI taxonomy.

MG7 itself and all the libraries used are written in Scala `2.11`.

## 2.x Statika

<!-- TODO expand? -->
[Statika](https://github.com/ohnosequences/statika) is a Scala library developed by **so and so** which serves as a way of defining and composing machine behaviors statically. The main component are **bundles**. Each bundle declares a sequence of computations (its behavior) which will be executed in an **environment**. A bundle can *depend* on other bundles, and when being executed by an environment, its DAG of dependencies is linearized and run in sequence. In our use, bundles correspond to what an EC2 instance should do and an environment to an image (AMI: **A**mazon **M**achine **I**mage) which prepares the basic configuration, downloads the Scala code and runs it.

## 2.x Datasets

<!-- TODO explain this better, type-level resource lookup etc -->
[Datasets](https://github.com/ohnosequences/datasets) is a Scala library developed by **so and so** to declare datasets and their location. A particular piece of data is represented as a singleton type, with a reference to another singleton type representing its type.

These singleton types are then used as keys for describing *where* this data is located. A location can be an S3 object or a local file; through the use of type-level keys we can easily keep track at the type level of resources, so that a process which locally has an input a set of reads can use the corresponding static data key.

## 2.x Loquat

[Loquat](https://github.com/ohnosequences/loquat) is a library developed by so and so designed for the execution of embarrassingly parallel tasks using S3, SQS and EC2.

At the core there is a process with explicit input and output datasets (declared using the *Datasets* library described above). Workers will read from a SQS queue particular instances for the input data, consisting on the corresponding data locations at S3, together with where the corresponding outputs need to be placed at S3. This process corresponds to a bundle (which can declare any dependencies needed to perform its task). The input S3 objects will then be downloaded as local files, the process executed on them and the outputs written to their respective locations at S3. A *loquat* thus comprises essentially the process to be executed, the AWS configuration, and a set of inputs on which to run this process. There are deploy and resource management methods, making it easy to use it either as a library or from (for example) a Scala REPL.

The input and output (and their locations) being defined statically has several critical advantages. First, composing different loquats is easy and safe; just use the output types and locations of the first one as input for the second one.

## 2.x Type-safe DSLs for BLAST and FLASH

<!-- TODO cite BLAST and FLASH -->
We developed our own type-safe DSLs (Domain Specific Language) for [FLASH](https://github.com/ohnosequences/flash) and [BLAST](https://github.com/ohnosequences/blast) expressions and their execution.

### 2.x.a BLAST DSL

In the case of BLAST we use a model for expressions where we can guarantee for each BLAST command expression at compile time

- all required arguments are provided
- only valid options are provided
- correct types for each option value
- valid output record specification

Generic type-safe parsers returning an heterogeneous record of BLAST output fields are also available, together with output data defined using *Datasets* which have a reference to the exact BLAST command options which yielded that output.

### 2.x.b FLASH DSL

In the same spirit as for BLAST, ...

## 2.x Amazon Web Services

<!-- EC2, SQS, S3. -->

## 2.x Bio4j

[Bio4j](https://github.com/bio4j/bio4j) is a data platform integrating data from different resources such as UniProt or GO in a graph data paradigm. We use the module containing the NCBI Taxonomy, and the use their Java API from Scala in the assignment phase.

# 3. Results

## 3.1 Overview

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

## 3.x 16S Reference Database Construction

Our 16S Reference Database is a curated subset of sequences from NCBI nucleotide database **nt**. This subset of 16S sequences was selected by similarity with the bacterial and archaeal reference sequences downloaded from RDP database [@Cole-2014]. RDP unaligned sequences were used to capture new16S sequences from nt using BLAST similarity strategies and, then, performing additional curation steps to remove sequences with poor taxonomic assignments to taxonomic nodes close to the root of the taxonomic tree. All the nucleotide sequences included in nt database has a taxonomic assignment provided by the genbank sequence submitter. NCBI provides a table (available at ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/) to do the mapping of any Genbank Identifier (GI) to its Taxonomy Identifier (TaxID). Thus, we are based on a submitter-maintained taxonomic annotation system for reference sequences that supposes a sustainable system able to face the expected number of reference sequences that will populate the public global nucleotide databases in the near future. Another advantageous point is that we are based on NCBI taxonomy, the de facto standard taxonomic classification for biomolecular data [@Cochrane-2010]. NCBI taxonomy is, without any doubt, the most used taxonomy all over the world and the most similar to the official taxonomies of each specific field. This is a crucial point because all the type-culture and tissue databanks follow this official taxonomical classification and, in addition, all the knowledge accumulated is referred to this taxonomy. In addition NCBI provides a direct connection between taxonomical formal names and the physical specimens that serve as exemplars for the species [@Federhen-2015].

If metagenomics results are easily integrated with the theoretical and experimental knowledge of each specific area, the impact of metagenomics will be higher that if metagenomics progress in a disconnected research branch. This strategy for building our database allows substituting the 16S database by any other subset of nt, even by the complete nt database if it would needed, for example, for analyzing shotgun metagenomics data.

## 3.x Bio4j and Graph Databases

## 3.x MG7 Pipeline Description

## 3.x Taxonomic Assignment Algorithms

### 3.x.y Lowest Common Ancestor based Taxonomic Assignment

For each read:

­1. Select only one BLASTN alignment (HSP) per reference sequence (the HSP with lowest e value)
2. Filter all the HSPs with bitscore below a defined BLASTN bitscore threshold s_0
3. Find the best bitscore value S in the set of BLASTN HSPs corresponding to hits of that read
4. Filter all the alignments with bitscore below p * S (where p is a fixed by the user coefficient to define the bitscore required, e.g. if p=0.9 and S=700 the required bitscore threshold would be 630)
5. Select all the taxonomic nodes to which map the reference sequences involved in the selected HSPs:
    - If all the selected taxonomic nodes forms a line in the taxonomy tree (are located in a not branched lineage to the tree root) we should choose the most specific taxID as the final assignment for that read
    - If not, we should search for the (sensu stricto) Lowest Common Ancestor (LCA) of all the selected taxonomic nodes (See Figure X)

In this approach the value used for evaluating the similarity is the bitscore that is a value that increases when similarity is higher and depends a lot on the length of the HSP

### 3.x.z Best BLAST hit taxonomic assignment

We have maintained the simpler method of Best BLAST Hit (BBH) taxonomic assignment because, in some cases, it can provide information about the sequences that can be more useful than the obtained using LCA algorithm. Using LCA algorithm when some reference sequences with BLAST alignments over the required thresholds map to a not sufficiently specific taxID, the read can be assigned to an unspecific taxon near to the root. If the BBH reference sequence maps to a more specific taxa this method, in that case, gives us useful information.

## 3.x Other

General approach. An analysis is defined as a software project. It can evolve in the same way. We can run the analysis in a test phase, review configuration and changes, etc. Key advantages of this approach are

- **Reproducibility** the same analysis can be run again with exactly the same configuration in a trivial way.
- **Versioning** The analysis is a software project so it goes through the same stages, there can be different versions, stable releases, etc.
- **Reuse** we can build standard configurations on top of this and reuse them for subsequent data analysis.
- **Decoupling** We can start working on the analysis specification, without any need for data in a much easier way.
- **Expresiveness and safety** choose only from valid Illumina read types, build default FLASH command based on that, ...

## 3.x Using MG7 with some example data-sets

<!-- ?? -->
We selected the datasets described in [Kennedy-2014] (??)

## 3.7 MG7 availability

MG7 is open source, available at https://github.com/ohnosequences/mg7 under an [AGPLv3](http://www.gnu.org/licenses/agpl-3.0.en.html) license.

# 4. Discussion

<!-- From instructions: This section may be divided by subheadings. Discussions should cover the key findings of the study: discuss any prior art related to the subject so to place the novelty of the discovery in the appropriate context; discuss the potential short-comings and limitations on their interpretations; discuss their integration into the current understanding of the problem and how this advances the current views; speculate on the future direction of the research and freely postulate theories that could be tested in the future. -->

## 4.1 Novelty points of MG7

<!--  TODO fix all this -->

The most innovative ideas and developments integrated in MG7 are:

­- The management dependencies checking their correctness before compilation using Scala type system
- The automation of cloud resources and processes (parallelization management)
- The cloud-oriented development of the system including a modeling AWS resources based on the powerful data typing of Scala
- The use of the Graph databases paradigm to store and manage the taxonomy tree to obtain the taxonomic assignments and the cumulative frequencies
- MG7 provides a sustainable model for updating the database of reference sequences appropriate to face the challenging amount of sequences that are generating the new high throughput technologies of sequencing

## 4.2 Designed for future challenges

Other possible uses of the general schema: statika, loquat, ...

## 4.3 MG7 Future developments

### 4.3.1 Comparison of groups of samples

### 4.3.2 Interactive visualizations using the output files of MG7 (Biographika project)

# 5 Acknowledgements

INTERCROSSING (Grant 289974)

# 6 References

# 7 Tables and Figures
