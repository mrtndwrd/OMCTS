#!/bin/bash
MAX=$1
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
date=`date +%Y-%m-%d_%H-%M`
# Get into `root` directory
cd $DIR
cd ..
# Create and empty (if needed) output dir
mkdir -p output
rm output/*
ant
for i in $(eval echo {1..$MAX})
do
	# movet test file to this iteration's file, so I can check how bad it works
	# afterwards
	mv testq oldTables/testq$date$i
	mv testd oldTables/testd$date$i
	mv testn oldTables/testn$date$i
	mv test oldTables/test$date$i
	echo "Running test" $i "on" `date +%H:%M:%S`
	# Run test and remove the q-table data
	java -cp classes Test > output/complete_output_$i
	cat output/complete_output_$i | scripts/getScore.sh > output/o$i
done
python scripts/plot.py output/o* -ws