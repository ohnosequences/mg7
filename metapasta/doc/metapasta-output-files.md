# Metapasta Output files Spec

## Output files description 

A Metapasta analysis generates files related to the following levels (from more general to more specific):

- **Project** files
- **Groups** files
- **Samples** files

In every Metapasta analysis the **Samples** are grouped into **Groups**, and all the groups make up the **Project**.

The folder structure is as follows

- Project
    - Group1
    - Group2
    - GroupN
    - Sample1
    - Sample2
    - SampleN

The files of each category should be placed under the corresponding folder.


---------------------------------------------------------------------
### FOLDERS and FILES 

This is a more detailed description of the folder structure with the kind of files that each folder includes:

- snapshot folder (like the root)
    - **Lots** of flash folders
    - Project folder (called here `results`)
        - Project result files (not massive)
        - Sample folder    
            - Sample result files (with the sample prefix)
            - Blast folder 
            - Reads folder
        - Group folder
            - Group result files (with the group prefix)

---------------------------------------------------------------------

### Files common to all the levels

All the following files must be generated for each level.

1. CSV files with all the frequencies (File type A). File name: ItemID.frequencies.csv
2. CSV files with all the frequencies of Kingdom (File type A). File name: ItemID.Kingdom.frequencies.csv
3. CSV files with all the frequencies of Phylum (File type A). File name: ItemID.Phylum.frequencies.csv
4. CSV files with all the frequencies of Class (File type A). File name: ItemID.Class.frequencies.csv
5. CSV files with all the frequencies of Order (File type A). File name: ItemID.Order.frequencies.csv
6. CSV files with all the frequencies of Family (File type A). File name: ItemID.Family.frequencies.csv
7. CSV files with all the frequencies of Genus (File type A). File name: ItemID.Genus.frequencies.csv
8. CSV files with all the frequencies of Species (File type A). File name: ItemID.Species.frequencies.csv


### Levels-specific files

The files described below are specific to each level and should be added to the files described above.

_Project-specific files_

9. viz `*.pdf`. File name: ItemID.frequencies.tree.pdf

10. Clean CSV files with direct absolute frequencies. Modification of file 1 for MetagenAssist usage (File type B). File name: ItemID.direct.absolute.counts.clean.csv
11. Clean CSV files with direct relative frequencies. Modification of file 1 for HeatMap viz (File type C). File name: ItemID.direct.percentage.clean.csv

_Groups-specific files_

12. Group specific CSV file with all the frequencies. Modification of file 1 (File type D). File name: ItemID.grouped.frequencies.csv

_Sample-specific files_

13. FASTA files with the read assignment in the header (File type E). File name: ItemID.reads.fasta
14. Blast/Last output files. File name: ItemID.blast.out
15. PDF reports and charts (File type F). File name: TBD

## Percentage

The percentage of the reads assigned at each taxa is obtained this way

```bash
Percentage of taxa X = absolute.counts of taxa X / Number of merged reads
```
Dividing the absolute counts of reads assigned to such taxa by the number of merged reads


## File format description

### File type A: Sample level

CSV files with these **columns**:

- TaxonomyID
- TaxonomyName
- TaxonomyRank
- TaxonomyLevel : @rtobes will provide you with a conversion table between `TaxonomyRank` and `TaxonomyLevel`
- Sample1ID.direct.absolute.counts: This is the number of reads directly assigned to such Taxa for that sample
- Sample1ID.direct.percentage: This is the percentage of reads directly assigned to such taxa for that sample.
- Sample1ID.cumulative.absolute.counts: This is the cumulative freq of that taxa for that sample
- Sample1ID.cumulative.percentage: This is the percentage value of the cumulative frequency of such taxa for that sample.
- Sample2ID.direct.absolute.counts: This is the number of reads directly assigned to such Taxa for that sample
- Sample2ID.direct.percentage: This is the percentage of reads directly assigned to such taxa for that sample.
- Sample2ID.cumulative.absolute.counts: This is the cumulative freq of that taxa for that sample
- Sample2ID.cumulative.percentage: This is the percentage value of the cumulative frequency of such taxa for that sample.
...
- SampleNID.direct.absolute.counts: This is the number of reads directly assigned to such Taxa for that sample
- SampleNID.direct.percentage: This is the percentage of reads directly assigned to such taxa for that sample.
- SampleNID.cumulative.absolute.counts: This is the cumulative freq of that taxa for that sample
- SampleNID.cumulative.percentage: This is the percentage value of the cumulative frequency of such taxa for that sample.

When it comes to **rows**:

- One row per taxa with assigned reads in **any** of the samples in the set
- One additional row with the data of reads **Not assigned via GI** (the column name should be `Not assigned via GI`)
- One additional row with the data of reads **Not assigned at this rank** (the column name should be `Not assigned at this rank`)
- One additional row with the data of reads **Not assigned due to BBH/LCA thresholds** (the column name should be `Not assigned due to threshold`)
- - One additional row with the data of reads **No Hit** (the column name should be `No hit`)
- One additional row with the total values of the counts and percentages for the **direct** assignments only (not cummulative)


### File type B: Project level absolute values

This file type is a modification of the file 1 and it is generated only for the _Project_ level.

The file name should be like this `ItemID.direct.absolute.counts.csv`

It is a CSV file with only these **columns**:

- TaxonomyName
- Sample1ID.direct.absolute.counts: This is the number of reads directly assigned to such Taxa for that sample
- Sample2ID.direct.absolute.counts: This is the number of reads directly assigned to such Taxa for that sample
...
- SampleNID.direct.absolute.counts: This is the number of reads directly assigned to such Taxa for that sample

And the **rows** as for the file type A


### File type C: Project level percentage values

This file type is a modification of the file 1 and it is generated only for the _Project_ level.

The file name should be like this `ItemID.direct.percentage.csv`

It is a CSV file with only these **columns**:

- TaxonomyName
- Sample1ID.direct.percentage: This is the percentage of reads directly assigned to such taxa for that sample. 
- Sample2ID.direct.percentage: This is the percentage of reads directly assigned to such taxa for that sample. 
...
- SampleNID.direct.percentage: This is the percentage of reads directly assigned to such taxa for that sample. 
- Total: this column contains the sum of the direct relative frequencies of all the samples

Only report in this file the **rows** that fulfill any of these 2 criteria:

- `Total` value is larger than the threshold x
- Any of the `direct.relative.freq` values is larger than the threshold x

### File type D: Group level average values

This file type is a modification of the file 1 and it is generated only for the _Group_ level.

The file name should be like this `ItemID.frequencies.complete.csv`

It has **all the columns** as the file type A plus these two columns:

- average.direct.percentage: This fields has the average value of the direct relative frequencies of all the samples in the set
- average.cumulative.percentage: This fields has the average value of the cumulative relative frequencies of all the samples in the set

And about the **rows**, exactly the same as for the file type A



### File type E: Fasta files with the read sequences with assignment data in the headers

FASTA file with the sequences of the reads. 

The header format must be like this

> > ReadID|SampleID|TaxonomyName|TaxonomyID|TaxonomyRank|ID of the 16S database sequence responsible for the assignment

It is important that you replace the spaces with underscore `_` in all the items included in the header

####About the ID of the sequence responsible for the assignment:

- With BBH algorithm the ID is unique and is defined
- With LCA the ID could be multiple when the assignment is done using LCA sensu stricto (more that 1 hit and all the hits  not in the same line of the taxonomy tree). In that case all the IDs involved in the assignment would have to be included in the header separated by the character "|".

### File type F: Reports and Charts

PDF reports and charts: TBD.


