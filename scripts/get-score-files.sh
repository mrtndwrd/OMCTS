#!/bin/bash
for i in $(find output/*/* -name 'o_*')
do
	cat $i | scripts/get-score.sh > ${i}_score
done
