### how to use compota?
very compuation with compota should form separated sbt project with instructions, datasets, compota configuration


### how to obtain of such project?

nisperoCLI is a command line tool that allows download compota projects tempaltes from github

how to install nisperoCLI?

* install cs: `curl https://raw.githubusercontent.com/n8han/conscript/master/setup.sh | sh`
* install nispero super cli: `cs ohnosequences/nisperoCLI -b super-cli` or `~/bin/cs ohnosequences/nisperoCLI -b super-cli` or  [manually](https://github.com/ohnosequences/nisperoCLI/blob/master/doc/installation.md)
nispero create <template_repository>

### how to use it?

`nispero create <template_repository>`

e.g.

`nispero create ohnosequences/metapasta.g8`


[nisperoCLI documentation](https://github.com/ohnosequences/nisperoCLI/blob/master/doc/universal-cli-tool.md)


### how to launch compota project?

publish it:
`sbt publish`

run:

`sbt "run run"`

### how to check the status of my computations?

the link to compota web console will be sent to your e-mail
