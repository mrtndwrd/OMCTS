import core.ArcadeMachine;
import core.competition.CompetitionParameters;

import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import mrtndwrd.Lib;
import mrtndwrd.Agent;
import mrtndwrd.SingleTreeNode;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MyTest
{

	// List of all games, used for help
	private static String allGames[] = new String[]{
		//CIG 2014 Training Set Games
		"aliens", "boulderdash", "butterflies", "chase", "frogs",
			"missilecommand", "portals", "sokoban", "survivezombies", "zelda", "prey",
		//CIG 2014 Validation Set Games
		"camelRace", "digdug", "firestorms", "infection", "firecaster",
			"overload", "pacman", "seaquest", "whackamole", "eggomania",
		//CIG 2015 New Training Set Games
		"bait", "boloadventures", "brainman", "chipschallenge",  "modality",
			"painter", "realportals", "realsokoban", "thecitadel", "zenpuzzle",
		//CIG 2014 TEST SET / GECCO 2015 VALIDATION SET
		"roguelike", "surround", "catapults", "plants", "plaqueattack",
			"jaws", "labyrinth", "boulderchase", "escape", "lemmings"};

	public static void main(String[] args)
	{
		//Available games:
		String gamesPath = "examples/gridphysics/";

		// Default controller
		String controller = "controllers.sampleMCTS.Agent";
		// Default game
		String gameName = "prey";
		// Default level(s)
		String[] levels = {"0"};
		// Default number of games to be played:
		int numberOfGames = 20;

		for (String arg : args)
		{
			if(arg.equals("-h") || arg.equals("--help"))
			{
				printHelpMessage();
				System.exit(0);
			}
			// Controller argument: -c or --controller
			else if(arg.startsWith("-c=") || arg.startsWith("--controller="))
			{
				controller = arg.replace("-c=", "")
					.replace("--controller=", "");
				System.out.printf("Controller set to %s\n", controller);
			}
			// Choose game
			else if(arg.startsWith("-g=") || arg.startsWith("--game="))
			{
				gameName = arg.replace("-g=", "")
					.replace("--game=", "");
				System.out.printf("gameName set to %s\n", gameName);
			}
			else if(arg.startsWith("-l=") || arg.startsWith("--levels="))
			{
				arg = arg.replace("-l=", "")
					.replace("--levels=", "");
				levels = arg.split(",");
				System.out.printf("levels set to %s\n", Arrays.asList(levels).toString());
			}
			// Sets the file postfix for my kind of qlearning agent
			else if(arg.startsWith("-p=") || arg.startsWith("--file-postfix="))
			{
				Lib.filePostfix = arg.replace("-p=", "")
					.replace("--file-postfix=", "");
				System.out.printf("file postfix set to %s\n", Lib.filePostfix);
			}
			else if(arg.startsWith("-n=") || arg.startsWith("--number-of-games="))
			{
				numberOfGames = Integer.parseInt(arg.replace("-n=", "")
					.replace("--number-of-games=", ""));
				System.out.printf("Number of games set to %s\n", numberOfGames);
			}
			else if(arg.startsWith("-c=") || arg.startsWith("--gamma="))
			{
				Agent.GAMMA = Double.parseDouble(arg.replace("-c=", "")
					.replace("--gamma=", ""));
				System.out.printf("GAMMA set to %f\n", Agent.GAMMA);
			}
			else if(arg.startsWith("-S=") || arg.startsWith("--steepness="))
			{
				SingleTreeNode.STEEPNESS = Double.parseDouble(arg.replace("-S=", "")
					.replace("--steepness=", ""));
				System.out.printf("STEEPNESS set to %f\n", SingleTreeNode.STEEPNESS);
			}
			else if(arg.startsWith("-d=") || arg.startsWith("--rollout-depth="))
			{
				Agent.ROLLOUT_DEPTH = Integer.parseInt(arg.replace("-d=", "")
					.replace("--rollout-depth=", ""));
				System.out.printf("ROLLOUT_DEPTH set to %d\n", Agent.ROLLOUT_DEPTH);
			}
			else if(arg.startsWith("-r=") || arg.startsWith("--random-rollout="))
			{
				SingleTreeNode.RANDOM_ROLLOUT = Boolean.parseBoolean(arg.replace("-r=", "")
					.replace("--random-rollout=", ""));
				System.out.printf("RANDOM_ROLLOUT set to %B\n", SingleTreeNode.RANDOM_ROLLOUT);
			}
			else if(arg.startsWith("-a=") || arg.startsWith("--learning="))
			{
				Agent.LEARNING = Boolean.parseBoolean(arg.replace("-a=", "")
					.replace("--learning=", ""));
				System.out.printf("LEARNING set to %B\n", Agent.LEARNING);
			}
			else if(arg.startsWith("-i=") || arg.startsWith("--naive="))
			{
				Agent.NAIVE_PLANNING = Boolean.parseBoolean(arg.replace("-i=", "")
					.replace("--naive=", ""));
				System.out.printf("NAIVE_PLANNING set to %B\n", Agent.NAIVE_PLANNING);
			}
			else if(arg.startsWith("-M=") || arg.startsWith("--use-mean-reward="))
			{
				SingleTreeNode.USE_MEAN_REWARD = Boolean.parseBoolean(arg.replace("-M=", "")
					.replace("--use-mean-reward=", ""));
				System.out.printf("USE_MEAN_REWARD set to %B\n", SingleTreeNode.USE_MEAN_REWARD);
			}
			else if(arg.startsWith("-s=") || arg.startsWith("--uct-start-visits="))
			{
				SingleTreeNode.UCT_START_VISITS = Integer.parseInt(arg.replace("-s=", "")
					.replace("--uct-start-visits=", ""));
				System.out.printf("UCT_START_VISITS set to %d\n", SingleTreeNode.UCT_START_VISITS);
			}
			else if(arg.startsWith("-m=") || arg.startsWith("--max-action-time="))
			{
				CompetitionParameters.ACTION_TIME = Integer.parseInt(arg.replace("-m=", "")
					.replace("--max-action-time=", ""));
				CompetitionParameters.ACTION_TIME_DISQ = CompetitionParameters.ACTION_TIME + 100;
				System.out.printf("ACTION TIME set to %d\n", CompetitionParameters.ACTION_TIME);
				System.out.printf("ACTION TIME DISQ set to %d\n", CompetitionParameters.ACTION_TIME_DISQ);
			}
			else
			{
				System.out.printf("Unrecognized argument: %s\n", arg);
				printHelpMessage();
				System.exit(1);
			}
		}

		//CIG 2014 Validation Set Games
		//String games[] = new String[]{"camelRace", "digdug", "firestorms", "infection", "firecaster",
		//		"overload", "pacman", "seaquest", "whackamole", "eggomania"};


		//Other settings
		boolean visuals = true;
		String recordActionsFile = "actions.txt"; //where to record the actions executed. null if not to save.
		int seed = new Random().nextInt();

		//Game and level(s) to play
		String game = gamesPath + gameName + ".txt";
		String[] levelNames = new String[levels.length];
		for(int i=0; i<levels.length; i++)
		{
			levelNames[i] = gamesPath + gameName + "_lvl" + levels[i] +".txt";
		}

		// 1. This starts a game, in a level, played by a human.
		//ArcadeMachine.playOneGame(game, levelNames[0], recordActionsFile, seed);

		// 2. This plays a game in a level by the controller.
		//ArcadeMachine.runOneGame(game, levelNames[0], visuals, sampleMCTSController, recordActionsFile, seed);
		//ArcadeMachine.runOneGame(game, levelNames[0], visuals, controller, recordActionsFile, seed);

		// 3. This replays a game from an action file previously recorded
		//String readActionsFile = "actionsFile_aliens_lvl0.txt";  //This example is for
		//ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

		// 4. This plays a single game, in N levels, M times :
		if(!gameName.equals("all"))
		{
			ArcadeMachine.runGames(game, levelNames, numberOfGames, controller, null);
		}
		//5. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
		else
		{
			int N = 10;
			for(int i = 0; i < N; ++i)
			{
				game = gamesPath + allGames[i] + ".txt";
				// for(int j = 0; j < L; ++j){
				// 	levelNames = gamesPath + games[i] + "_lvl" + j +".txt";
				// }
				ArcadeMachine.runGames(game, levelNames, numberOfGames, controller, null);
			}
		}
	}

	public static void printHelpMessage()
	{
		System.out.println("Testing program for GVG-AI");
		System.out.println("Usage: java -cp classes MyTest [<args>]");
		System.out.println("\t-h\t\t--help\t\tPrints this message");
		System.out.println("\t-c=CONTROLLER\t--controller=CONTROLLER\tSet controller to CONTROLLER. This must be an available package and class extending AbstractPlayer. Default: 'controllers.sampleMCTS.Agent'");
		System.out.println("\t-y=GAMMA\t--gamma=GAMMA\t Gamma for Options");
		System.out.println("\t-l=LEVELS\t--levels=LEVELS\tSet list of levels to LEVEL. This must be an index ranging from 0 to 4, can be comma separated for more values. Default: '0'");
		System.out.println("\t-p=POSTFIX\t--file-postfix=POSTFIX\tAlgorithms made by Maarten de Waard can save or write files. They will have this postfix. Defaults to an empty string");
		System.out.println("\t-n=NUMBER\t--number-of-games=NUMBER\tNumber of games to be run by ArcadeMachine.runGames. Default: 20");
		System.out.println("\t-r=BOOLEAN\t--random-rollout=BOOLEAN\t when this is set to 'true' (-r=true, 1 is seen as false) random rollouts are done in stead of rollouts using the current option");
		System.out.println("\t-g=GAME\t\t--game=GAME\tSet game to GAME. Possible games: " + Arrays.toString(allGames) + " or 'all' for all games. Default: 'prey'");
		System.out.println("\t-d=ROLLOUT_DEPTH\t\t--rollout-depth=ROLLOUT_DEPTH\tSet the maximum depth for MCTS rollout this number.");
		System.out.println("\t-s=UCT_START_VISITS\t\t--uct-start-visits=UCT_START_VISITS\tAfter a node is visited this many times, UCT is used in stead of the crazyStone algorithm");
		System.out.println("\t-S=STEEPNESS\t\t--steepness=STEEPNESS\tSteepness variable for the crazyStone algorithm. Higher = less exploration");
		System.out.println("\t-a=LEARNING\t\t--learning=LEARNING\tIf this is true, the agent will keep optionRankings for several games. If it's false, option rankings are reset every game");
		System.out.println("\t-i=NAIVE\t\t--naive=NAIVE\tTurn on or off naive planning");
		System.out.println("\t-m=ACTION_TIME\t\t--max-action-time=ACTION_TIME\tSet the ACTION_TIME time to this number. ACTION_TIME_DISQ will be set to this + 100ms");
		System.out.println("\t-M=USE_MEAN_REWARD\t\t--use-mean-reward=USE_MEAN_REWARD\tIf this is true, the mean reward of an option is used, if false, the mean return of an option's node is used");

	}
}

