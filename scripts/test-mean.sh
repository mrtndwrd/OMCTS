#!/bin/bash
dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $dir/..

# Variables (command line time?)
controllers="mrtndwrd.Agent controllers.sampleMCTS.Agent"
games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda"
#games="aliens boulderdash"
levels="0 1 2 3 4"
numberOfGames="4"

ant
rm -r output/*

# Run 3 parallel jobs of java until $max jobs are done
parallel -j3 --eta "mkdir -p output/{1}; and java -cp classes MyTest \
		--controller={1} \
		--game={2} \
		--levels={3} \
		--number-of-games={4} \
		> output/{1}/o_{2}-{3}" ::: $controllers ::: $games ::: $levels ::: $numberOfGames

for i in $(find output/* -name 'o_*')
do
	cat $i | scripts/get-score.sh > ${i}_score
done


