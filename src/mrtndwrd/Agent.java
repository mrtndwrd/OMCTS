package mrtndwrd;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import core.competition.CompetitionParameters;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

	public static int ROLLOUT_DEPTH = 8;

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
	public static double ALPHA = .3;

	/** The set of all options that are currently available */
	public ArrayList<Option> possibleOptions = new ArrayList<Option>();

	/** A set containing which obsId's already have options in this agent */
	public HashSet<Integer> optionObsIDs = new HashSet<Integer>();

	/** A set containing which itypes alreade have options in this agent */
	public HashSet<Integer> optionItypes = new HashSet<Integer>();

	/** Denominator of the ranking (lower part of fraction) */
	public static DefaultHashMap<String, Double> optionRankingD;
	/** Ranking of an option */
	public static DefaultHashMap<String, Double> optionRanking;
	/** Variance of the ranking of an option */
	public static DefaultHashMap<String, Double> optionRankingVariance;

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

		// Create actions for rollout
		actions = new Types.ACTIONS[act.size()];
		for(int i = 0; i < actions.length; ++i)
		{
			actions[i] = act.get(i);
		}

		
		// Add the actions to the option set
		Lib.setOptionsForActions(act, this.possibleOptions);
		Lib.updateOptions(so, this.possibleOptions, this.optionObsIDs, 
				this.optionItypes);

		// Load old optionRanking (if possible)
		if(readOptionRanking())
		{
			System.out.println("Loaded option ranking D:");
			System.out.println(this.optionRankingD);
			System.out.println("Loaded option ranking:");
			System.out.println(this.optionRanking);
			System.out.println("Loaded option ranking Variance:");
			System.out.println(this.optionRankingVariance);
		}
		else
		{
			optionRankingVariance = new DefaultHashMap<String, Double>(1000.);
			optionRankingD = new DefaultHashMap<String, Double>(0.);
			optionRanking = new DefaultHashMap<String, Double>(0.);
		}

		//Create the player.
		mctsPlayer = new SingleMCTSPlayer(random);


		// set orientation:
		setAvatarOrientation(so);

		while(elapsedTimer.remainingTimeMillis() > CompetitionParameters.ACTION_TIME)
		{
			ElapsedCpuTimer elapsedTimerTemp = new ElapsedCpuTimer();
			elapsedTimerTemp.setMaxTimeMillis(CompetitionParameters.ACTION_TIME);
			// Startup the optionRanking
			// Set the state observation object as the root of the tree.
			mctsPlayer.init(so, this.possibleOptions, this.optionObsIDs, this.currentOption);
			mctsPlayer.run(elapsedTimerTemp);
		}
	}

	private void setAvatarOrientation(StateObservation so)
	{
		if(so.getAvatarOrientation().x == 0. && 
				so.getAvatarOrientation().y == 0.)
			Agent.avatarOrientation = Lib.spriteFromAvatarOrientation(so);
		else
			Agent.avatarOrientation = so.getAvatarOrientation();
	}

	/** Loads filename, filename + Variance and filename + D into
	 * this.optionRankingVariance and this.optionRankingD respectively, afterwards computing optionRanking by
	 * dividing every value in optionRankingVariance with the corresponting value in
	 * optionRankingD
	 */
	private boolean readOptionRanking()
	{
		this.filename = "tables/optionRanking" + Lib.filePostfix;
		// Load objects 
		try
		{
			Object o = Lib.loadObjectFromFile(filename + 'D');
			if(o == null)
				return false;
			this.optionRankingD = (DefaultHashMap<String, Double>) o;
			o = Lib.loadObjectFromFile(filename + "Variance");
			if(o == null)
				return false;
			this.optionRankingVariance = (DefaultHashMap<String, Double>) o;
			o = Lib.loadObjectFromFile(filename);
			if(o == null)
				return false;
			this.optionRanking = (DefaultHashMap<String, Double>) o;
			return true;
		}
		catch(Exception e)
		{
			System.out.println("Couldn't load optionRanking from file");
			e.printStackTrace();
			return false;
		}
	}

	/** write q to file */
	public void writeOptionRanking()
	{
		// System.out.println("Writing hasmap optionRankingD");
		// System.out.println(optionRankingD);
		// System.out.println("Writing hasmap optionRankingVariance");
		// System.out.println(optionRankingVariance);
		System.out.println("Final option ranking: \n" + optionRanking);
		Lib.writeHashMapToFile(this.optionRankingD, filename + "D");
		System.out.println("Final option variance: \n" + optionRankingVariance);
		Lib.writeHashMapToFile(this.optionRankingVariance, filename + "Variance");
		Lib.writeHashMapToFile(this.optionRanking, filename);
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
			//if(currentOption.isFinished(so))
			//	currentOption.updateOptionRanking();
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
		System.out.println("Using option " + currentOption);
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
