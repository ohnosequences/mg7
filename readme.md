# metagenomica

The idea is to have different nisperos for each step and a global configuration which wires everything (names basically).

### Global configuration

- auth (keys, profiles, whatever)
- base S3 bucket + folder
- input (two FASTQ files per sample)
-

We create task providers at this level. This is important, as it actually guarantees that names do match. we can also check if all output files are there at the end etc.

### Nisperos

#### FLASh merging

- **tasks** one per sample, links to two FASTQ files corresponding to the paired end reads (S3 objects)
- **instructions** Run FLASh on that, store the results in S3 scoped by sample name.
- **dependencies** FLASh

#### BLAST mapping

- **tasks** one per sample, link to one FASTA file with the merged reads from the previous step
- **instructions** run BLAST on that, store the results in S3 scoped by sample name
- **dependencies** BLAST

#### Taxonomic assignment

- **tasks** one per sample, link to the BLAST results from the previous step
- **instructions** that funky code for assignment coming from metapasta
- **dependencies** Bio4j taxonomy

#### Global stats

- **tasks** just one, with all the output files for assignment.
- **instructions** again funky code from metapasta
- **dependencies** Bio4j taxonomy
