#!/bin/bash

# Testing script for mrtndwrd.Agent using parallel to run it with the variables
# set below. To find a description of all variables, run 
# `java -cp classes MyTest -h`

dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $dir/..

# There has to be this directory, and it should be empty on start
# mkdir -p tables/
# rm tables/*

# Variables (command line time?)
#controllers="mrtndwrd.Agent controllers.sampleMCTS.Agent"
controllers="mrtndwrd.Agent"

# CIG 2014 Training Set Games
#games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda"
games="chase"
# CIG 2014 Validation Set Games
#games="camelRace digdug firestorms infection firecaster overload pacman seaquest whackamole eggomania"
#CIG 2015 New Training Set Games
#games="bait boloadventures brainman chipschallenge modality painter realportals realsokoban thecitadel zenpuzzle"
# CIG 2014 TEST SET / GECCO 2015 VALIDATION SET
#games="roguelike surround catapults plants plaqueattack jaws labyrinth boulderchase escape lemmings"

levels="4"
numberOfGames="10"

# Best: 0.5
alpha="0.5"
# Best: 0.9
gamma="0.9"
# Best: false
random_rollout="false"

ant
rm -r output/*

# Run 3 parallel jobs of java until $max jobs are done
parallel -j3 --eta "mkdir -p output/{1}a{5}g{6}r{7}; and java -cp classes MyTest \
		--controller={1} \
		--game={2} \
		--levels={3} \
		--number-of-games={4} \
		--alpha={5} \
		--gamma={6} \
		--random-rollout={7} \
		> output/{1}a{5}g{6}r{7}/o_{2}-{3}" ::: $controllers ::: $games ::: $levels ::: $numberOfGames ::: $alpha ::: $gamma ::: $random_rollout


scripts/get-score-files.sh
