#!/bin/bash
dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $dir/..

# Variables (command line time?)
controllers="mrtndwrd.Agent controllers.sampleMCTS.Agent"
# games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda prey"
games="aliens boulderdash"
levels="1 2"
numberOfGames="1"

ant
rm -r output_*

# Run 3 parallel jobs of java until $max jobs are done
parallel -j3 --eta "mkdir -p output_{1}; and java -cp classes MyTest \
		--controller={1} \
		--game={2} \
		--levels={3} \
		--number-of-games={4} \
		> output_{1}/o_{2}-{3}" ::: $controllers ::: $games ::: $levels ::: $numberOfGames

for i in $(find output* -name 'o_*')
do
	cat $i | scripts/getScore.sh > ${i}_score
done


