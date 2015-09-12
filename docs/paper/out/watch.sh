#!/bin/sh
set -x
ME=`dirname $0`
ls ${ME}/../paper.md ${ME}/refs.bib | entr -c ${ME}/out.sh
