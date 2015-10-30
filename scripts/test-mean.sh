#!/bin/bash

# Testing script for mrtndwrd.Agent using parallel to run it with the variables
# set below. To find a description of all variables, run 
# `java -cp classes MyTest -h`

dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $dir/..

# There has to be this directory, and it should be empty on start
mkdir -p tables/
rm tables/*

# Variables (command line time?)
#controllers="mrtndwrd.Agent controllers.sampleMCTS.Agent"
controllers="mrtndwrd.Agent"

# CIG 2014 Training Set Games
#games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda"
#games="zelda"
# CIG 2014 Validation Set Games
games="camelRace digdug firestorms infection firecaster overload pacman seaquest whackamole eggomania"
#CIG 2015 New Training Set Games
#games="bait boloadventures brainman chipschallenge modality painter realportals realsokoban thecitadel zenpuzzle"
# CIG 2014 TEST SET / GECCO 2015 VALIDATION SET
#games="roguelike surround catapults plants plaqueattack jaws labyrinth boulderchase escape lemmings"

# All games!
# games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda camelRace digdug firestorms infection firecaster overload pacman seaquest whackamole eggomania bait boloadventures brainman chipschallenge modality painter realportals realsokoban thecitadel zenpuzzle roguelike surround catapults plants plaqueattack jaws labyrinth boulderchase escape lemmings"


levels="4"
numberOfGames="20"

# Best: 0.9
gamma="0.9"
# Best: false
random_rollout="false"

#Current best: m=80: d=40, s=30

#rollout_depth="5 15 30"
rollout_depth="30 40"
#TODO: Test-scenario where rollout_depth = 30, uct_start_visits = 40

#uct_start_visits="5 15 30"
uct_start_visits="30 40"

learning="true false"

max_action_time="40"

ant
rm -r output/*

# Run 3 parallel jobs of java until $max jobs are done
parallel -j3 --eta "mkdir -p output/{1}g{5}r{6}d{7}s{8}a{9}m{10}; and java -cp classes MyTest \
		--controller={1} \
		--game={2} \
		--levels={3} \
		--number-of-games={4} \
		--gamma={5} \
		--random-rollout={6} \
		--rollout-depth={7} \
		--uct-start-visits={8} \
		--learning={9} \
		--max-action-time={10} \
		--file-postfix={1}g{5}r{6}d{7}s{8}a{9}m{10}game{2}level{3} \
		> \
		output/{1}g{5}r{6}d{7}s{8}a{9}m{10}/o_{2}-{3}" ::: $controllers ::: $games ::: $levels ::: $numberOfGames ::: $gamma ::: $random_rollout ::: $rollout_depth ::: $uct_start_visits ::: $learning ::: $max_action_time

scripts/get-score-files.sh
