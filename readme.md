## MG7

[![](https://travis-ci.org/ohnosequences/mg7.svg?branch=master)](https://travis-ci.org/ohnosequences/mg7)
[![](https://img.shields.io/codacy/96ad3cc701a54c548deb4ef0d5564655.svg)](https://www.codacy.com/app/era7/mg7)
[![](https://img.shields.io/github/release/ohnosequences/mg7.svg)](https://github.com/ohnosequences/mg7/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/ohnosequences/mg7)

A tool for 16S metagenomics data analysis using Scala and Amazon Web Services.

You can find some documentation in the [docs/](docs/) folder and read the preprint:

#### MG7: Configurable and scalable 16S metagenomics data analysis

_Alexey Alekhin, Evdokim Kovach, Marina Manrique, Pablo Pareja-Tobes, Eduardo Pareja, Raquel Tobes, Eduardo Pareja-Tobes_  

- bioRxiv doi: <http://dx.doi.org/10.1101/027714>
- available under a [CC-BY-ND 4.0 International license](http://creativecommons.org/licenses/by-nd/4.0/)

#### Abstract

As part of the Cambrian explosion of omics data, metagenomics brings to the table a specific, defining trait: its social essence. The *meta* prefix exerts its influence, with multitudes manifesting themselves everywhere; from samples to data analysis, from actors involved to (present and future) applications. Of these dimensions, data analysis is where needs lay further from what current tools provide. Key features are, among others, scalability, reproducibility, data provenance and distribution, process identity and versioning. These are the goals guiding our work in MG7, a 16S metagenomics data analysis system. The basic principle is a new approach to data analysis, where configuration, processes, or data locations are static, type-checked and subject to the standard evolution of a well-maintained software project. Cloud computing, in its Amazon Web Services incarnation, when coupled with these ideas, produces a robust, safely configurable, scalable tool. Processes, data, machine behaviors and their dependencies are expressed using a set of libraries which bring as much as possible checking and validation to the type level, without sacrificing expressiveness. Together they form a toolkit for defining scalable cloud-based workflows composed of stateless computations, with a static reproducible specification of dependencies, behavior and wiring of all steps. The modeling of taxonomy data is done using Bio4j, where the new paradigm of graph databases allows for both a simple expression of taxonomic assignment tasks and the calculation of taxa abundance values considering the hierarchic structure of the taxonomy tree. MG7 includes a new 16S reference database, *16S-DB7*, built with a flexible and sustainable update system, and the possibility of project-driven personalization. The first and second authors contributed equally to this work.
