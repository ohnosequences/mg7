#!/bin/sh
set -x
ME=`dirname $0`
pandoc --template=${ME}/frontiers.tex ${ME}/../paper.md --smart -s -o ${ME}/paper.tex
pdflatex ${ME}/paper.tex
pdflatex ${ME}/paper.tex
