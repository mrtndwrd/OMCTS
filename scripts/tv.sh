#!/bin/bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

while [[ 1 ]]
do
	clear
	cat output/*/* | $DIR/get-score.sh
	sleep 1
done
