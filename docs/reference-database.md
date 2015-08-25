# 16S Reference database

## FASTA file with reference sequences

The database was built taking the sequences from `nt` that showed similarity with the sequences from the `RDP` database and removing the following sequences:

- They are assigned to taxa with level 3 or higher

- They belong to any of these groups

    - `root; cellular organisms; Bacteria ; unclassified Bacteria (Taxonomy ID: 2323)`

    - `root; cellular organisms; Bacteria ; environmental samples (Taxonomy ID: 48479)`

    - `root; cellular organisms; Archaea ; unclassified Archaea (Taxonomy ID: 29294)`

    - `root; cellular organisms; Archaea ; environmental samples (Taxonomy ID: 48510)`

    - `root; unclassified sequences (Taxonomy ID: 12908)`

    - `other sequences; artificial sequences ; Synthetic construct (Taxonomy ID: 32630)`

This is the FASTA file with the sequences to be used as reference sequences `s3://resources.ohnosequences.com/sequences/nt.rdp.archaea-and-bacteria-only.fasta.tar.gz`

This file describes more in detail how this file was built [https://github.com/era7bio/archive/blob/68c75c65b319435acb2efd5e33d51d5d676cb0f2/mg7-16s/doc/test-16s-database/building-16s-blast-database.md](building-16s-blast-database.md)

The file

- is 698M nt.rdp.archaea-and-bacteria-only.fasta

- contains **682,914 sequences**

## Blast database

**Input**

`s3://resources.ohnosequences.com/16s/era7.16S.reference.sequences.0.1.0.fasta.gz`

**Output**

`s3://resources.ohnosequences.com/16s/era7.16S.reference.sequences.blastdb.tgz`

**Instructions**

```bash
mv nt.rdp.archaea-and-bacteria-only.fasta era7.16S.reference.sequences.0.1.0.fasta
ncbi-blast-2.2.31+/bin/makeblastdb -in era7.16S.reference.sequences.0.1.0.fasta -parse_seqids -dbtype nucl
```
