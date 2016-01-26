TODO 
====

## Aantekeninen diederik
Ik gebruik te veel bijvoegelijke naamwoorden. Elke keer dat er een staat en ik
denk "kan dit weg?" moet het weg.

\epsilon -> \varepsilon

### abstract
eennalaatste regel: "especially those that require high level planning"
herformuleren naar iets dat niet a -> a implies

### Introduction
Meer in de vorm van AI schrijven, niet als "twee research methodes", zinsbouw
net iets anders hiervoor




## Paper:
Elke sectie begint met herhaling van probleem en eindigt met intuitie en vooruit
verwijzen

Meer references: Paper over GVGAI, Andere papers die hebben getest op mijn
testset, Arnoud Visser vragen waar je naar moet refereren voor A*


### Experiments
- Kijken of learning wel werkt met game-specifieke parameters voor 1 game (bijv.
  Zelda)
- Goed onderzoeken waarom learning werkt voor Bait en Surround
- kijken of de complexiteit van de games uit te drukken is op een of andere
  manier
- goed verklaren waarom verschillen zo groot zijn in voordeel van MCTS

### Planning

- DONE starten met: In order to ... we introduce options into mcts
- DONE "An option spans several actions and therefore several nodes in the search
  tree" toevoegen aan praatje over traditional MCTS
- DONE Verwijzingen naar named entities beginnen met een hoofdletter (Figure,
  Algorithm, etc., behalve lines)
- DONE Labels toevoegen aan plaatje O-MCTS
- DONE Verwachting toevoegen dat de options ervoor zorgen dat je sneller dieper
  actions plant. Verwijzen naar experiments sectie die laat zien dat het ook
  echt beter werkt.

### Learning 
- DONE? Wat is de intuitie, waarom kan het evt. werken? Als het lukt ook een intuitie
  geven waarom het niet zou kunnen werken
- Uitleg algoritme
- Aan het einde een intuitie en een vooruitverwijzing

## Other

- run seaquest, whackamole, jaws, plaqueattack with more time (120ms action time?)
- test with *less* time
- run random to find complexity of games

- read [literature/optionsRecentDevelopments.pdf](literature/optionsRecentDevelopments.pdf)
- run test-mean.sh with learning=true
	- should probably improve on (because MCTS is better): whackamole, modality, zenpuzzle, thecitadel,
	  shipschallenge, survivezombies, jaws
	- should improve on (because always loses): lemmings, digdug, portals,
	  brainman, realportals, chase, roguelike, overload, cameRace,
	  oloadventures, boulderdash, plaqueattack, plants, pacman, firecaster,
	  sokoban
- rerun omcts and OLMCTS with WaitAndShoot(0) and uncommented while loop

- Tweak Lib.MAX_OBSERVATIONS

## Categorized list of games
- Puzzle:
	1.
		- sokoban
	2. 
	3. 
		- brainman
		- chipschallenge
		- modality
		- realportals
		- painter
		- realsokoban
		- thecitadel
		- zenpuzzle
	4. 
		- catapults
		- labyrinth
		- escape
		- 
- Random:
	1. 
		- aliens
		- boulderdash
		- butterflies
		- chase
		- frogs
		- missilecommand
		- portals
		- survivezombies
		- zelda
	2. 
		- camelRace
		- digdug
		- firestorms
		- infection
		- firecaster
		- overload
		- pacman
		- seaquest
		- whackamole
		- eggomania
	3. 
		- bait
		- boloadvantures
	4.
		- roguelike
		- surround
		- plants
		- plaqueattack
		- jaws
		- boulderchase
		- lemmings

## Test
- check if using random rollout is better than option-rollout

## Schrijven
- review pseudocode for Algorithm 1

- sectie introduction: 
	- Wat is AI in games?
	- Er bestaat game-specifieke AI, die werkt goed
	- Volgende stap: General AI
		- Als mensen games spelen, maken ze gebruik van hun algemene kennis over
		games.
		- Bijv. GVGAI competitie
	- Meest effectieve algoritmes op dit moment zijn gebasseerd op MCTS, maar doen
	nog weinig met algemene kennis, daarom onze bijdrage.
		- Introductie van options (abstracter denkniveau)
		- Introductie van leren over options
		- Options zijn nog nooit gebruikt in MCTS, die combinatie is een nieuwe
		toevoeging.
- sectie background: 
	- Legt uit wat je moet snappen om mijn onderzoek te snappen (options, MCTS)
- sectie planning 
- sectie learning
- sectie experiments
	- subsectie planning
		- Ook zeggen dat TD (Q-learing) met options niet werkte in deze setting
	- subsectie learning
	- significance: p = 0.032448
- sectie related work:
	- Waarom moeten we niet deep q-learning gebruiken? 
	- Wat is het verschil? Wat is het verschil met TD met options?
- sectie conclusion

Note voor intro: Er is nu een driedeling in ai:

- Specifieke AI (hier zijn we heel goed in)
- Daarna: General AI
	- google deep mind 
	- generalized game competitie
- Uiteindelijk: Strong AI
	- hoeven we het niet over te hebben

Oude notes:

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
