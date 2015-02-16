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
	protected ArrayList<Types.ACTIONS> possibleActions;
	/** Mapping from State, Action (as index from above actions array) to
	 * expected Reward (value), the "Q table" */
	protected DefaultHashMap<SerializableTuple<SimplifiedObservation, Types.ACTIONS>, Double> q;

	protected Random random = new Random();
	/** Saves the last non-greedy action timestep */
	protected boolean lastActionGreedy = false;

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
		//Get the actions in a static array.
		possibleActions = so.getAvailableActions();
	}

	/** Selects an epsilon greedy value based on the internal q table. Returns
	 * the optimal action as index of the this.actions array.
	 */
	protected Types.ACTIONS epsilonGreedyAction(StateObservation so, double epsilon)
	{
		// Either way we need to know WHAT the greedy action is, in order to
		// know whether we have taken a greedy action
		Types.ACTIONS greedyAction = greedyAction(so);
		// Select a random action with prob. EPSILON
		if(random.nextDouble() < epsilon)
		{
			// Get random action
			Types.ACTIONS action = possibleActions.get(
				random.nextInt(possibleActions.size()));
			// Set whether the last action was greedy to true: This is used for
			// learning 
			lastActionGreedy = action == greedyAction;
			return action;
		}
		// Else, select greedy action:
		lastActionGreedy = true;
		return greedyAction;
	}

	/** Take the greedy action, based on HashMap q. 
	 * @param so The state observation
	 * @param print if this is true, the observation and greedy action are
	 * printed 
	 */
	protected Types.ACTIONS greedyAction(StateObservation so, boolean print)
	{
		double value;
		double maxValue = Lib.HUGE_NEGATIVE;
		Types.ACTIONS maxAction = possibleActions.get(0);
		SerializableTuple<SimplifiedObservation, Types.ACTIONS> sa;
		SimplifiedObservation sso = new SimplifiedObservation (so, aStar);
		if(print)
			System.out.println(sso);
		// select action with highest value for this sso
		for (Types.ACTIONS a : possibleActions)
		{
			// Create state-action tuple
			sa = new SerializableTuple<SimplifiedObservation, Types.ACTIONS>(sso, a);
			// Get the next action value, with a little bit of noise to enable
			// random selection
			value = Utils.noise(q.get(sa), THETA, random.nextDouble());
			value = q.get(sa);
			if(value > maxValue)
			{
				maxValue = value;
				maxAction = a;
			}
		}
		if(print)
			System.out.printf("Action %s with value %f\n\n", maxAction, maxValue);
		// return the optimal action
		return maxAction;
	}

	/** Overload for backwards compatibility */
	protected Types.ACTIONS greedyAction(StateObservation so)
	{
		return greedyAction(so, false);
	}

}
