To the Editor:


The NGS revolution has made Genomics a new Big Data player (http://www.nature.com/news/genome-researchers-raise-alarm-over-big-data-1.17912); by 2025 Genomics will be one of the four main Big Data sources. In this context, Metagenomics data is likely to represent the lion's share, due to its multidimensional complexity: sampling from particular environments (soil, sea, all the microbiomes, plants, ...), under changing conditions, from diverse populations, across different times. Thus the pressing need of new scalable and reproducible methods for data analysis and storage. Here we submit MG7, a new 16S metagenomics data analysis system capable of addressing these challenges. The basic principle is a new approach to data analysis, where configuration, processes, or data locations are static, type-checked and subject to the standard evolution of a well-maintained software project. At the infrastructure level, MG7 runs on top of Amazon Web Services, using a set of Scala libraries specifically developed for defining workflows composed of stateless computations, with a static reproducible specification of dependencies, behavior and wiring. Taxonomy data is based on graph databases, through Bio4j (http://bio4j.com). We also generated a 16S reference database with a sustainable update model, offering the possibility of project-guided reference customization.


Kind regards,

Eduardo Pareja-Tobes
