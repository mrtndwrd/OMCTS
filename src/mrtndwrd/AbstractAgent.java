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
	/** The previous score */
	protected double previousScore = 0;

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
	public final double THETA = 1e-6;

	/** File to write q table to */
	protected String filename;

	/** Own state heuristic */
	protected StateHeuristic stateHeuristic;

	/** AStar for searching for stuff */
	protected AStar aStar;

	public AbstractAgent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		aStar = new AStar(so);
		stateHeuristic = new SimpleStateHeuristic(so);
		possibleOptions = new ArrayList<Option>();
		// instantiate possibleOptions with actions
		setOptionsForActions(so.getAvailableActions());
		// TODO: More options here!
	}

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
	protected Option epsilonGreedyOption(StateObservation so, double epsilon)
	{
		// Either way we need to know WHAT the greedy option is, in order to
		// know whether we have taken a greedy option
		Option greedyOption = greedyOption(so);
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
	 * @param so The state observation
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
			value = q.get(sop);
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


	/** Overload for backwards compatibility */
	protected Option greedyOption(SimplifiedObservation sso)
	{
		return greedyOption(sso, false);
	}

}
