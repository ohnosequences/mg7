# Metapasta bugs and improvements

We are working on this at the level of the [ohnosequences/metapasta](https://github.com/ohnosequences/metapasta) codebase. We are **not** going to migrate metapasta to a new compota version. If there are changes to be made (according to the bugs and improvements listed below) to the underlying compota-like infrastructure, they will be made starting with the version used by metapasta at this moment.

Bugs and improvements are listed and detailed below, grouped into a set of (somewhat loose) categories

## Development and release process

### Stable dependencies

Metapasta needs to depend on libraries with *stable* versions, which themselves need to have proper releases.

### Release process

Use our standard release infrastructure: [ohnosequences/nice-sbt-settings](https://github.com/ohnosequences/nice-sbt-settings). The release process should be the one specified there. Note that this implies

- Follow semantic versioning. There should be git tags for each version.
- release notes
- proper tests, integrated in the release process

## Input checking

### Checking that all input resources exist

All input files should be checked before creating any resources. These checks should not involve downloading them.

### Input read count

Count the reads first before creating any resources.

## Metapasta Configuration

### Defining groups of samples

For groups of samples there should be an option to specify if we want to get the results for the complementary group (composed by all the samples not included in the defined group) of each defined sample group or not.

## Resource management

## Statika 2.0 deployment

The current home-grown approach needs to be changed to one based in statika 2.0.

### Autoscaling

All instances used should be wrapped in autoscaling groups.

### Resource tagging

Provided through the static configuration. There should be a global config which is used by the different metapasta steps.

## Output generation and reporting

### Output generation architecture

The output generation phase needs to be **totally independent** from the first part, which runs sequence comparisons and generates assignments. Its configuration should be of course independent too. It should be possible and easy to run first the phase that generates assignments, doing the report/output generation in a totally independent way.

### Output layout

The metapasta output layout needs to be amenable for human browsing and with a predictable name structure derived from the configuration and generic metapasta naming conventions.

There is a base bucket + prefix which is **user-configurable**. In our case, for example, we will use `s3://era7p/<project-name>/<analysis-name>` or something similar. From there, metapasta adds a `metapasta-<metapasta-version>` folder, which contains the results, following the folder structure of that version.

### Output description

The input of Metapasta is

1. a set of reads (divided in a set of samples) Metapasta
2. a taxonomy tree, with each node labeled with a set of sequences (the _reference database_)

From this, metapasta calculates

- a new labeling of the input taxonomy (actually one per assignment algorithm), the taxonomic assignment tree.
- a mapping from reads to one of the terminal read states (onesuch per assignment algorithm)

Let's describe both in detail:

#### Taxonomic assignment tree

The labeling is just the number of reads (for each sample) that are assigned to that node; this is just the product of numbers for each sample.

This tree can be represented in different ways (formats). Metapasta uses a csv representation where each row represents a node, together with

- node properties (name, phylum, whatever)
- parent node (thus the complete tree topology)
- labeling (the taxonomy assignment with counts per sample)

The order of the rows does not represent anything in particular. It could be useful to have them ordered according to topology?

There is of course one such taxonomic assignment tree per assignment algorithm. The name structure is

- `<analysis-name>/metapasta-<metapasta-version>/<algorithm>.assignment`

#### Read assignment

The other output is a function from read to terminal read states; those at which metapasta stops processing: see [this diagram](https://github.com/ohnosequences/metapasta/blob/f8b5598476bc911111ced4dcd4e7ffefc884284f/doc/Metapasta_reads_fate_V2.fw.png). The terminal states we are considering are:

- `NotMerged`
- `Assigned`
- `WrongTaxID`
- `WrongGI`
- `LowSim`
- `NoHit`

The format is a csv file per sample, with one read at each row. From here both global and per sample statistics can be calculated, also taking into account different groupings of the states above (like the current readstats file).

### Derived output

From the output labeled tree we just described (the taxonomic assignment tree) a number of derived output can be generated, independently of the assignment process. I describe the defaults:

The output structure reflects that of the analysis: Samples are grouped, the same sample can be in different groups at the same time, and each sample is in at least one group. There is always a group containing **all** samples. There are **three** levels

- project
- group
- sample

At each level (project, group, sample) we have output files containing the result of filtering the global output, keeping those corresponding to that level. In general, the file name is

- `<level>.<algorithm>.<taxa-rank>.<count-type>.<value-type>`

where

1. **level** is a project, group or sample
2. **algorithm** is `BBH` or `LCA` (plus maybe BLAST/LAST?)
3. **taxa rank** `all`, `kingdom`, `phylum`, `class`, `order`, `family`, `genus`, `species`; see below.
4. **count type** `direct` or `cumulative`
5. **value type** `count` or `frequency`

**IMPORTANT** frequencies are always calculated with respect to the XXX of reads.

##### Taxonomy ranks and subdivisions

The output is reported for the following taxonomy subtrees:

- `all` the whole taxonomy tree
- `kingdom` only these nodes
- `phylum` only these nodes
- `class` only these nodes
- `order` only these nodes
- `family` only these nodes
- `genus` only these nodes
- `species` only these nodes

#### Project specific output files

- **Project description** `<project>-description` including versions used, input etc
- **Project stats** ` project-stats` read below
- **PDF tree output** `<project>.phylum.tree.pdf` containing ???
- **MetagenAssist file** ???

#### Project stats file

This file contains statistics _for each assignment algorithm_ grouped according to the terminal read states; see [this diagram](https://github.com/ohnosequences/metapasta/blob/f8b5598476bc911111ced4dcd4e7ffefc884284f/doc/Metapasta_reads_fate_V2.fw.png). The terminal states we are considering are:

- `NotMerged`
- `Assigned`
- `WrongTaxID`
- `WrongGI`
- `LowSim`
- `NoHit`

and we will report the following groups (apart from the terminal states themselves)

- `WithHitWithoutAssignment` = `LowSim + WrongGI + WrongTaxID`
- `WithHit` = `Assigned + WithHitWithoutAssignment`
- `Merged` = `NoHit + WithHit`

The names are awful, but this is not so important now.

#### Group specific output files

- **Group description** `<group>-description` including project and complementary group, if present.

#### Sample specific output files

- `raw/` folder. This contains any sample specific logs, mapping raw results, etc
- `seqs/` FASTA files with the assignment included in the header (see FASTA header format below)

### FASTA output

For each sample we return a FASTA file comprised of the reads which were assigned. The header includes enough information so as to retrieve the assignment from it, if needed. The header format is

```
> "${readID}|${sampleID}|${TaxonName}|${TaxaID}|${TaxaRank}|${16SseqIDs}"
```

In the case of LCA, the last field will contain a list of 16S sequence IDs, separated by commas: those from which computing their LCA yielded that `${TaxaID}`.

#### Extra field for parent taxa

All output files which include taxa should have a field with their parent taxon.

#### Tree representation of abundance - PDF version

The PDF abundance representations should be restricted to the **phylum** level. It would be useful to have group-specific representations (for the average abundance of the samples of the group) with the same format that the PDFs provided for each sample.

#### Output consistency checks

Basic integrity checks. To name a few

- coherence between percentages and direct assignment counts
- sums across different counts should match totals
- ....
Status API Training Shop Blog About Help
© 2015 GitHub, Inc. Terms Privacy Security Contact
