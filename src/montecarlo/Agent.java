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
	private DefaultHashMap<SerializableTuple<SimplifiedObservation, Option>, Double> n;
	/** Denominator of the q table */
	private DefaultHashMap<SerializableTuple<SimplifiedObservation, Option>, Double> d;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		super(so, elapsedTimer);
		this.filename = "tables/montecarlo" + Lib.filePostfix;
		// Load objects 
		try
		{
			Object o = Lib.loadObjectFromFile(filename + 'q');
			q = (DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Option>, Double>) o;
			//System.out.println("Loaded q");
			//System.out.println(q);
			o = Lib.loadObjectFromFile(filename + 'd');
			d = (DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Option>, Double>) o;
			o = Lib.loadObjectFromFile(filename + 'n');
			n = (DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Option>, Double>) o;
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
				<SimplifiedObservation, Option>, Double> (DEFAULT_Q_VALUE);
		}
		if(d == null)
		{
			System.out.println("D not initialized.");
			d = new DefaultHashMap<SerializableTuple<SimplifiedObservation, Option>, Double>(0.);
		}
		if(n == null)
		{
			System.out.println("N not initialized.");
			n = new DefaultHashMap<SerializableTuple<SimplifiedObservation, Option>, Double>(0.);
		}
		explore(so, elapsedTimer, EXPLORATION_DEPTH);
		System.out.printf("End of constructor, miliseconds remaining: %d\n", elapsedTimer.remainingTimeMillis());
	}

	/** 
	 * Explore using the state observation and enter values in the V and Q
	 * tables 
	 */
	public void explore(StateObservation so, ElapsedCpuTimer elapsedTimer, int explorationDepth)
	{
		// Initialize variables for loop:
		// depth of inner for loop
		int depth;
		// state that is advanced to try out a new action
		StateObservation soCopy;
		// Simplified observation made of soCopy
		SimplifiedObservation s;
		// initialize lastNonGreedyDepth to 0, in case all actions are greedy
		int lastNonGreedyDepth = 0;
		// Action chosen bij the option
		Types.ACTIONS a;
		// used for checking if an option is finished before updating it
		boolean finished;
		// score keeping
		double newScore, oldScore = 0;

		// Currently only the greedy action will have to be taken after this is
		// done, so we can take as much time as possible!
		while(elapsedTimer.remainingTimeMillis() > 10.)
		{
			//System.out.println("Starting exploration with remaining time " + elapsedTimer.remainingTimeMillis());
			soCopy = so.copy();
			// create histories of options and states
			Option[] optionHistory = new Option[explorationDepth];
			SimplifiedObservation[] stateHistory = 
				new SimplifiedObservation[explorationDepth];
			lastNonGreedyDepth = 0;
			depth = 0;
			// At first, use the currently chosen option (TODO: Maybe option
			// breaking should be introduced later)
			Option chosenOption = currentOption;
			s = new SimplifiedObservation(soCopy);
			while(depth<explorationDepth && !soCopy.isGameOver())
			{
				// {{{ Check starting time
				if(elapsedTimer.remainingTimeMillis() < 4)
				{
					System.out.printf("TOO LITTLE TIME AT START, returning with %d milliseconds left\n",
						elapsedTimer.remainingTimeMillis());
					break;
				} // }}}
				newScore = Lib.simpleValue(soCopy);
				// Firstly, check if the current option is finished
				finished = chosenOption.isFinished();
				// Then, update the option, maybe chosing a new one
				// the oldState is not actually used in this class
				chosenOption = updateOption(chosenOption, s, null, newScore - oldScore, false);
				// If a new option is chosen, add it and the current state to 
				// the history
				if(finished)
				{
					// Add the finished option to the array of chosen options
					optionHistory[depth] = chosenOption;
					stateHistory[depth] = s;
					depth++;
				}
				// Get the action from the option
				a = chosenOption.act(soCopy);
				
				if(!lastOptionGreedy)
					lastNonGreedyDepth = depth;
				// Advance the state, this should advance everywhere, with pointers and
				// stuff
				soCopy.advance(a);
				s = new SimplifiedObservation(soCopy);
				// Set oldState and oldScore to current state and score
				oldScore = newScore;
				// {{{ Check remaining time 
				if(elapsedTimer.remainingTimeMillis() < 4)
				{
					System.out.printf("TOO LITTLE TIME AT END, returning with %d milliseconds left\n",
						elapsedTimer.remainingTimeMillis());
					break;
				} // }}}
			}
			// process the states and options from this rollout, using the value
			// of the last visited state
			backUp(stateHistory, optionHistory, stateHeuristic.evaluateState(soCopy), depth, lastNonGreedyDepth);
			// Reset lastNonGreedyDepth
			lastNonGreedyDepth = 0;
		}
	}

	/** This function does:
	 * 1. update the option reward
	 * 2. check if the option is done
	 * 3. choose a new option if needed
	 * 4. update the Q-values
	 * FIXME I should find a way to move this code to the abstract agent...
	 * oldState is not actually used in this class
	 */
	protected Option updateOption(Option option, SimplifiedObservation newState,
			SimplifiedObservation oldState, double score, boolean greedy)
	{
		// Add the new reward to this option
		option.addReward(score);
		if(option.isFinished())
		{
			// get a new option
			if(greedy)
				option = greedyOption(newState);
			else
				option = epsilonGreedyOption(newState, EPSILON);
			oldState = newState;
		}
		// If a new option is selected, return the new option. Else the old
		// option will be returned
		return option;
	}

	/** Update q values using Off-Policy Monte Carlo Control
	 * http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node56.html
	 * @param stateHistory The states visited in the last run. stateHistory[0]
	 * is the first state
	 * @param optionHistory The options chosen in each state from stateHistory.
	 * optionHistory[i] is an option chosen in stateHistory[i]
	 * @param score The score in the last state in stateHistory. 
	 * @param lastDepth Some times stateHistory.length is not
	 * this.EXPLORATION_DEPTH, because the game was ended before
	 * this.EXPLORATION_DEPTH was reached. Therefore we need the last depth.
	 * This is, in the literature, referred to as T
	 * @param lastNonGreedyDepth depth at which the last non-greedy option was
	 * selected
	 */
	private void backUp(SimplifiedObservation[] stateHistory, 
		Option[] optionHistory, double score, int lastDepth, 
		int lastNonGreedyDepth)
	{
		if(lastDepth < lastNonGreedyDepth)
		{
			System.out.printf("Something's DEFINITELY wrong! lastDepth: %d, lastNonGreedyDepth: %d\n",
				lastDepth, lastNonGreedyDepth);
		}
		// Will be used as index in the q table
		SerializableTuple<SimplifiedObservation, Option> sa;
		// Will be used for the score that is currently in the q table
		double lastScore;
		// Will be index of the first state-option pair:
		// t = time of first occurrence of s, o, such that t > lastNonGreedyDepth
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
			t = getFirstStateOptionIndex(stateHistory[i], optionHistory[i], 
				stateHistory, optionHistory, lastNonGreedyDepth);

			// w = product(1/(pi'(s_k, a_k)))
			// Simplified version: w = 1/((1-EPSILON)^(T-t))
			w = Math.pow(1/(1-EPSILON), (lastDepth-t));
			sa = new SerializableTuple(stateHistory[i], optionHistory[i]);
			newN = n.get(sa) + w * Math.pow(GAMMA, lastDepth-t) * score;
			n.put(sa, newN);
			newD = d.get(sa) + Math.pow(GAMMA, lastDepth-t) * w;
			d.put(sa, newD);
			q.put(sa, newN/newD);
		}
	}

	/** Searches stateHistory and optionHistory for the first occurrence of the
	 * given state/option pair after index 'first'
	 * */
	private int getFirstStateOptionIndex(SimplifiedObservation state, 
		Option option, SimplifiedObservation[] stateHistory, 
		Option[] optionHistory, int first)
	{
		if(stateHistory.length != optionHistory.length)
		{
			System.err.println(
				"WARNING: stateHistory size differs from optionHistory size!");
			return 0;
		}
		for(int i=first; i<stateHistory.length; i++)
		{
			if(state.equals(stateHistory[i]) && option.equals(optionHistory[i]))
				return i;
		}
		System.err.println("WARNING state option pair not found!? Returning last index");
		return stateHistory.length - 1;
	}

	/** write q to file */
	@Override
	public void teardown()
	{
		Lib.writeHashMapToFile(d, filename + "d");
		Lib.writeHashMapToFile(n, filename + "n");
		super.teardown();
	}
}
