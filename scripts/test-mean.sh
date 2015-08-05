#!/bin/bash
dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $dir/..

# Variables (command line time?)
#controllers="mrtndwrd.Agent controllers.sampleMCTS.Agent"
controllers="mrtndwrd.Agent"

# CIG 2014 Training Set Games
#games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda"
games="butterflies zelda boulderdash chase"
# CIG 2014 Validation Set Games
#games="camelRace digdug firestorms infection firecaster overload pacman seaquest whackamole eggomania"
#CIG 2015 New Training Set Games
#games="bait boloadventures brainman chipschallenge modality painter realportals realsokoban thecitadel zenpuzzle"
# CIG 2014 TEST SET / GECCO 2015 VALIDATION SET
#games="roguelike surround catapults plants plaqueattack jaws labyrinth boulderchase escape lemmings"

levels="0 1 2 3 4"
numberOfGames="3"

#alpha="0 0.2 0.3 0.4 0.5"
alpha="0.5"
#gamma="0 0.5 0.9"
gamma="0.5"

ant
rm -r output/*

# Run 3 parallel jobs of java until $max jobs are done
parallel -j3 --eta "mkdir -p output/{1}a{5}g{6}; and java -cp classes MyTest \
		--controller={1} \
		--game={2} \
		--levels={3} \
		--number-of-games={4} \
		--alpha={5} \
		--gamma={6} \
		> output/{1}a{5}g{6}/o_{2}-{3}" ::: $controllers ::: $games ::: $levels ::: $numberOfGames ::: $alpha ::: $gamma

for i in $(find output/* -name 'o_*')
do
	cat $i | scripts/get-score.sh > ${i}_score
done


