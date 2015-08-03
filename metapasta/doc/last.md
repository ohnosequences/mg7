# LAST mapping tool

[LAST](http://last.cbrc.jp/) is a general purpose mapping tool that we are going to use in the *metapasta*. 
Main advantages of it in compare to BLAST are:

* performance (at least in 100 times faster)
* configurable alignment scoring schemes 

## Parameters

### Q
```
This option allows lastal to use sequence quality scores, or
PSSMs, for the queries.  0 means read queries in fasta format
(without quality scores); 1 means fastq-sanger format; 2 means
fastq-solexa format; 3 means fastq-illumina format; 4 means prb
format; 5 means read PSSMs.
```
**default:** 0 (FASTA)

#### value for metapasta
Working with FASTA files is easy because user shouldn't carry about format of quality. However, quality can improve results. See discussion here: https://github.com/ohnosequences/metapasta/issues/8  


### m
```
Maximum multiplicity for initial matches.  Each initial match is
lengthened until it occurs at most this many times in the
reference.
```

**default:** 10

#### value for metapasta

This parameters is critical when the reference contains a lot of repetitions, for example nt.16S database. Because length of reads are bounded LAST will never find initial matches for read that occurs in more than *m* reference sequences. So this parameter should be big enough (at least 20) to find initial matches for any read from queue, but in same time too big value can decrease performance.

### s

```
Specify which query strand should be used: 0 means reverse only,
1 means forward only, and 2 means both.
```

**default:** 2 for DNA

#### value for metapasta

Default value is fine, only one issue with it that it can produce some crappy alignments, but it is not a problem if we LAST doesn't skip right ones (see `m` option)


### T
```
Type of alignment: 0 means "local alignment" and 1 means
"overlap alignment".  Local alignments can end anywhere in the
middle or at the ends of the sequences.  Overlap alignments must
extend until they hit the end of one sequence.
```

**default:** 0

#### value for metapasta
Default value is fine, but if we sure that reads will cover all full reference strings we can choose -T1

### r, q and p

Match/Mismatch scores, match/mismatch score matrix

**default:** for DNA: (1, -1) Q=0, (6, -8) otherwise

#### value for metapasta
(1, -1) should be OK

## a
```
Gap existence cost.
```

**default:** for DNA: 7 (Q=0) and 21 (otherwise) 

#### value for metapasta
I don't understand why by default it much bigger that mismatch. (Are SNPs much more frequent than and deletions). Anyway it should differs a lot from q value.

## b  
```
Gap extension cost.  A gap of size k costs: a + b*k.
```

#### value for metapasta
Again it seams that I don't have some domain specific knowledge. In classical edit distance it just the same as *a* parameter.

## A, B
```
Insertion existence/extension cost.
```

**default:** a, b

#### value for metapasta
Default values are ok, because there is not big difference between deletion and insertion (isn't it?)

#### value for metapasta

### f
```
Choose the output format: 0 means tabular and 1 means MAF
```

**default:** 1 (MAF)

#### value for metapasta
Currently I'm using only tabular (should be faster), but probably it will be a good idea to add support for MAF format that contains more information (e.g. alignments)


### e
```
Minimum alignment score.  (If you do gapless alignment with
option -j1, then -d and -e mean the same thing.  If you set
both, -e will prevail.)
```

**default:** 40 for DNA

#### value for metapasta
Depends a lot on quality of queries: if this threshold is too low, a lot of reads will be unassigned. To small value (<50) can decrease performance sometimes leads to errors of last, but in fact doesn't change the results in "best hit" scenario. 

