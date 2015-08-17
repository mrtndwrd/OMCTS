package mrtndwrd;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

	public static int ROLLOUT_DEPTH = 7;

	/** Constant C (also known as K) for exploration vs. exploitation */
	public static double K = Math.sqrt(2);

	/** AStar for searching for stuff */
	public static AStar aStar;

	private Random random;

	/** mcts player for the agent. */
	private SingleMCTSPlayer mctsPlayer;
		
	/** list of actions for random action selection in rollout */
	public static Types.ACTIONS[] actions;

	/** The gamma of this algorithm */
	public static double GAMMA = .9;

	/** AMAF alpha for determining how many times we count the optionRanking */
	public static double ALPHA = .3;

	/** The set of all options that are currently available */
	public ArrayList<Option> possibleOptions = new ArrayList<Option>();

	/** A set containing which obsId's already have options in this agent */
	public HashSet<Integer> optionObsIDs = new HashSet<Integer>();

	/** Numerator of the ranking (top part of fraction) */
	public static DefaultHashMap<String, Double> optionRankingN;
	/** Denominator of the ranking (lower part of fraction) */
	public static DefaultHashMap<String, Double> optionRankingD;
	/** Ranking of an option */
	public static DefaultHashMap<String, Double> optionRanking;

	/** which act to call. 1 = MCTS, 2 = random */
	public static final int METHOD = 2;

	/** Currently followed option */
	private Option currentOption;

	/**
	 * Public constructor with state observation and time due.
	 * @param so state observation of the current game.
	 * @param elapsedTimer Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		Agent.aStar = new AStar(so);
		ArrayList<Types.ACTIONS> act = so.getAvailableActions();
		
		// Add the actions to the option set
		Lib.setOptionsForActions(act, this.possibleOptions);
		Lib.setOptions(so, this.possibleOptions, this.optionObsIDs);
		
		// Create actions for rollout
		actions = new Types.ACTIONS[act.size()];
		for(int i = 0; i < actions.length; ++i)
		{
			actions[i] = act.get(i);
		}

		this.random = new Random();

		if(METHOD == 1)
			initMcts(so, elapsedTimer);
		else
			initRandom(so, elapsedTimer);
	}

	public void initMcts(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		optionRankingN = new DefaultHashMap<String, Double>(0.);
		optionRankingD = new DefaultHashMap<String, Double>(0.);
		optionRanking = new DefaultHashMap<String, Double>(0.);

		//Create the player.
		mctsPlayer = new SingleMCTSPlayer(random);

		// Set the state observation object as the root of the tree.
		mctsPlayer.init(so, this.possibleOptions, this.optionObsIDs, this.currentOption);

		// Startup the optionRanking
		mctsPlayer.run(elapsedTimer);
	}

	public void initRandom(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		currentOption = this.possibleOptions.get(
			random.nextInt(this.possibleOptions.size())).copy();
	}

	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		aStar.setLastObservationGrid(stateObs.getObservationGrid());
		if(stateObs.getAvatarPosition().x == -1
				&& stateObs.getAvatarPosition().y == -1)
			// You dead, man!
			return Types.ACTIONS.ACTION_NIL;

		// Update options:
		Lib.setOptions(stateObs, this.possibleOptions, this.optionObsIDs);

		if(METHOD == 1)
			return actMcts(stateObs, elapsedTimer);
		else
			return actRandom(stateObs, elapsedTimer);
	}

	/**
	 * Picks an action using MCTS
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS actMcts(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) 
	{

		if(this.currentOption != null)
			mctsPlayer.init(stateObs, this.possibleOptions, this.optionObsIDs, this.currentOption);
		else
			mctsPlayer.init(stateObs, this.possibleOptions, this.optionObsIDs, null);

		// Determine the action using MCTS...
		int option = mctsPlayer.run(elapsedTimer);

		// return a copy (don't adjust the options in the
		// possibleOption set. This can give trouble later).
		currentOption = this.possibleOptions.get(option).copy();

		Types.ACTIONS action = currentOption.act(stateObs);
		//System.out.println("Tree:\n" + mctsPlayer.printRootNode());
		//System.out.println("Orientation: " + stateObs.getAvatarOrientation());
		//System.out.println("Location: " + stateObs.getAvatarPosition());
		//System.out.println("Action: " + action);
		//System.out.println("Astar:\n" + aStar);
		//System.out.println("Option ranking:\n" + optionRanking);
		//System.out.print("Wall iType scores: "); aStar.printWallITypeScore();
		//System.out.println("Using option " + currentOption);
		//System.out.println("Possible options: " + this.possibleOptions);
		return action;
	}

	public Types.ACTIONS actRandom(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		if(currentOption.isFinished(stateObs))
			currentOption = this.possibleOptions.get(
				random.nextInt(this.possibleOptions.size())).copy();
		// TODO: Learn A* walls
		Types.ACTIONS action = currentOption.act(stateObs);
		//System.out.println("Orientation: " + stateObs.getAvatarOrientation());
		//System.out.println("Location: " + stateObs.getAvatarPosition());
		//System.out.println("Action: " + action);
		//System.out.println("Astar:\n" + aStar);
		//System.out.println("Option ranking:\n" + optionRanking);
		//System.out.print("Wall iType scores: "); aStar.printWallITypeScore();
		System.out.println("Using option " + currentOption);
		//System.out.println("Possible options: " + this.possibleOptions);
		return action;
	}
}
