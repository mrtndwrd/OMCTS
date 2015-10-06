Achieving Goals Quickly Using Real-time Search: Experimental Results in Video Games
===================================================================================

[pdf](realTimeSearch.pdf)

## 2. Previous Work

* LRTA\*: Two methods:
	1. If a state has never been visited before: Depth first limited search as
	   heuristic
	2. Else, use some sort of learned estimate as heuristic
* LSS-LRTA\*: Betetr than LRTA in that it has less variance in lookahead times
  and learns heuristics for all states (also unvisited ones). Also performs
  fewer lookaheads, by acting several times after 1 lookahead in stead of once
  per lookahead (as LRTA does)

## 3. Evaluating Real Time Search (RTS) Algorithms
Traditionally, two evaluation criteria: Convergence time, Solution length.
Convergence time is the number of repeated start-to-goal plans. Solution length
is the number of actions needed to achieve the goal.

### Goal Achievement Time
goal achievement time $= time\_{planning} + time\_{executing} + time\_{both}$

sometime distance is multiplied by some time constant, because no time is
available

## Lookahead Commitment

