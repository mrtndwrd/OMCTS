TODO 
====

- improve USE_MEAN_REWARD = false setting (it works with USE_MEAN_REWARD = true
  and game camelRace)
- Remove currentPath from GoToPositionOption, probably improves in stochastic
  games
- Lees BALO.pdf
- Waarde van een node moet discounted worden, optionRanking gemiddelde
  discounted node waarde van die option
- Softmax opzoeken

## Test
- check if using random rollout is better than option-rollout

## Program:
- option for pushing boxes around (sokoban, other puzzle games) (too difficult?)
- wait-and-shoot option voor bepaalde bounding box
- een actie overslaan voor meer rekentijd

## Schrijven
- Inleiding
	- voorlaatste paragraaf: onderzoeksvragen
	- (laatste: overzicht thesis)
- Related Work
- Part 1: competition version
	- Method
	- Experiments
	- Results
- Part 2: multilevel-memory version
	- Method
	- Experiments
	- Results
- Conclusion
- Future Work

## Lezen: 
Hoofdstuk 4, 5, 9, 17 uit *Reinforcement Learning, State of the Art* (PDF van
Diederik) (4, 5 gelezen)

## Uitzoeken: 

- **Maak overzicht van voor- en nadelen van verschillende mogelijkheden** Als ik
  alle ideeën op een rijtje heb: Zorg ervoor dat je nog kunt overstappen naar
  andere mogelijkheden (generiek programmeren)

- **Hoe leer je de spel-dynamiek?**

- **Hoe kom je aan optie-sets?**

- **Hebben SMDP's speciale oplossingsmethodes?**

- **Wordt de act-functie nog aangeroepen als de game is afgelopen?**
	nee.

- **Nadenken over hoe je om kunt gaan met sprites die niet per se hetzelfde zijn
	in een nieuwe game als in een oude**
	- Geef jezelf een definitie van zelda(?), definieer twee levels, waarin de
		je bijv. een blokje met een monster omwisselt

	Hoe pas je een policy aan in een nieuw level? Wat kun je doen met informatie
	die je al geleerd hebt? 

- **Nadenken over gedragshierarchie leren**

- **Lezen over DYNA en DYNA 2 en Prioritized Sweeping**
	- Prioritized sweeping gelezen

- **Nadenken over mogelijke transfer-implementaties**
	- Nu: simpelste optie, volledig model overzetten

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

	Voor zover ik kan zien in de oude resultaten is transfer nog niet gebruikt.

	Eerste plek vorig jaar Open loop tree search met UCB (incl. "taboo bias"
	tegen kort geleden bezochte posities): 
	In short, an open loop tree search. It builds a tree representing sequences
	of actions. At every iteration, one node/action is added to the tree. When
	navigating in the tree, the forward model is always used to generate states
	(i.e. no state is stored in the tree, which is why I called this "open
	loop"). To balance exploration/exploitation, it uses a classic UCB formula,
	with a small addition: a "taboo bias", that gives penalty to actions that
	lead to avatar positions visited in the recent past.

- **Welke games worden (werden?) er geboden? *Is het leren, of plannen?***

	De testset van 2014 staat hier omschreven:
	http://gvgai.net/evaluation_games.php
	Dit zijn zo'n beetje allemaal 'leren' games en niet 'planning' (puzzel)
	games. 
- **Weet je altijd wat de goal-state is?**

	Nee. Volgens mij weet je dat in principe nooit...

- **Heeft een agent toegang tot de VGDL-descriptie?**

	Nee. Het enige wat je hebt is het "observation grid" Hierin staan op init-time
	de observaties die je aan het begin kunt doen (bijv. het hele level). Aan de
	hand hiervan kun je niet per se bepalen watvoor game je gaat spelen.
