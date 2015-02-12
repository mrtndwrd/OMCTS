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
	private final Double DEFAULT_Q_VALUE = 10.0;
	/** Exploration depth for building q and v */
	private final int INIT_EXPLORATION_DEPTH = 30;
	/** Exploration depth for building q and v */
	private final int EXPLORATION_DEPTH = 10;
	/** Epsilon for exploration vs. exploitation */
	private final double EPSILON = .5;
	/** The learning rate of this algorithm */
	private final double ALPHA = .3;
	/** Gamma for bellman equation */
	private final double GAMMA = .95;
	/** Theta for noise */
	private final double THETA = 1e-6;

	/** File to write q table to */
	private String filename;
	/** AStar for searching for stuff */
	private AStar aStar;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		// Instantiate a* walls
		aStar = new AStar(so);
		this.filename = "tables/qlearning" + Lib.filePostfix;
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
		explore(so, elapsedTimer, INIT_EXPLORATION_DEPTH);
		System.out.printf("End of constructor, miliseconds remaining: %d\n", elapsedTimer.remainingTimeMillis());
	}

	/** 
	 * Explore using the state observation and enter values in the V and Q
	 * tables 
	 */
	public void explore(StateObservation so, ElapsedCpuTimer elapsedTimer, 
		int explorationDepth)
	{
		int depth;
		StateObservation soCopy;
		double maxAQ, oldScore, newScore;
		SerializableTuple<SimplifiedObservation, Types.ACTIONS> sa;
		SimplifiedObservation newState;
		
		// Currently only the greedy action will have to be taken after this is
		// done, so we can take as much time as possible!
		while(elapsedTimer.remainingTimeMillis() > 5.)
		{
			// Copy the state so we can advance it
			soCopy = so.copy();
			// Advance the state, this should advance everywhere, with pointers and
			// stuff
			SimplifiedObservation s = new SimplifiedObservation(soCopy, aStar);
			oldScore = Lib.simpleValue(soCopy);
			for(depth=0; depth<explorationDepth && !soCopy.isGameOver(); depth++)
			{
				// Get a new state and the action that leads to it in an
				// epsilon-greedy manner
				// This advances soCopy with the taken action
				Types.ACTIONS a = epsilonGreedyAction(soCopy, EPSILON);
				soCopy.advance(a);
				newState = new SimplifiedObservation(soCopy, aStar);
				newScore = Lib.simpleValue(soCopy);
				maxAQ = getMaxQFromState(newState);
				sa = new SerializableTuple
					<SimplifiedObservation, Types.ACTIONS>(s, a);
				// Update rule from
				// http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node65.html
				q.put(sa, q.get(sa) + ALPHA * (newScore - oldScore + GAMMA * 
					maxAQ - q.get(sa)));
				s = newState;
				oldScore = newScore;
				if(elapsedTimer.remainingTimeMillis() < 3)
					return;
			}
		}
	}

	private double getMaxQFromState(SimplifiedObservation newState)
	{
		SerializableTuple<SimplifiedObservation, Types.ACTIONS> sa;
		double maxAQ = Lib.HUGE_NEGATIVE;
		double qValue;
		for (Types.ACTIONS a : possibleActions)
		{
			sa = new SerializableTuple
				<SimplifiedObservation, Types.ACTIONS>(newState, a);
			qValue = q.get(sa);
			if(qValue > maxAQ)
			{
				maxAQ = qValue;
			}
		}
		return maxAQ;
	}

	/** Selects an epsilon greedy value based on the internal q table. Returns
	 * the optimal action as index of the this.actions array.
	 */
	private Types.ACTIONS epsilonGreedyAction(StateObservation so, double epsilon)
	{
		// Select a random action with prob. EPSILON
		if(random.nextDouble() < epsilon)
		{
			return possibleActions.get(random.nextInt(possibleActions.size()));
		}
		// Else, select greedy action:
		return greedyAction(so);
	}

	/** Take the greedy action, based on HashMap q. 
	 * @param so The state observation
	 * @param print if this is true, the observation and greedy action are
	 * printed 
	 */
	private Types.ACTIONS greedyAction(StateObservation so, boolean print)
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
	private Types.ACTIONS greedyAction(StateObservation so)
	{
		return greedyAction(so, false);
	}

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		//System.out.println("Starting action");
		// Create simplified observation:
		explore(so, elapsedTimer, EXPLORATION_DEPTH);
		//return greedyAction(so, true);
		return epsilonGreedyAction(so, .005);
	}

	/** write q to file */
	@Override
	public final void teardown()
	{
		Lib.writeHashMapToFile(q, filename);
		super.teardown();
	}
}
