# Public dataset for testing purposes

We are using the public data from this 16S project [http://www.ebi.ac.uk/ena/data/view/PRJEB6592](http://www.ebi.ac.uk/ena/data/view/PRJEB6592)

**Project ID**

PRJEB6592

**Project title** (from the ENA project entry)

Illumina 16S bias evaluation

**Project description** (from the ENA project entry)

> Massively parallel sequencing of 16S rRNA genes enables the comparison of terrestrial, aquatic, and host-associated microbial communities with sufficient sequencing depth for robust assessments of both alpha and beta diversity. Establishing standardized protocols for the analysis of microbial communities is dependent on increasing the reproducibility of PCR-based molecular surveys by minimizing sources of methodological bias. In this study, we tested the effects of template concentration, pooling of PCR amplicons, and sample preparation/inter-lane sequencing on the reproducibility associated with paired-end Illumina sequencing of bacterial 16S rRNA genes. Using DNA extracts from soils and fecal samples as templates, we sequenced pooled amplicons and individual reactions for both high (5-10 ng) and low (0.1 ng) template concentrations. In addition, all experimental manipulations were repeated on two separate days and sequenced on two different MiSeq lanes. Although within-sample sequence profiles were highly consistent, template concentration had a significant impact on sample profile variability. Pooling of multiple PCR amplicons influenced the separation of all profiles from each sample and reduced within-sample heterogeneity, although these effects were not always significant. By comparison, sample preparation and inter-lane variability did not influence sample sequence data significantly. This systematic analysis underlines the importance of optimizing template concentration in order to minimize variability in microbial community surveys and indicates that the practice of pooling multiple PCR amplicons prior to sequencing contributes proportionally less to reducing bias in 16S rRNA gene surveys with next-generation sequencing.

Article describing the dataset [http://aem.asm.org/content/80/18/5717.full.pdf](http://aem.asm.org/content/80/18/5717.full.pdf)


Script used to get the data and put it in S3

```bash
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567374/ERR567374_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567374/ERR567374_2.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567375/ERR567375_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567375/ERR567375_2.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567384/ERR567384_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567384/ERR567384_2.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567385/ERR567385_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR567/ERR567385/ERR567385_2.fastq.gz

aws s3 cp --region eu-west-1 ./ s3://resources.ohnosequences.com/16s/public-datasets/PRJEB6592/reads/ --recursive
```

Reads metadata

| Sample ID      | Source            | Run           |
| :----          | --------:         |  --------:    |
|  SAMEA2661455  |    gut            |   ERR567374   |
|  SAMEA2661456  |    gut            |   ERR567375   |
|  SAMEA2661465  |    soil           |   ERR567384   |
|  SAMEA2661466  |    soil           |   ERR567385   |
