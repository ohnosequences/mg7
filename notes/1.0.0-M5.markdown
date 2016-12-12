This milestone doesn't add many new features, but rather focuses on the internal code improvements and testing with a new reference database and several datasets. Here are the main changes since the [v1.0-M4 release](https://github.com/ohnosequences/mg7/releases/tag/v1.0.0-M4):

* #71, #113: BLAST output filtering based on the maximum `pident` is now configurable
* #112: Breaking changes to the way MG7 pipelines are defined and used in the user code: thanks to the improvements in Loquat user doesn't need to write boilerplate code to define an MG7 pipeline.
* #107, #63: Stats and Summary steps are removed from the pipeline
* #84, #96, #97, #98, #99: Global code review and refactoring
* #78: Switched to the CSV format for all tables
* #95: Bio4j taxonomy-related code is now in a separate project: [ohnosequences/ncbitaxonomy](https://github.com/ohnosequences/ncbitaxonomy)
* #86, #102 : Added Illumina and Pacbio BEI mock communities as test pipelines

----

See the full list of pull requests merged in this release in the [v1.0-M4 milestone](https://github.com/ohnosequences/mg7/issues?q=milestone%3Av1.0-M4).
