#!/bin/bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

while [[ 1 ]]
do
	clear
	cat output/complete_output_* | $DIR/getScore.sh
	sleep 1
done
