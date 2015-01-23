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
import java.io.FileOutputStream;

/**
 * User: mrtndwrd
 * Date: 13-01-2015
 * @author Maarten de Waard
 */
public class Agent extends AbstractPlayer 
{
	private ArrayList<Types.ACTIONS> possibleActions;
	/** Mapping from state to value, the "Value Table" */
	private DefaultHashMap<SimplifiedObservation, Double> v;
	/** Mapping from State, Action (as index from above actions array) to
	 * expected Reward (value), the "Q table" */
	private DefaultHashMap<SerializableTuple<SimplifiedObservation, Types.ACTIONS>, Double> q;
	private Random random = new Random();

	/** Default value for v */
	private final Double DEFAULT_V_VALUE = 0.0;
	/** Default value for q */
	private final Double DEFAULT_Q_VALUE = 0.0;
	/** Exploration depth for building q and v */
	private final int EXPLORATION_DEPTH = 20;
	/** Epsilon for exploration vs. exploitation */
	private final double EPSILON = .1;
	/** The learning rate of this algorithm */
	private final double ALPHA = .1;
	/** Gamma for bellman equation */
	private final double GAMMA = .9;
	/** Theta for noise */
	private final double THETA = 1e-6;

	/** File to write q table to */
	private String filename;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		this.filename = "test";
		//Get the actions in a static array.
		possibleActions = so.getAvailableActions();
		// Initialize V and Q:
		v = new DefaultHashMap<SimplifiedObservation, Double>(DEFAULT_V_VALUE);
		try
		{
			Object o = Lib.loadObjectFromFile(filename);
			q = (DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Types.ACTIONS>, Double>) o;
			System.out.printf("time remaining: %d\n", 
				elapsedTimer.remainingTimeMillis());
		}
		catch (Exception e)
		{
			System.out.println(
				"probably, it wasn't a hashmap, or the file didn't exist or something. Using empty q table");
		}
		if(q == null)
			q = new DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Types.ACTIONS>, Double> (DEFAULT_Q_VALUE);
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

		// Currently only the greedy action will have to be taken after this is
		// done, so we can take as much time as possible!
		while(elapsedTimer.remainingTimeMillis() > 15.)
		{
			soCopy = so.copy();
			// create histories of actions and states
			Types.ACTIONS[] actionHistory = new Types.ACTIONS[EXPLORATION_DEPTH];
			SimplifiedObservation[] stateHistory = new SimplifiedObservation[EXPLORATION_DEPTH];
			for(depth=0; depth<EXPLORATION_DEPTH && !soCopy.isGameOver(); depth++)
			{
				// Get a new state and the action that leads to it in an
				// epsilon-greedy manner
				// This advances soCopy with the taken action
				Types.ACTIONS a = epsilonGreedyAction(soCopy);
				// Advance the state, this should advance everywhere, with pointers and
				// stuff
				SimplifiedObservation s = new SimplifiedObservation(soCopy);
				soCopy.advance(a);
				// add state-action pair to history arrays
				stateHistory[depth] = s;
				actionHistory[depth] = a;
			}
			// process the states and actions from this rollout, using the value
			// of the last visited state
			backUp(stateHistory, actionHistory, Lib.simpleValue(soCopy), depth);
		}
	}

	/** Update q values using the bellman equation
	 * @param stateHistory The states visited in the last run. stateHistory[0]
	 * is the first state
	 * @param actionHistory The actions taken in each state from stateHistory.
	 * actionHistory[i] is an action taken in stateHistory[i]
	 * @param score The score in the last state in stateHistory. This is used in
	 * combination with this.GAMMA to update all q values
	 * @param lastDepth Some times stateHistory.length is not
	 * this.EXPLORATION_DEPTH, because the game was ended before
	 * this.EXPLORATION_DEPTH was reached. Therefore we need the last depth
	 */
	private void backUp(SimplifiedObservation[] stateHistory, Types.ACTIONS[] actionHistory, double score, int lastDepth)
	{
		// Will be used as index in the q table
		SerializableTuple<SimplifiedObservation, Types.ACTIONS> sa;
		// Will be used for the score that is currently in the q table
		double lastScore;
		double gamma = GAMMA;
		// We can not be entirely sure that the array is full, since we stop
		// exploring when a game is over
		for(int i=lastDepth-1; i>-1 && stateHistory[i] != null; i--)
		{
			sa = new SerializableTuple(stateHistory[i], actionHistory[i]);
			lastScore = q.get(sa);
			//q.put(sa, gamma * lastScore + score);
			q.put(sa, lastScore + ALPHA * (gamma * score - lastScore));
			// reduce gamma, because we're a state further from the score
			gamma *= GAMMA;
		}
	}

	/** Selects an epsilon greedy value based on the internal q table. Returns
	 * the optimal action as index of the this.actions array.
	 */
	private Types.ACTIONS epsilonGreedyAction(StateObservation so)
	{
		// Select a random action with prob. EPSILON
		if(random.nextDouble() < EPSILON)
		{
			return possibleActions.get(random.nextInt(possibleActions.size()));
		}
		// Else, select greedy action:
		return greedyAction(so);
	}

	private Types.ACTIONS greedyAction(StateObservation so)
	{
		double value;
		double maxValue = Lib.HUGE_NEGATIVE;
		Types.ACTIONS maxAction = possibleActions.get(0);
		SerializableTuple<SimplifiedObservation, Types.ACTIONS> sa;
		SimplifiedObservation sso = new SimplifiedObservation (so);
		// select action with highest value for this sso
		for (Types.ACTIONS a : possibleActions)
		{
			// Create state-action tuple
			sa = new SerializableTuple<SimplifiedObservation, Types.ACTIONS>(sso, a);
			// Get the next action value, with a little bit of noise to enable
			// random selection
			value = Utils.noise(q.get(sa), THETA, random.nextDouble());
			if(value > maxValue)
			{
				maxValue = value;
				maxAction = a;
			}
		}
		// return the optimal action
		return maxAction;
	}

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		//System.out.println("Starting action");
		// Create simplified observation:
		explore(so, elapsedTimer);
		return greedyAction(so);
	}

	/** write q to file */
	@Override
	public final void teardown()
	{
		Lib.writeHashMapToFile(q, filename);
		super.teardown();
	}
}
