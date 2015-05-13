package mrtndwrd;

import mrtndwrd.*;

import controllers.Heuristics.StateHeuristic;
import controllers.Heuristics.SimpleStateHeuristic;
import core.game.StateObservation;
import core.game.Observation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.lang.Math;

/**
 * User: mrtndwrd
 * Date: 13-01-2015
 * @author Maarten de Waard
 */
public abstract class AbstractAgent extends AbstractPlayer 
{
	protected ArrayList<Option> possibleOptions;
	/** Mapping from State, Option (as index from above options array) to
	 * expected Reward (value), the "Q table" */
	protected DefaultHashMap<SerializableTuple<SimplifiedObservation, Option>, Double> q;

	protected Random random = new Random();

	/** Saves the last non-greedy option timestep */
	protected boolean lastOptionGreedy = false;
	/** The option that is currently being followed */
	protected Option currentOption;
	/** The previous score, assumes scores always start at 0 (so no games should
	 * be present that have a score that decreases over time and starts > 0 or
	 * something) */
	protected double previousScore;
	/** The previous state */
	protected SimplifiedObservation previousState;

	/** Default value for q */
	public final Double DEFAULT_Q_VALUE = 0.0;
	/** Exploration depth for building q and v */
	public final int EXPLORATION_DEPTH = 20;
	/** Epsilon for exploration vs. exploitation */
	public final double EPSILON = .2;
	/** The learning rate of this algorithm */
	public final double ALPHA = .1;
	/** The gamma of this algorithm */
	public final double GAMMA = .9;
	/** Theta for noise */
	public final double THETA = 1e-5;

	/** File to write q table to */
	protected String filename;

	/** Own state heuristic */
	protected StateHeuristic stateHeuristic;

	/** AStar for searching for stuff */
	public static AStar aStar;


	public AbstractAgent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		AbstractAgent.aStar = new AStar(so);
		stateHeuristic = new SimpleStateHeuristic(so);
		possibleOptions = new ArrayList<Option>();
		this.previousScore = score(so);
		// instantiate possibleOptions with actions
		setOptionsForActions(so.getAvailableActions());
		// TODO: More options here!
	}

	/** Scores the state. This enables simple changing of the scoring method
	 * without having to change it everywhere
	 */
	protected double score(StateObservation so)
	{
		return Lib.simpleValue(so);
	}

	/** Explores until almost all time is up */
	abstract public void explore(StateObservation so, ElapsedCpuTimer elapsedTimer, int explorationDepth);

	/** Update an option, chosing and returning a new one if needed */
	abstract protected Option updateOption(Option option, SimplifiedObservation
			newState, SimplifiedObservation oldState, double score, 
			boolean greedy);

	/** Instantiates options array with ActionOptions for all possible actions
	 */
	private void setOptionsForActions(ArrayList<Types.ACTIONS> actions)
	{
		for(Types.ACTIONS action : actions)
		{
			this.possibleOptions.add(new ActionOption(GAMMA, action));
		}
	}

	/** Selects an epsilon greedy value based on the internal q table. Returns
	 * the optimal option as index of the this.actions array.
	 */
	protected Option epsilonGreedyOption(SimplifiedObservation sso, double epsilon)
	{
		// Either way we need to know WHAT the greedy option is, in order to
		// know whether we have taken a greedy option
		Option greedyOption = greedyOption(sso);
		// Select a random option with prob. EPSILON
		if(random.nextDouble() < epsilon)
		{
			// Get random option
			Option option = possibleOptions.get(
				random.nextInt(possibleOptions.size()));
			// Set whether the last option was greedy to true: This is used for
			// learning 
			lastOptionGreedy = option == greedyOption;
			return option.copy();
		}
		// Else, select greedy option:
		lastOptionGreedy = true;
		return greedyOption;
	}

	/** Take the greedy option, based on HashMap q. 
	 * @param sso The simplified state observation
	 * @param print if this is true, the observation and greedy option are
	 * printed 
	 */
	protected Option greedyOption(SimplifiedObservation sso, boolean print)
	{
		double value;
		double maxValue = Lib.HUGE_NEGATIVE;
		Option maxOption = possibleOptions.get(0);
		SerializableTuple<SimplifiedObservation, Option> sop;
		if(print)
			System.out.println(sso);
		// select option with highest value for this sso
		for (Option o : possibleOptions)
		{
			// Create state-option tuple
			sop = new SerializableTuple<SimplifiedObservation, Option>(sso, o);
			// Get the next option value, with a little bit of noise to enable
			// random selection
			value = Utils.noise(q.get(sop), THETA, random.nextDouble());
			if(print)
				System.out.printf("Value %s = %f\n", o, value);
			if(value > maxValue)
			{
				maxValue = value;
				maxOption = o;
			}
		}
		if(print)
			System.out.printf("Option %s with value %f\n\n", maxOption, maxValue);
		// return the optimal option
		return maxOption.copy();
	}

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		double newScore = score(so);
		SimplifiedObservation newState = new SimplifiedObservation(so);
		//double newScore = so.getGameScore();
		explore(so, elapsedTimer, EXPLORATION_DEPTH);
		// update option. This also updates previousState if needed (or at least
		// I hope so)
		SimplifiedObservation oldState = this.previousState;
		// We can only update from step 2
		if(this.previousState == null)
		{
			// First time, initialize previous state to the current state and currentOption to
			// greedy option: From now on, stuff can happen!
			this.previousState = newState;
			currentOption = greedyOption(newState);
		}
		else
		{
			currentOption = updateOption(this.currentOption, newState, this.previousState, newScore - previousScore, true);
		}
		
		this.previousScore = newScore;
		return currentOption.act(so);
	}

	/** Overload for backwards compatibility */
	protected Option greedyOption(SimplifiedObservation sso)
	{
		return greedyOption(sso, false);
	}

	/** write q to file */
	@Override
	public void teardown()
	{
		Lib.writeHashMapToFile(q, filename);
		//System.out.println(q);
		super.teardown();
	}

}
