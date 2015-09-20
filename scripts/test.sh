#!/bin/bash
if [ $# -ne 2 ]
then
	echo "Usage: test2.sh MAX GAMES"
	echo "	MAX: the number of tests"
	echo "	GAMES: The number of games that will be played in each test"
	exit 1
fi

# Set number of tests
let max=$1
# Set the number of games to the second argument
let games=$2

dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $dir/..

# Variables (command line time?)
controller="mrtndwrd.Agent"
# controller="controllers.sampleMCTS.Agent"
# CIG 2014 Training Set Games
# "aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda"
game="missilecommand"
# CIG 2014 Validation Set Games
#game="camelRace digdug firestorms infection firecaster overload pacman seaquest whackamole eggomania"
#CIG 2015 New Training Set Games
#game="bait boloadventures brainman chipschallenge modality painter realportals realsokoban thecitadel zenpuzzle"
# CIG 2014 TEST SET / GECCO 2015 VALIDATION SET
#game="roguelike surround catapults plants plaqueattack jaws labyrinth boulderchase escape lemmings"
levels="3"

ant
rm -r output/*
rm -r tables/*

# Run 3 parallel jobs of java until $max jobs are done
seq $max | parallel -j3 --eta "java -cp classes MyTest \
		--controller=$controller \
		--game=$game \
		--levels=$levels \
		--number-of-games=$games \
		--file-postfix={#} \
		> output/complete_output_{#}"
# Extract the score from the outputs
for i in $(eval echo {1..$max})
do
	cat output/complete_output_$i | scripts/get-score.sh > output/o$i
done
# Make the plots
python scripts/plot.py output/o* -ws -o winScore.pdf
python scripts/plot.py output/o* -t -o time.pdf
