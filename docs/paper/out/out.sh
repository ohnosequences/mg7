#!/bin/sh
set -x
ME=`dirname $0`
pandoc --template=${ME}/frontiers.tex ${ME}/../paper.md --smart -s -o ${ME}/paper.tex
cd ${ME}
pdflatex -halt-on-error paper.tex
pdflatex -halt-on-error paper.tex
