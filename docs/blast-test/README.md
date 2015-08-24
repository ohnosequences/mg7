# Blast Tests

These tests were done to check whether blastn outputs the GI in a particular field

**Input**

These 4 reads

```bash
>M02288:145:000000000-AG3W5:1:1101:14822:1972 1:N:0:27
GTGTCAGCAGCCGCGGTAATACGTAGGGCGCAAGCGTTATCCGGATTTACTGGGTGTAA
AGGGAGCGTAGACGGTTTTGCAAGTCTGAAGTGAAAGCCCGGGGCTTAACCCCGGGACT
GCTTTGGATACTGTAGGTCTTGAGTGCAGGAGAGGTAAGTGGAATTCCTAGTGTAGCGG
TGAAATGCGTAGATATTAGGAGGAACACCAGTGGCGAAGGCGGCTTACTGGACTGTAAC
TGACGTTGAGGCTC
>M02288:145:000000000-AG3W5:1:1101:15578:1998 1:N:0:27
GTGTCAGCAGCCGCGGTAATACGGCGGATCCGAGCGTTATCCGGATTTATTGGGTTTAA
AGGGCGCGTAGGCGGGTCCTTAAGTCAGTTGTAAAAGTTTGCAGCTCAACCGTAGCACT
CCGTTTGATCCTTTGTCTCTTAGATGCGACGAGGGAGGCGGGACATGTTTAGTTGGGGG
GAAATTTATTGATTTTTCTTGGAACCCCCATTTCGGAGGGAGCCTTCCTGGCCTGTTGT
GGACGTTGATGCTG
>M02288:145:000000000-AG3W5:1:1101:16315:2013 1:N:0:27
GTGCCAGCCGCCGCGGTAATACGTAGGGGGCGAGCGTTGTCCGGATTTACTGGGCGTAA
AGGGTGCGTAGGCGGTTTGGTAAGTTGGATGTGAAATCCCCGGGCTTAACTTGGGGGCT
GCATCCAATACTGTCGGACTTGAGTGCAGGAGAGGAAAGCGGAATTCCTAGTGTAGCGG
TGAAATGCGTAGATATTAGGAGGAACACCGGTGGCGAAGGCGGCTTTCTGGACTGTAAC
TGACGCTGAGGCAC
>M02288:145:000000000-AG3W5:1:1101:14126:2110 1:N:0:27
GTGCCAGCAGCCGCGGTAATACGTAGGGGGCAAGCGTTATCCGGATTTACTGGGTGTAA
AGGGAGCGTAGACGGCTGTGCAAGTCTGAAGTGAAAGGCATGGGCTCAACCTGTGGACT
GCTTTGGAAACTGTGCAGCTAGAGTGTCGGAGAGGTAAGTGGAATTCCTAGTGTAGCGG
TGAAATGCGTAGATATTAGGAGGAACACCAGTGGCGAAGACGGCTTACTGGACGATGAC
TGACGTTGAGGCTG
```

**Instructions**

Two tests were run to see whether the `-show_gis` option was needed. Conclusions

- `-show_gis` option is not needed to output the subject GI in an independent field

- Setting the `-outfmt` to 7 and specifying `sgi` we have the subject GI in a independent field in the output file

Test 1

```bash
./ncbi-blast-2.2.31+/bin/blastn -db era7.16S.reference.sequences.0.1.0.fasta -query reads.fasta -out blastn.test.out.txt -outfmt "7 qseqid qlen sseqid sgi sacc slen qstart qend sstart send evalue" -show_gis  -num_alignments 10
```

Test 2

```bash
./ncbi-blast-2.2.31+/bin/blastn -db era7.16S.reference.sequences.0.1.0.fasta -query reads.fasta -out blastn.test2.out.txt -outfmt "7 qseqid qlen sseqid sgi sacc slen qstart qend sstart send evalue"  -num_alignments 10
```

Test 3

```bash
./ncbi-blast-2.2.31+/bin/blastn -db era7.16S.reference.sequences.0.1.0.fasta -query reads.fasta -out blastn.test3.out.txt -outfmt "10 qseqid qlen sseqid sgi sacc slen qstart qend sstart send evalue"  -num_alignments 10
```



**Output**

- `s3://era7p/metagenomica/data/out/blast-test/blastn.test.out.txt`
- `s3://era7p/metagenomica/data/out/blast-test/blastn.test2.out.txt`
