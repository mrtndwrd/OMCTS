Thesis Defense - Feb 22$^{nd}$

* Background
	* General Video Game Playing
	* Markov Decision Processes
	* Monte Carlo Tree Search
	* Options
	* SMDP Q-learning
* Methods
	* General Video Game Playing
		* MY game prey
	* Option MCTS
	* Option Learning MCTS
* Demo
	* Explain game camel race first
	* DO NOT CLOSE WINDOW
	* Explain difference before opening O-MCTS game
* Experiments
	* Ordered by complexity, based on random algorithm
	* SMDP Q-learning
		* Did learn on prey
		* SMDP Q-learning learns over several games, here we see:
			* Q-learning1: pink
			* Q-learning4: blue
		* Outperforms MCTS only in CamelRace and Overload
		* More learning leads to disqualifications
	* OMCTS
		* O-MCTS outperforms MCTS in 10 games:
			* missile command
			* bait
			* camel race
			* survive zombies
			* firestorms
			* lemmings
			* fire caster
			* overload
			* zelda
			* chase
	* OL-MCTS
		* Plotted like SMDP Q-learning, learns over 5 games
			* O-MCTS: Blue
			* OL-MCTS1: Red
			* OL-MCTS5: Yellow
		* Outperforms MCTS in bait (and prey)
		* Performs comparable in other games
		* Performance drops on first learning game
* Conclusion
	* O-MCTS
		* Outperforms SMDP Q-learning in EVERY GAME
		* excels in games with small level grid, low number of sprites and high complexity
		* performs below expectation on games with many sprites or a big level grid
	* OL-MCTS
		* Does learn on Prey and Bait
		* Indicates improvements, but needs more work
