# Paper draft

<!-- TODO: title, authors, keywords etc -->

## 1. Introduction

It is increasing daily the interest in sequencing all kind of microbiomes (gut, mouth, skin, urinary tract, airway, milk, bladder) in different conditions of health and disease or after different treatments. The amount of data will be overwhelming in the short time. In addition, old datasets could be reanalyzed against new databases from time to time.

In addition, if we compare metagenomics data with other genomics data used in clinical genotyping we find a differential feature that is its mutability along time.
Thus, for example, serial metagenomics sampling of the same patient along several weeks or years is used to the follow up of some intestinal pathologies or for study the evolution of gut microbiome after antibiotic treatment, or for colon cancer early detection [@Zeller-2014]. This need of serial sampling longitudinally over time adds more complexity to the problem of metagenomics data storage and demands adapted algorithms to detect state variations along time as well as idiosyncratic commonalities of the microbiome of each individual [@Franzosa-2015].

<!--  
  TODO: what's this

  “genomics does not yet have standards for converting raw sequence data into processed data”
  “The world has a limited capacity for data collection and analysis, and it should be used well. Because of the accessibility of sequencing, the explosive growth of the community has occurred in a largely decentralized fashion, which can't easily address questions like this,"
  “Genomics poses some of the same challenges as astronomy, atmospheric science, crop science, particle physics and whatever big-data domain you want to think about”
  “Some major change is going to need to happen to handle the volume of data and speed of analysis that will be required.”
-->

New methods and tools are required to handle the volume of data with the sufficient speed of analysis. In this high growth of metagenomics are involved:

- The importance of metagenomics studies especially in human health (diagnostics, treatments, drug response, prevention) environmental sciences and biotechnology
- The increasing throughput, and the derived decreasing cost, of massively parallel sequencing technologies

­Many bioinformatics methodologies currently used for metagenomics data analysis were designed for a scenario very different that the current one. These are some of the aspects that have suffered crucial changes and advances with a direct impact in metagenomics data analysis.

i. The first aspect is related to the sequences to be analyzed: the reads are larger, the deep of per sample sequencing and the number of samples of each project are considerably bigger. The first metagenomics studies were very local projects, but, on the contrary, now, the most fruitful studies are done at a global level (international, continental, national). This kind of global studies is allowing discovering clinical biomarkers for diseases of the importance of cancer, obesity or inflammatory bowel diseases.
ii. The second aspect derives from the impressive genomics explosion and affect to the reference sequences. The immense amount of sequences available in the public repositories demands new models of curation, updating and storage for metagenomics reference databases since current models have problems to face the future avalanche of metagenomic sequences.
iii. The appearance of new models for massive computation and storage, new programming methodologies (scala,..) and new cloud models and resources are the third aspect to consider for metagenomics data analysis.
iv. And finally the new social manner to do science, and especially genomic science is the forth aspect to consider. Metagenomics evolves in a social and global scenario following a science democratization trend in which many small research groups from distant countries share a common big project. This global cooperation demands systems allowing following exactly the same pipelines using equivalent cloud resources to modularly execute the analysis in an asynchronous way of working between different groups.

Considering all these aspects we have designed a new open source methodology for analyzing metagenomics data that use the new possibilities that cloud computing offers to get a system robust, programmatically configurable, modular, distributed, flexible, scalable and traceable in which the reference biological databases can be easily updated and/or frequently substituted by new ones.

## 2. Materials and Methods
<!--
  As far as my understanding of this section goes, here we should put a list-like enumeration of what we use
-->

### 2.x Scala

Scala is a hybrid object-functional programming language which runs on Java Virtual Machine. It has support for type-level programming, type-dependent types (through type members and singleton types), and Java interoperability.

### 2.x Statika

Statika is a Scala library developed by so and so which serves as a way of defining, composing and using machine behaviors statically. The main component are *bundles*. Bundles can depend on other bundles. ...
<!-- TODO explain statika, images, environments, configurations -->

### 2.x Datasets

<!-- TODO the idea, based on cosas, locations (`File`s, S3, ...) -->

### 2.x Loquat

Loquat is a library developed by so and so designed for the execution of embarrassingly parallel tasks using S3, SQS and EC2. Features are

1. compile-time definition of input and output types
2. both workers and masters behavior corresponds to Statika bundles

### 2.x Type-safe DSLs for BLAST and FLASH

