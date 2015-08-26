# Public dataset for testing purposes

We are using the public data from this 16S project [http://www.ebi.ac.uk/ena/data/view/PRJEB6070](http://www.ebi.ac.uk/ena/data/view/PRJEB6070)

**Project ID**

ERP005534
PRJEB6070

**Project title** (from the ENA project entry)

Potential of fecal microbiota for early stage detection of colorectal cancer

**Project description** (from the ENA project entry)

> Several bacterial species have been implicated in the development of colorectal carcinoma (CRC), but CRC-associated changes of fecal microbiota and their potential for cancer screening remain to be explored. Here we used metagenomic sequencing of fecal samples to identify taxonomic markers that distinguished CRC patients from tumor-free controls in a study population of 156 participants. Accuracy of metagenomic CRC detection was similar to the standard fecal occult blood test (FOBT) and when both approaches were combined, sensitivity improved >45% relative to the FOBT while maintaining its specificity. Accuracy of metagenomic CRC detection did not differ significantly between early and late-stage cancer and could be validated in independent patient and control populations (N=335) from different countries. CRC-associated changes in the fecal microbiome at least partially reflected microbial community composition at the tumor itself, indicating that observed gene pool differences may reveal tumor-related host-microbe interactions. Indeed, we deduced a metabolic shift from fiber degradation in controls to utilization of host carbohydrates and amino acids in CRC patients accompanied by an increase of lipopolysaccharide metabolism.

Article describing the dataset [http://www.ncbi.nlm.nih.gov/pubmed/25432777](http://www.ncbi.nlm.nih.gov/pubmed/25432777)


Script used to get the data and put it in S3

```bash
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475467/ERR475467_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475467/ERR475467_2.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475472/ERR475472_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475472/ERR475472_2.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475468/ERR475468_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475468/ERR475468_2.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475469/ERR475469_1.fastq.gz
wget ftp://ftp.sra.ebi.ac.uk/vol1/fastq/ERR475/ERR475469/ERR475469_2.fastq.gz

aws s3 cp --region eu-west-1 ./ s3://resources.ohnosequences.com/16s/public-datasets/PRJEB6070/reads/ --recursive
```

Reads metadata

| Sample ID      | Diagnosis         | Run           |
| :----          | --------:         |  --------:    |
| SAMEA2448331   | Small adenoma     | ERR475467     |
| SAMEA2448336   | Large adenoma     | ERR475472     |
| SAMEA2448332   | Normal            | ERR475468     |
| SAMEA2448333   | Normal            | ERR475469     |
