#!/bin/bash
# Remove disqualifications, since they are created by a bug in the framework
# grep -v 'Result (1->win; 0->lose):-100, Score:-1000.0, timesteps:' | \
# Get all the rules with scores in them
grep 'Result (1->win; 0->lose)' | \
	# Extract only the scores
	sed -r 's/\s*[][a-zA-Z]*\s*Result \(1->win; 0->lose\):([-]*[0-9]+), Score:([-]*[0-9]+.[0-9]+), timesteps:([-]*[0-9]+)/\1 \2 \3/g'