We developed our own type-safe DSLs for FLASH and BLAST expressions and their execution. Useful on their own, explain why we use them (somewhere else?)

### 2.x Amazon Web Services

<!-- EC2, SQS, S3. -->

## 3. Results

### 3.x 16S Reference Database Construction

Our 16S Reference Database is a curated subset of sequences from NCBI nucleotide database `nt`. This subset of 16S sequences was selected by similarity with the bacterial and archaeal reference sequences downloaded from RDP database [@Cole-2014]. RDP unaligned sequences were used to capture new16S sequences from nt using BLAST similarity strategies and, then, performing additional curation steps to remove sequences with poor taxonomic assignments to taxonomic nodes close to the root of the taxonomic tree. All the nucleotide sequences included in nt database has a taxonomic assignment provided by the genbank sequence submitter. NCBI provides a table (available at ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/) to do the mapping of any Genbank Identifier (GI) to its Taxonomy Identifier (TaxID). Thus, we are based on a submitter-maintained taxonomic annotation system for reference sequences that supposes a sustainable system able to face the expected number of reference sequences that will populate the public global nucleotide databases in the near future. Another advantageous point is that we are based on NCBI taxonomy, the de facto standard taxonomic classification for biomolecular data [@Cochrane-2010]. NCBI taxonomy is, without any doubt, the most used taxonomy all over the world and the most similar to the official taxonomies of each specific field. This is a crucial point because all the type-culture and tissue databanks follow this official taxonomical classification and, in addition, all the knowledge accumulated is referred to this taxonomy. In addition NCBI provides a direct connection between taxonomical formal names and the physical specimens that serve as exemplars for the species [@Federhen-2015].

If metagenomics results are easily integrated with the theoretical and experimental knowledge of each specific area, the impact of metagenomics will be higher that if metagenomics progress in a disconnected research branch. This strategy for building our database allows substituting the 16S database by any other subset of nt, even by the complete nt database if it would needed, for example, for analyzing shotgun metagenomics data.

### 3.x Bio4j and Graph Databases

### 3.x MG7 Pipeline Description

### 3.x Taxonomic Assignment Algorithms

#### 3.x.y Lowest Common Ancestor based Taxonomic Assignment

For each read:

­1. Select only one BLASTN alignment (HSP) per reference sequence (the HSP with lowest e value)
2. Filter all the HSPs with bitscore below a defined BLASTN bitscore threshold s_0
3. Find the best bitscore value S in the set of BLASTN HSPs corresponding to hits of that read
4. Filter all the alignments with bitscore below p * S (where p is a fixed by the user coefficient to define the bitscore required, e.g. if p=0.9 and S=700 the required bitscore threshold would be 630)
5. Select all the taxonomic nodes to which map the reference sequences involved in the selected HSPs:
    - If all the selected taxonomic nodes forms a line in the taxonomy tree (are located in a not branched lineage to the tree root) we should choose the most specific taxID as the final assignment for that read
    - If not, we should search for the (sensu stricto) Lowest Common Ancestor (LCA) of all the selected taxonomic nodes (See Figure X)

In this approach the value used for evaluating the similarity is the bitscore that is a value that increases when similarity is higher and depends a lot on the length of the HSP

#### 3.x.z Best BLAST hit taxonomic assignment

We have maintained the simpler method of Best BLAST Hit (BBH) taxonomic assignment because, in some cases, it can provide information about the sequences that can be more useful than the obtained using LCA algorithm. Using LCA algorithm when some reference sequences with BLAST alignments over the required thresholds map to a not sufficiently specific taxID, the read can be assigned to an unspecific taxon near to the root. If the BBH reference sequence maps to a more specific taxa this method, in that case, gives us useful information.

### 3.x Other

General approach. An analysis is defined as a software project. It can evolve in the same way. We can run the analysis in a test phase, review configuration and changes, etc. Key advantages of this approach are

- **Reproducibility** the same analysis can be run again with exactly the same configuration in a trivial way.
- **Versioning** The analysis is a software project so it goes through the same stages, there can be different versions, stable releases, etc.
- **Reuse** we can build standard configurations on top of this and reuse them for subsequent data analysis.
- **Decoupling** We can start working on the analysis specification, without any need for data in a much easier way.
- **Expresiveness and safety** choose only from valid Illumina read types, build default FLASH command based on that, ...
