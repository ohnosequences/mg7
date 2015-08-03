### Metapasta
Metapasta is an open-source, fast and horizontally scalable tool for community profiling based on the analysis of 16S metagenomics data. It is entirely cloud-based and specifically designed to take advantage of it: it performs the community profiling of a sample starting from raw Illumina reads in approximately 1 hour, needing approximately the same time for doing the same on hundreds of samples. It uses BLAST or LAST, but other mapping solutions can be integrated. The taxonomic assignment is done using a best hit and a lowest common ancestor paradigm taking the NCBI taxonomy as reference. As an output, Metapasta generates the frequencies of all the identified taxa in any of the samples in tab-separated value text files. This output includes direct assignment frequencies and cumulative frequencies based on the hierarchical structure of the taxonomy tree. Reports format can be configured using DSL similar to spreadsheet formulas. PDF files with assigned taxonomy tree can be rendered.

Metapasta is implemented in Scala and based on cloud computing (Amazon Web Services). The graph data platform [Bio4j](www.bio4j.com) is used for retrieving taxonomy related information and the tool [Compota](http://ohnosequences.com/compota) is used for distributing and coordinating compute tasks.

[Usage](https://github.com/ohnosequences/metapasta/blob/master/doc/usage.md).

### Contacts

This project is maintained by [@evdokim](https://github.com/evdokim).
