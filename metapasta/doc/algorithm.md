### Merging reads


#### Input 

Samples paired-end 16S sequencing reads. Each sample has following fields:

* name
* address of S3 object with "left part" of reads in FASTQ fromat (.fastq.gz is also supported)
* address of S3 object with "right part" of reads in FASTQ fromat (.fastq.gz is also supported)

It is also possible to work with not pair-end samples by specifying the same S3 objects for left and right parts. 



#### Output

1. Sample chunks. Each chunk has following fields:
* name
* address of S3 object with merged reads in FASTQ format
* start position in the file (in bytes)
* last position in the file (in bytes)

2. Reads stats, Following values per each pair (sample, assignmentType):

* total amount of reads from the input sample
* total amount of reads from the input sample that have been merged
* total amount of reads from the input sample that have not been merged

3. S3 object with reads that have not been merged

#### Step description

1. downloading samples,
2. uncompressing them if it's necessary
3. if sample is pair-ended Metapasta merges it by the FLASh tool (http://ccb.jhu.edu/software/FLASH/):

```
flash 1.fastq 2.fastq
```

The tool produced following output:

* FASTQ file with merged reads 
* FASTQ file with not merged reads from the first FASTQ file
* FASTQ file with not merged reads from the second FASTQ file
* Counts for "Total", "Combined" and "Uncombined" reads

Then Metapasta uploads file with merged reads to S3 and produces sample chunks for this file. This splitting into chunks is needed to parallelize next mapping step.

Besides the chunks Metapasta also produce (using output of FLASh, or manual calculation if the input sample is not paired-end) the following reads statistics:

* total amount of reads from the input sample
* total amount of reads from the input sample that have been merged
* total amount of reads from the input sample that have not been merged

### Mapping to 16S database

#### Input 

Chunks of samples (name, S3 object, start and end position).

#### Output

List of hits for each  input chunk. Each hit has:

* read id
* reference id (from 16S database)
* score


#### Step description

1. Retrieving all reads from chunk S3 object that covered by start and end position in the chunk. 

2. Saving it into FASTA format.

3. Mapping the FASTA file again 16 database using command line options from Metapasta project configuration.

4. Parsing BLAST results and producing list of hits

BLAST can be replaced with any other mapping tool (e.g. LAST)



### Assignment to taxonomy tree

#### Input 

* Pairs of chunks with their corresponded hits

#### Output

For each sample, assignment algorithm (LCA and BBH) and taxon:
* total amount of reads that have been assigned to the taxon i.e. *direct frequency* of the taxon in the sample
* total amount of reads that have been assigned to the taxon or its descendant i.e. *accumulative frequency*

Reads stats. For each sample and assignment algorithm:

* amount of assignments to a taxon 
* amount of "NotAssigned"
* amount of "NoTaxIdAssignment"



#### Step description


1. Grouping all hits by their read id
2. Retrieving taxon identifiers from the reference identifiers from 16S database. Assigning the read to NoTaxIdAssignment in case the corresponded taxon identifiers are not found for all hits.
3. Running LCA and BBH assigning algorithms (see bellow) for each read and its hits together with retrieved taxon identifiers.
4. Counting for each assignment algorithm (LCA, BBH) and each taxon: *direct frequency* -- amount of reads that have been assigned to the taxon; *accumulative frequency* -- amount of reads that have been assigned to the taxon or its descendant taxa. 



### LCA assignment

#### Input

1. read id
2. list of hits with corresponded taxon id 

#### Output 

Taxon identifier or:

* NotAssigned
* NoTaxIdAssignment

#### Algorithm description

1. calculation maximum score from the list of hits -- M
2. filtering out all hits whose score is lower than global score threshold
3. filtering out all hits whose score is lower than M * c (where is c is coefficient from Metapasta project configuration, default value is 0.8). Assigning read to "NotAssigned" if all hits are filtered.
4. checking that the taxa of the rest of hits form a continuous line is in the taxonomy tree: if yes, the read is assigned to the most specific taxon; if not, the read is assigned to the lowest common ancestor of the rest of the taxa


### BBH assignment

#### Input

1. read id
2. list of hits with corresponded taxon id 

#### Output 

taxon identifier


#### Algorithm description

1. finding the taxon id with the maximal score in corresponded hit



