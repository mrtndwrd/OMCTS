#!/bin/bash
for i in $(find outputPaperAll/Q-LEARNING/* -name 'o_*')
do
	cat $i | scripts/get-score.sh > ${i}_score
done
