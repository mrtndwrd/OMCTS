#!/bin/bash

# Testing script for mrtndwrd.Agent using parallel to run it with the variables
# set below. To find a description of all variables, run 
# `java -cp classes MyTest -h`

dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $dir/..

# This directory has to be present, and it should be empty on start
mkdir -p tables/
rm tables/*

# CIG 2014 Training Set Games
#games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda"
# CIG 2014 Validation Set Games
#games="camelRace digdug firestorms infection firecaster overload pacman seaquest whackamole eggomania"
# CIG 2015 New Training Set Games
#games="bait boloadventures brainman chipschallenge modality painter realportals realsokoban thecitadel zenpuzzle"
# CIG 2014 TEST SET / GECCO 2015 VALIDATION SET
#games="roguelike surround catapults plants plaqueattack jaws labyrinth boulderchase escape lemmings"

#probably good games:
#games="whackamole modality zenpuzzle thecitadel chipschallenge survivezombies jaws"

# All games!
games="aliens boulderdash butterflies chase frogs missilecommand portals sokoban survivezombies zelda camelRace digdug firestorms infection firecaster overload pacman seaquest whackamole eggomania bait boloadventures brainman chipschallenge modality painter realportals realsokoban thecitadel zenpuzzle roguelike surround catapults plants plaqueattack jaws labyrinth boulderchase escape lemmings"

# Config variables:
controllers="mrtndwrd.Agent"
#controllers="controllers.sampleMCTS.Agent"
levels="0"
number_of_games="5"
number_of_tests="1"
gamma="0.9"
random_rollout="false"
#Current best: m=80: d=40, s=30
rollout_depth="70"
uct_start_visits="40"
learning="true false"
#naive="true false"
naive="false"
steepness="1.5"
max_action_time="40"
use_mean_reward="true"

ant
rm -r output/*
game_numbers=`seq $number_of_tests`

# Run 3 parallel jobs of java until $max jobs are done
parallel -j1 --eta "mkdir -p output/{1}g{5}r{6}d{7}s{8}a{9}m{10}i{11}S{12}M{14}; and java -cp classes MyTest \
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
		--naive={11} \
		--steepness={12} \
		--use-mean-reward={14} \
		--file-postfix={1}g{5}r{6}d{7}s{8}a{9}m{10}i{11}S{12}M{14}game{2}level{3}_{13} \
		> \
		output/{1}g{5}r{6}d{7}s{8}a{9}m{10}i{11}S{12}M{14}/o_{2}-{3}_{13}" ::: $controllers ::: $games ::: $levels ::: $number_of_games ::: $gamma ::: $random_rollout ::: $rollout_depth ::: $uct_start_visits ::: $learning ::: $max_action_time ::: $naive ::: $steepness ::: $game_numbers ::: $use_mean_reward

scripts/get-score-files.sh
