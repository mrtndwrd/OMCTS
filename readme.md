Monte Carlo Tree Search with Options for General Video Game Playing	
===================================================================

The Monte Carlo tree search algorithm always plans over actions and does not
incorporate any high level planning, as one would expect from a human game
player. In this thesis, we introduce a new algorithm called "Option Monte Carlo
Tree Search". It offers general video game knowledge and high level planning in
the form of options, which are action sequences aimed at achieving a specific
subgoal. Additionally, we introduce "Option Learning MCTS", which applies a
progressive widening technique to the expected returns of options in order to
focus exploration on fruitful parts of the search tree.

## Dependencies

- scripts/test-mean.sh 
	- parallel
- scripts/\*.py
	- numpy
	- matplotlib
	- argparse
- src/
	- *oracle* java 7
