import core.ArcadeMachine;

import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import mrtndwrd.Lib;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MyTest
{

	//CIG 2014 Training Set Games
	private static String games[] = new String[]{"aliens", "boulderdash", "butterflies", "chase", "frogs",
			"missilecommand", "portals", "sokoban", "survivezombies", "zelda", "prey"};
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
				game = gamesPath + games[i] + ".txt";
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
		System.out.println("\t-h\t--help\t\tPrints this message");
		System.out.println("\t-c=CONTROLLER\t--controller=CONTROLLER\tSet controller to CONTROLLER. This must be an available package and class extending AbstractPlayer. Default: 'controllers.sampleMCTS.Agent'");
		System.out.println("\t-g=GAME\t--game=GAME\tSet game to GAME. Possible games: " + Arrays.toString(games) + " or 'all' for all games. Default: 'prey'");
		System.out.println("\t-l=LEVELS\t--levels=LEVELS\tSet list of levels to LEVEL. This must be an index ranging from 0 to 4, can be comma separated for more values. Default: '0'");
		System.out.println("\t-p=POSTFIX\t--file-postfix=POSTFIX\tAlgorithms made by Maarten de Waard can save or write files. They will have this postfix. Defaults to an empty string");
		System.out.println("\t-n=NUMBER\t--number-of-games=NUMBER\tNumber of games to be run by ArcadeMachine.runGames. Defaunt: 20");
	}
}

