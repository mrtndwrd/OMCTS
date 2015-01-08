- Lezen: Hoofdstuk
  4, 5, 6, 17
  uit *Reinforcement
  Learning, State of the Art* (PDF van Diederik)
  (4 en 5 gelezen)

- Uitzoeken: 
	- **Hoe zit de competitie precies in elkaar? Hoe werkt de rating?**

		Er is een trainingset en een validatieset met verschillende games bekend. De
		trainingset kun je krijgen, je resultaten op de validatieset kun je op de
		server zien. De uiteindelijke competitie wordt gedraaid op een testset
		van 10 nieuwe games, ieder bestaande uit 5 verschillende levels. Iedere
		agent heeft een bepaalde hoeveelheid denktijd per cycle, als die wordt
		overschreden word je gediskwalificeerd 

		Rating: 
		- Wins, score en tijd (in game cycles) worden opgeteld van 5 levels per
		  game. Diskwalificaties tellen als -1000 score en 0 voor wins

		Rankings worden gedaan op volgorde van wins, daarna score, daarna tijd.
		Elke entry krijgt punten volgens zijn rank, volgens het F1 scoresysteem.
		(25, 18, 15, 12, 10, 8, 6, 4, 2 and 1; van eerste tot de tiende rank.
		Alles daarna heeft 0 punten.)

	- **Wat is er gedaan op dit gebied? Wat is er al afgetast, en waar valt winst
	  te behalen?**

		Hoofdstuk 5 van *reinforcement learning, state of the art* omschrijft
		transfer, een mogelijkheid om geleerde data over te geven naar MDP's
		met dezelfde state-action set, of andersoortige overeenkomsten
	- **Welke games worden (werden?) er geboden? *Is het leren, of plannen?***

		De testset van 2014 staat hier omschreven:
		http://gvgai.net/evaluation_games.php
		Dit zijn zo'n beetje allemaal 'leren' games en niet 'planning' (puzzel)
		games. 
	- **Weet je altijd wat de goal-state is?**

		Nee. Volgens mij weet je dat in principe nooit...
