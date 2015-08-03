## metapasta input, output and formats

### input

* **samples** - list of S3 objects that contain paired reads in FASTQ format
* **database** - not actually part of metapasta, it will be prepared BLAST database stored in S3 as ZIP archive. Database ids should contain GI in headers.

### assignment

so far metapasta will support only one algorithm of assignment best BLAST hit (threshold should be specified)

### output

* **table with reads** - dynamodb table that contains following attributes: read_id, sample_id, sequence, ref_id (from BLAST database), tax_id, tax_name, tax_name, sequence. For reads without hit last attributes will be empty.

* **annotated FASTA files** for every sample (with same fields as in table). actually with file can be generated from the table in the end.

* **assignment table** - table (dynamo or cvs, we should decide) with information for every tax_id presented in assignments: tax_id, name (from bio4j), level (from bio4j), rank (from bio4j), and then for every sample: hits_count (how many ready were mapped to this tax_id), hits_count_accumulated (how many ready were mapped to this tax_id and its children).

## doubts and questions

### from FASTQ to FASTA
how to join paired read to single, and then transform FASTQ to FASTA? we need some robust tool for it

### FASTA format
It is important for BLAST to have identifiers of reads without spaces. So we should take it into account when we will process out FASTQ files with the tool.
The same for the database, ids shouldn't contain spaces, also metapasta should know how to extract GI from header (this code can be part of database bundle)


### how to run BLAST?
so far I use this command for run BLAST:
```
blastn -task megablast -db nt.rdp.subset.filtered.by.ncbi.id.new.fasta -query reads.fasta -out result -max_target_seqs 1 -num_threads 1 -outfmt 6 -show_gis
```
probably for users it will be useful to have ability to modify it, add thresholds for example. I suggest to use some template like:

```
blastn -task megablast -db $db$ -query $reads$ -out $result$ -max_target_seqs 1 -num_threads 1 -outfmt 6 -show_gis
```

### unknown reads.
what metapasta should do with it. Do we need one extra item in assignment table that correspond to these reads



