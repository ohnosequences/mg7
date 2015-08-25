# metagenomica

The idea is to have different nisperos for each step and a global configuration which wires everything (names basically).

### Global configuration

- auth (keys, profiles, whatever)
- base S3 bucket + folder
- input (two FASTQ files per sample)

We create task providers at this level. This is important, as it actually guarantees that names do match. we can also check if all output files are there at the end etc. We should have data types and datasets for all the stuff involved:

- merged reads
- BLAST results
- ...

### Nisperos

#### FLASh merging

- **tasks** one per sample, links to two FASTQ files corresponding to the paired end reads (S3 objects)
- **instructions** Run FLASh on that, store the results in S3 scoped by sample name. Calculate number of reads *and* of merged reads.
- **dependencies** FLASh API, FLASh bundle

#### BLAST mapping

- **tasks** one per sample, link to one FASTA file with the merged reads from the previous step
- **instructions** run BLAST on that with a predefined batch size, merge with previous results, store the global results in S3 scoped by sample name
- **dependencies** BLAST API, BLAST bundle, 16SDB bundle

#### Taxonomic assignment

- **tasks** one per sample, link to the BLAST results from the previous step
- **instructions** First build a map with keys the merged reads and values a list of GIs (from the BLAST csv results). Then, translate (filtering duplicates) to taxids using a big map built from a text file. Once you have that, calculate assignments using the NCBI taxonomy in Bio4j. Lastly, calculate accumulated frequencies by starting with the initial direct assignment map and creating a new one going up through the taxonomy tree.
- **dependencies** Bio4j taxonomy bundle, Bio4j API, BLAST API, GI-Taxid bundle

#### Global stats

**OPTIONAL**

- **tasks** just one, with all the output files for assignment.
- **instructions** ...
- **dependencies**
