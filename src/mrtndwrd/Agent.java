package mrtndwrd;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Map;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

	public static int ROLLOUT_DEPTH = 10;

	/** Constant C (also known as K) for exploration vs. exploitation */
	public static double K = Math.sqrt(2);

	/** AStar for searching for stuff */
	public static AStar aStar;

	/** orientation */
	public static Vector2d avatarOrientation;

	/** Random generator for the agent. */
	private SingleMCTSPlayer mctsPlayer;
		
	/** list of actions for random action selection in rollout */
	public static Types.ACTIONS[] actions;

	/** The gamma of this algorithm */
	public static double GAMMA = .9;

	/** (start of) Filename for optionRanking tables when they are saved */
	private String filename = "tables/optionRanking";

	/** AMAF alpha for determining how many times we count the optionRanking */
	public static double ALPHA = .5;

	/** The set of all options that are currently available */
	public ArrayList<Option> possibleOptions = new ArrayList<Option>();

	/** A set containing which obsId's already have options in this agent */
	public HashSet<Integer> optionObsIDs = new HashSet<Integer>();

	/** A set containing which itypes alreade have options in this agent */
	public HashSet<Integer> optionItypes = new HashSet<Integer>();

	/** Numerator of the ranking (top part of fraction) */
	public static DefaultHashMap<String, Double> optionRankingN;
	/** Denominator of the ranking (lower part of fraction) */
	public static DefaultHashMap<String, Double> optionRankingD;
	/** Ranking of an option */
	public static DefaultHashMap<String, Double> optionRanking;

	/** Currently followed option */
	private Option currentOption;

	public static Random random;

	/**
	 * Public constructor with state observation and time due.
	 * @param so state observation of the current game.
	 * @param elapsedTimer Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		Agent.random = new Random();
		Agent.aStar = new AStar(so);
		ArrayList<Types.ACTIONS> act = so.getAvailableActions();
		
		// Add the actions to the option set
		Lib.setOptionsForActions(act, this.possibleOptions);
		Lib.setOptions(so, this.possibleOptions, this.optionObsIDs);
		if(act.contains(Types.ACTIONS.ACTION_USE))
		{
			Lib.setWaitAndShootOptions(so, this.possibleOptions, 1);
			Lib.setWaitAndShootOptions(so, this.possibleOptions, 2);
			Lib.setWaitAndShootOptions(so, this.possibleOptions, 3);
			Lib.setWaitAndShootOptions(so, this.possibleOptions, 4);
			Lib.setWaitAndShootOptions(so, this.possibleOptions, 5);
		}
		Lib.setGoToNearestOptions(so, possibleOptions, optionItypes);

		// Add avoidance-option
		possibleOptions.add(new AvoidNearestNpcOption(Agent.GAMMA));
		
		// Create actions for rollout
		actions = new Types.ACTIONS[act.size()];
		for(int i = 0; i < actions.length; ++i)
		{
			actions[i] = act.get(i);
		}
		optionRankingN = new DefaultHashMap<String, Double>(0.);
		optionRankingD = new DefaultHashMap<String, Double>(0.);
		optionRanking = new DefaultHashMap<String, Double>(0.);

		//Create the player.
		mctsPlayer = new SingleMCTSPlayer(random);

		// Set the state observation object as the root of the tree.
		mctsPlayer.init(so, this.possibleOptions, this.optionObsIDs, this.currentOption);

		// Load old optionRanking (if possible)
		readOptionRanking();
		System.out.println("Loaded option ranking: ");
		System.out.println(this.optionRanking);
		System.out.println("Loaded option ranking N: ");
		System.out.println(this.optionRankingN);
		System.out.println("Loaded option ranking D: ");
		System.out.println(this.optionRankingD);

		// set orientation:
		setAvatarOrientation(so);

		// Startup the optionRanking
		mctsPlayer.run(elapsedTimer);
	}

	private void setAvatarOrientation(StateObservation so)
	{
		if(so.getAvatarOrientation().x == 0. && 
				so.getAvatarOrientation().y == 0.)
			Agent.avatarOrientation = Lib.spriteFromAvatarOrientation(so);
		else
			Agent.avatarOrientation = so.getAvatarOrientation();
	}

	/** Loads filename + N and filename + D into this.optionRankingN and
	 * this.optionRankingD respectively, afterwards computing optionRanking by
	 * dividing every value in optionRankingN with the corresponting value in
	 * optionRankingD
	 */
	private void readOptionRanking()
	{
		this.filename = "tables/optionRanking" + Lib.filePostfix;
		// Load objects 
		try
		{
			Object o = Lib.loadObjectFromFile(filename + 'N');
			if(o == null)
				return;
			this.optionRankingN = (DefaultHashMap<String, Double>) o;
			o = Lib.loadObjectFromFile(filename + 'D');
			if(o == null)
				return;
			this.optionRankingD = (DefaultHashMap<String, Double>) o;
			String key;
			for(Map.Entry<String, Double> ranking : optionRankingN.entrySet())
			{
				key = ranking.getKey();
				this.optionRanking.put(key, ranking.getValue() / optionRankingD.get(key));
			}
		}
		catch(Exception e)
		{
			System.out.println("Couldn't load optionRanking from file");
			e.printStackTrace();
		}
	}

	/** write q to file */
	public void writeOptionRanking()
	{
		// System.out.println("Writing hasmap optionRankingD");
		// System.out.println(optionRankingD);
		// System.out.println("Writing hasmap optionRankingN");
		// System.out.println(optionRankingN);
		System.out.println("Final option ranking: " + optionRanking);
		Lib.writeHashMapToFile(this.optionRankingD, filename + "D");
		Lib.writeHashMapToFile(this.optionRankingN, filename + "N");
	}


	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * @param so Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		setAvatarOrientation(so);
		aStar.setLastObservationGrid(so.getObservationGrid());
		if(so.getAvatarPosition().x == -1
				&& so.getAvatarPosition().y == -1)
			// You dead, man!
			return Types.ACTIONS.ACTION_NIL;

		// Update options:
		Lib.updateOptions(so, this.possibleOptions, this.optionObsIDs, 
				this.optionItypes);
		if(this.currentOption != null)
		{
			if(currentOption.isFinished(so))
				currentOption.updateOptionRanking();
			mctsPlayer.init(so, this.possibleOptions, this.optionObsIDs, this.currentOption);
		}
		else
			mctsPlayer.init(so, this.possibleOptions, this.optionObsIDs, null);

		// Determine the action using MCTS...
		int option = mctsPlayer.run(elapsedTimer);

		//... and return a copy (don't adjust the options in the
		//possibleOption set. This can give trouble later).
		currentOption = this.possibleOptions.get(option).copy();

		Types.ACTIONS action = currentOption.act(so);
		System.out.println("Tree:\n" + mctsPlayer.printRootNode());
		//System.out.println("Orientation: " + so.getAvatarOrientation());
		//System.out.println("Location: " + so.getAvatarPosition());
		//System.out.println("Action: " + action);
		//System.out.println("Astar:\n" + aStar);
		//System.out.println("Option ranking:\n" + optionRankingD);
		//System.out.print("Wall iType scores: "); aStar.printWallITypeScore();
		//System.out.println("Option itypes: " + optionItypes);
		//System.out.println("Using option " + currentOption);
		//System.out.println("Possible options: " + this.possibleOptions);
		return action;
	}

	/** write optionRanking to file when agent is done*/
	@Override
	public final void teardown()
	{
		writeOptionRanking();
		super.teardown();
	}
}
