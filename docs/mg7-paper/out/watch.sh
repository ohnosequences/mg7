#!/bin/sh
ME=`dirname $0`
ls ${ME}/../paper.md | entr -rc ${ME}/out.sh
