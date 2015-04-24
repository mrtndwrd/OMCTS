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
public class Agent extends AbstractAgent
{
	/** Exploration depth for building q and v */
	private final int INIT_EXPLORATION_DEPTH = 30;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		super(so, elapsedTimer);
		this.filename = "tables/qlearning" + Lib.filePostfix;
		try
		{
			Object o = Lib.loadObjectFromFile(filename);
			q = (DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Option>, Double>) o;
			System.out.printf("time remaining: %d\n", 
				elapsedTimer.remainingTimeMillis());
		}
		catch (Exception e)
		{
			System.out.println(
				"probably, it wasn't a hashmap, or the file didn't exist or something. Using empty q table");
			e.printStackTrace();
		}
		if(q == null)
			q = new DefaultHashMap<SerializableTuple
				<SimplifiedObservation, Option>, Double> (DEFAULT_Q_VALUE);
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
		// Initialize variables for loop:
		// depth of inner for loop
		int depth;
		// state that is advanced to try out a new action
		StateObservation soCopy;
		double oldScore, newScore;
		// Key for the q-table
		SerializableTuple<SimplifiedObservation, Option> sop;
		// State observations
		SimplifiedObservation newState, oldState;
		
		// Currently only the greedy action will have to be taken after this is
		// done, so we can take as much time as possible!
		while(elapsedTimer.remainingTimeMillis() > 5.)
		{
			// Copy the state so we can advance it
			soCopy = so.copy();
			newState = SimplifiedObservation(soCopy);
			oldScore = Lib.simpleValue(soCopy);
			// At first, use the currently chosen option (TODO: Maybe option
			// breaking should be introduced later)
			Option chosenOption = currentOption;
			for(depth=0; depth<explorationDepth && !soCopy.isGameOver(); depth++)
			{
				newScore = Lib.simpleValue(soCopy);
				// Update option information and new score and, if needed, do
				// epsilon-greegy option selection
				chosenOption = updateOption(oldState, newState, newScore, oldScore, false);
				// This advances soCopy with the action chosen by the option
				soCopy.advance(chosenOption.act(soCopy));
				newState = new SimplifiedObservation(soCopy, aStar);
				// Set oldState and oldScore to current state and score
				oldState = newState;
				oldScore = newScore;
				if(elapsedTimer.remainingTimeMillis() < 3)
					return;
			}
		}
	}

	/** updates q values for newState. reward = newScore - oldScore */
	public void updateQ(SimplifiedObservation oldState, SimplifiedObservation newState, double reward)
	{
		double maxAQ = getMaxQFromState(newState);
		sop = new SerializableTuple
			<SimplifiedObservation, Option>(oldState, a);
		// Update rule from
		// http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node65.html
		q.put(sop, q.get(sop) + ALPHA * (reward + GAMMA * 
			maxAQ - q.get(sop)));
	}

	private double getMaxQFromState(SimplifiedObservation newState)
	{
		SerializableTuple<SimplifiedObservation, Option> sop;
		double maxAQ = Lib.HUGE_NEGATIVE;
		double qValue;
		for (Option a : possibleOptions)
		{
			sop = new SerializableTuple
				<SimplifiedObservation, Option>(newState, a);
			qValue = q.get(sop);
			if(qValue > maxAQ)
			{
				maxAQ = qValue;
			}
		}
		return maxAQ;
	}

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		double newScore = Lib.simpleValue(so);
		explore(so, elapsedTimer, EXPLORATION_DEPTH);
		currentOption = updateOption(this.currentOption, 
			new SimplifiedObservation(so), newScore, previousScore, true)
		this.previousScore = newScore;
		return currentOption.act();
	}

	/** This function does:
	 * 1. update the option reward
	 * 2. check if the option is done
	 * 3. choose a new option if needed
	 * 4. update the Q-values
	 * FIXME I should find a way to move this code to the abstract agent...
	 */
	private Option updateOption(Option option, SimplifiedObservation sso, 
			double newScore, double previousScore, boolean greedy)
	{
		// Add the new reward to this option
		option.addReward(newScore - previousScore);
		if(option.isFinished())
		{
			// Update q values for the finished option
			updateQ(sso, option.getReward());
			// get a new option
			if(greedy)
				option = greedyOption(sso);
			else
				option = epsilonGreedyOption(sso, EPSILON);
		}
		// If a new option is selected, return the new option. Else the old
		// option will be returned
		return option;
	}

	/** write q to file */
	@Override
	public final void teardown()
	{
		Lib.writeHashMapToFile(q, filename);
		super.teardown();
	}
}
