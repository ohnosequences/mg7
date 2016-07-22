#!/bin/sh
set -x
ME=`dirname $0`
pandoc --template=${ME}/peerj.tex ${ME}/../paper.md --smart -s --natbib --bibliography ${ME}/../refs -o ${ME}/paper.tex
pdflatex -output-directory ${ME} -halt-on-error ${ME}/paper.tex
bibtex ${ME}/paper
pdflatex -output-directory ${ME} -halt-on-error ${ME}/paper.tex
pdflatex -output-directory ${ME} -halt-on-error ${ME}/paper.tex
