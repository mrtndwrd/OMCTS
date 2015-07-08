#!/bin/bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

while [[ 1 ]]
do
	clear
	cat output2.txt | $DIR/getScore.sh
	sleep 1
done
