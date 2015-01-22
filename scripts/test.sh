#!/bin/bash
MAX=$1
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
# Get into `root` directory
cd $DIR
cd ..
# Create and empty (if needed) output dir
mkdir -p output
rm output/*
for i in $(eval echo {1..$MAX})
do
	echo $i
	# Run test and remove the q-table data
	ant run | scripts/getScore.sh > output/o$i
	rm test
done
python scripts/plot.py output/o* -t
