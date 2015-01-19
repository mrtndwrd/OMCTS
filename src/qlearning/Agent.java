package qlearning;

import mrtndwrd.*;

import controllers.Heuristics.StateHeuristic;
import controllers.Heuristics.WinScoreHeuristic;
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

/**
 * User: mrtndwrd
 * Date: 13-01-2015
 * @author Maarten de Waard
 */
public class Agent extends AbstractPlayer 
{
	// A static array with all possible actions of the current MDP
	public static Types.ACTIONS[] actions;
	// Mapping from state to value, the "Value Table"
	private DefaultHashMap<SimplifiedObservation, Double> v;
	// Mapping from State, Action (as index from above actions array) to
	// expected Reward (value), the "Q table"
	private DefaultHashMap<Tuple<SimplifiedObservation, Integer>, Double> q;
	// Rng
	private Random random = new Random();

	// Default value for v
	private final Double DEFAULT_V_VALUE = 0.0;
	// Default value for q
	private final Double DEFAULT_Q_VALUE = 0.0;
	// Exploration depth for building q and v
	private final int EXPLORATION_DEPTH = 100;
	// Epsilon for exploration vs. exploitation
	private final double EPSILON = .5;
	// Gamma for bellman equation
	private final double GAMMA = .9;


	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		//Get the actions in a static array.
		ArrayList<Types.ACTIONS> act = so.getAvailableActions();
		actions = new Types.ACTIONS[act.size()];
		for(int i = 0; i < actions.length; ++i)
		{
			actions[i] = act.get(i);
		}
		// Initialize V and Q:
		v = new DefaultHashMap<SimplifiedObservation, Double>(DEFAULT_V_VALUE);
		q = new DefaultHashMap<Tuple<SimplifiedObservation, Integer>, Double>
			(DEFAULT_Q_VALUE);
		explore(so, elapsedTimer);
		System.out.printf("End of constructor, miliseconds remaining: %d\n", elapsedTimer.remainingTimeMillis());
	}

	/** 
	 * Explore using the state observation and enter values in the V and Q
	 * tables 
	 */
	public void explore(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		int depth;
		StateObservation soCopy;

		while(elapsedTimer.remainingTimeMillis() > 10.)
		{
			soCopy = so.copy();
			// create histories of actions and states
			int[] actionHistory = new int[EXPLORATION_DEPTH];
			SimplifiedObservation[] stateHistory = new SimplifiedObservation[EXPLORATION_DEPTH];
			for(depth=0; depth<EXPLORATION_DEPTH; depth++)
			{
				// Get a new state and the action that leads to it in an
				// epsilon-greedy manner
				// This advances soCopy with the taken action
				int a = epsilonGreedy(soCopy);
				// Advance the state, this should advance everywhere, with pointers and
				// stuff
				SimplifiedObservation s = new SimplifiedObservation(soCopy);
				soCopy.advance(actions[a]);
				// add state-action pair to history arrays
				stateHistory[depth] = s;
				actionHistory[depth] = a;
			}
			// process the states and actions from this rollout, using the value
			// of the last visited state
			backUp(stateHistory, actionHistory, Lib.simpleValue(soCopy));
		}
	}

	/** Selects an epsilon greedy value based on the internal q table. Returns
	 * the optimal action
	 * TODO: CUrrently this.EPSILON is ignored (or assumed 0)
	 */
	private int epsilonGreedy(StateObservation so)
	{
		double value;
		double maxValue = Lib.HUGE_NEGATIVE;
		int maxAction = 0;
		// select action with highest value for this sso
		for (int action=0; action < actions.length; action++)
		{
			// Get the next action value, with a little bit of noise to enable
			// random selection
			value = Utils.noise(q.get(action), EPSILON, random.nextDouble());
			if(value > maxValue)
			{
				maxValue = value;
				maxAction = action;
			}
			System.out.printf("Max value: %f\n", value);
		}
		// return the optimal action
		return maxAction;
	}

	private void backUp(SimplifiedObservation[] stateHistory, int[] actionHistory, double score)
	{
		Tuple<SimplifiedObservation, Integer> sa;
		for(int i=EXPLORATION_DEPTH-1; i>-1; i--)
		{
			score *= GAMMA;
			sa = new Tuple(stateHistory[i], actionHistory[i]);
			q.put(sa, score);
		}
	}

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		//Lib.printObservationGrid(so.getObservationGrid());
		// Create simplified observation:
		int action = epsilonGreedy(so);
		return actions[action];
	}
}
