#!/bin/bash
grep 'Result (1->win; 0->lose)' | \
	sed -r 's/\s+\[java\] Result \(1->win; 0->lose\):([0-9]+), Score:([0-9]+.[0-9]+), timesteps:([0-9]+)/\1 \2 \3/g'
