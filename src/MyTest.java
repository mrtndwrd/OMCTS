import core.ArcadeMachine;

import java.util.Random;
import java.util.Arrays;

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
		String controller = "controllers.sampleMCTS";
		// Default game
		String gameName = "prey";
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
			}
			// Choose game
			else if(arg.startsWith("-g=") || arg.startsWith("--game="))
			{
				gameName = arg.replace("-g=", "")
					.replace("--game=", "");
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

		//Game and level to play
		String game = gamesPath + "prey" + ".txt";
		int levelIdx = 1; //level names from 0 to 4 (game_lvlN.txt).
		String level1 = gamesPath + gameName + "_lvl" + levelIdx +".txt";

		// 1. This starts a game, in a level, played by a human.
		//ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);

		// 2. This plays a game in a level by the controller.
		//ArcadeMachine.runOneGame(game, level1, visuals, sampleMCTSController, recordActionsFile, seed);
		//ArcadeMachine.runOneGame(game, level1, visuals, controller, recordActionsFile, seed);

		// 3. This replays a game from an action file previously recorded
		//String readActionsFile = "actionsFile_aliens_lvl0.txt";  //This example is for
		//ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

		// 4. This plays a single game, in N levels, M times :
		String level2 = gamesPath + gameName + "_lvl" + 0 +".txt";
		int M = 40;
		ArcadeMachine.runGames(game, new String[]{level1}, M, controller, null);

		//5. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
		/*int N = 10, L = 5, M = 2;
		boolean saveActions = false;
		String[] levels = new String[L];
		String[] actionFiles = new String[L*M];
		for(int i = 0; i < N; ++i)
		{
			int actionIdx = 0;
			game = gamesPath + games[i] + ".txt";
			for(int j = 0; j < L; ++j){
				levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
				if(saveActions) for(int k = 0; k < M; ++k)
					actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
			}
			ArcadeMachine.runGames(game, levels, M, sampleMCTSController, saveActions? actionFiles:null);
		}*/
	}

	public static void printHelpMessage()
	{
		System.out.println("Testing program for GVG-AI");
		System.out.println("Usage: java -cp classes MyTest [<args>]");
		System.out.println("\t-h\t--help\t\tPrints this message");
		System.out.println("\t-c=CONTROLLER\t--controller=CONTROLLER\tSet controller to CONTROLLER. This must be an available package and class extending AbstractPlayer");
		System.out.println("\t-g=GAME\t--game=GAME\tSet game to GAME. Possible games: " + Arrays.toString(games));
	}
}

