package montecarlo;

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
public class Agent extends AbstractAgent
{
	private DefaultHashMap<SerializableTuple<SimplifiedObservation, Types.ACTIONS>, Double> n;
	/** Denominator of the q table */
	private DefaultHashMap<SerializableTuple<SimplifiedObservation, Types.ACTIONS>, Double> d;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		super(so, elapsedTimer);
		this.filename = "tables/montecarlo" + Lib.filePostfix;
		// Load objects 
		try
		{
			Object o = Lib.loadObjectFromFile(filename + 'q');
			q = (DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Types.ACTIONS>, Double>) o;
			//System.out.println("Loaded q");
			//System.out.println(q);
			o = Lib.loadObjectFromFile(filename + 'd');
			d = (DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Types.ACTIONS>, Double>) o;
			o = Lib.loadObjectFromFile(filename + 'n');
			n = (DefaultHashMap<SerializableTuple
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
		{
			System.out.println("Q not initialized.");
			q = new DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Types.ACTIONS>, Double> (DEFAULT_Q_VALUE);
		}
		if(d == null)
		{
			System.out.println("D not initialized.");
			d = new DefaultHashMap<SerializableTuple<SimplifiedObservation, Types.ACTIONS>, Double>(0.);
		}
		if(n == null)
		{
			System.out.println("N not initialized.");
			n = new DefaultHashMap<SerializableTuple<SimplifiedObservation, Types.ACTIONS>, Double>(0.);
		}
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
		// initialize lastNonGreedyDepth to 0, in case all actions are greedy
		int lastNonGreedyDepth;

		// Currently only the greedy action will have to be taken after this is
		// done, so we can take as much time as possible!
		while(elapsedTimer.remainingTimeMillis() > 10.)
		{
			//System.out.println("Starting exploration with remaining time " + elapsedTimer.remainingTimeMillis());
			soCopy = so.copy();
			// create histories of actions and states
			Types.ACTIONS[] actionHistory = new Types.ACTIONS[EXPLORATION_DEPTH];
			SimplifiedObservation[] stateHistory = new SimplifiedObservation[EXPLORATION_DEPTH];
			lastNonGreedyDepth = 0;
			for(depth=0; depth<EXPLORATION_DEPTH && !soCopy.isGameOver(); depth++)
			{
				if(elapsedTimer.remainingTimeMillis() < 4)
				{
					System.out.printf("TOO LITTLE TIME AT START, returning with %d milliseconds left\n",
						elapsedTimer.remainingTimeMillis());
					break;
				}
				// Get a new state and the action that leads to it in an
				// epsilon-greedy manner
				// This advances soCopy with the taken action
				Types.ACTIONS a = epsilonGreedyAction(soCopy, EPSILON);
				if(!lastActionGreedy)
					lastNonGreedyDepth = depth;
				// Advance the state, this should advance everywhere, with pointers and
				// stuff
				SimplifiedObservation s = new SimplifiedObservation(soCopy, aStar);
				soCopy.advance(a);
				// add state-action pair to history arrays
				stateHistory[depth] = s;
				actionHistory[depth] = a;
				// Check the remaining time
				if(elapsedTimer.remainingTimeMillis() < 4)
				{
					System.out.printf("TOO LITTLE TIME AT END, returning with %d milliseconds left\n",
						elapsedTimer.remainingTimeMillis());
					break;
				}
			}
			// process the states and actions from this rollout, using the value
			// of the last visited state
			backUp(stateHistory, actionHistory, stateHeuristic.evaluateState(soCopy), depth, lastNonGreedyDepth);
			//backUp(stateHistory, actionHistory, Lib.simpleValue(soCopy), depth, lastNonGreedyDepth);
			// Reset lastNonGreedyDepth
			lastNonGreedyDepth = 0;
		}
	}

	/** Update q values using Off-Policy Monte Carlo Control
	 * http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node56.html
	 * @param stateHistory The states visited in the last run. stateHistory[0]
	 * is the first state
	 * @param actionHistory The actions taken in each state from stateHistory.
	 * actionHistory[i] is an action taken in stateHistory[i]
	 * @param score The score in the last state in stateHistory. 
	 * @param lastDepth Some times stateHistory.length is not
	 * this.EXPLORATION_DEPTH, because the game was ended before
	 * this.EXPLORATION_DEPTH was reached. Therefore we need the last depth.
	 * This is, in the literature, referred to as T
	 * @param lastNonGreedyDepth depth at which the last non-greedy action was
	 * taken
	 */
	private void backUp(SimplifiedObservation[] stateHistory, 
		Types.ACTIONS[] actionHistory, double score, int lastDepth, 
		int lastNonGreedyDepth)
	{
		if(lastDepth < lastNonGreedyDepth)
		{
			System.out.printf("Something's DEFINITELY wrong! lastDepth: %d, lastNonGreedyDepth: %d\n",
				lastDepth, lastNonGreedyDepth);
		}
		// Will be used as index in the q table
		SerializableTuple<SimplifiedObservation, Types.ACTIONS> sa;
		// Will be used for the score that is currently in the q table
		double lastScore;
		// Will be index of the first state-action pair:
		// t = time of first occurrence of s, a, such that t > lastNonGreedyDepth
		int t;
		// Probability of ending up in state s:
		double w;
		// newN and newD values for first computing and then setting. This
		// prevents getting the values twice
		double newN;
		double newD;
		// Loop from t to T-1
		for(int i=lastNonGreedyDepth; i<lastDepth && stateHistory[i] != null; i++)
		{
			// t = time of first occurrence of s, a, such that t > lastNonGreedyDepth
			t = getFirstStateActionIndex(stateHistory[i], actionHistory[i], 
				stateHistory, actionHistory, lastNonGreedyDepth);

			// w = product(1/(pi'(s_k, a_k)))
			// Simplified version: w = 1/((1-EPSILON)^(T-t))
			w = Math.pow(1/(1-EPSILON), (lastDepth-t));
			sa = new SerializableTuple(stateHistory[i], actionHistory[i]);
			newN = n.get(sa) + w * Math.pow(GAMMA, lastDepth-t) * score;
			n.put(sa, newN);
			newD = d.get(sa) + Math.pow(GAMMA, lastDepth-t) * w;
			d.put(sa, newD);
			q.put(sa, newN/newD);
		}
	}

	/** Searches stateHistory and actionHistory for the first occurrence of the
	 * given state action pair after index 'first'
	 * */
	private int getFirstStateActionIndex(SimplifiedObservation state, 
		Types.ACTIONS action, SimplifiedObservation[] stateHistory, 
		Types.ACTIONS[] actionHistory, int first)
	{
		if(stateHistory.length != actionHistory.length)
		{
			System.out.println(
				"WARNING: stateHistory size differs from actionHistory size!");
			return 0;
		}
		for(int i=first; i<stateHistory.length; i++)
		{
			if(state.equals(stateHistory[i]) && action.equals(actionHistory[i]))
				return i;
		}
		System.out.println("WARNING state action pair not found!? Returning last index");
		return stateHistory.length - 1;
	}


	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		//System.out.println("Starting action");
		// Create simplified observation:
		explore(so, elapsedTimer);
		//System.out.printf("Returning greedy action with %d time left\n", elapsedTimer.remainingTimeMillis());
		return greedyAction(so, true);
	}

	/** write q to file */
	@Override
	public final void teardown()
	{
		Lib.writeHashMapToFile(q, filename + "q");
		//System.out.println("Written q");
		//System.out.println(q);
		Lib.writeHashMapToFile(d, filename + "d");
		Lib.writeHashMapToFile(n, filename + "n");
		super.teardown();
	}
}
