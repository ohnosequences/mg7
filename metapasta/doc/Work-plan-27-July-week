# Metapasta Plan - 2015-07-27 week

During the first week we will focus on

- General project improvements: build process, dependencies, versioning, deployment, etc
- Metapasta configuration definition: simpler, better interface, extensions.
- Input checks: existence, validation.

## General project improvements

### Stable dependencies

Metapasta needs to depend on libraries with *stable* versions, which themselves need to have proper releases.

### Scala 2.11

Everything should be based on Scala 2.11

### Release process

Use our standard release infrastructure: [ohnosequences/nice-sbt-settings](https://github.com/ohnosequences/nice-sbt-settings). The release process should be the one specified there. This implies

- Follow semantic versioning. There should be git tags for each version.
- release notes
- proper tests, integrated in the release process

## Metapasta configuration

### Simplify configuration classes

Right now they contain a lot of stuff that the user should not need to specify.

### Defining groups of samples

For groups of samples there should be an option to specify if we want to get the results for the complementary group (composed by all the samples not included in the defined group) of each defined sample group or not.

### Output generation configuration

As the output process needs to be independent, it needs to have a separate configuration.

### Key management

Better, clearer, credentials management.

## Input checking

### Checking that all input resources exist

All input files should be checked before creating any resources. These checks should not involve downloading them.

### Input read count

Count the reads first before creating any resources.
