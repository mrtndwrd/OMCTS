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
		// Instantiate previousState to the starting state to prevent null
		// pointers.
		// Instantiate currentOption with an epsilon-greedy option FIXME: Should be in
		// abstract constructor after initializing q
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
		// old score and old state will act as surrogates for previousState and
		// previousScore in this function
		double oldScore, newScore;
		// Save current previousState in firstPreviousState 
		StateObservation firstPreviousState = this.previousState;
		//
		// Key for the q-table
		SerializableTuple<SimplifiedObservation, Option> sop;
		 
		// This loop's surrogate for this.currentOption
		Option chosenOption;
		
		// Currently only the greedy action will have to be taken after this is
		// done, so we can take as much time as possible!
		outerloop:
		while(elapsedTimer.remainingTimeMillis() > 10.)
		{
			// Copy the state so we can advance it
			soCopy = so.copy();
			// Initialize with the current old score. we don't need to
			// initialize the newScore and -State because that's done in the
			// first few lines of the inner loop
			oldScore = previousScore;
			// Start by using the currently chosen option (TODO: Maybe option
			// breaking should be introduced later)
			//Option chosenOption = currentOption.copy();
			if(currentOption == null || currentOption.isFinished(soCopy))
				chosenOption = epsilonGreedyOption(new SimplifiedObservation(soCopy), EPSILON);
			else
				chosenOption = this.currentOption.copy();
			// Instantiate previousState to the current state
			this.previousState = soCopy.copy();
			for(depth=0; depth<explorationDepth && !soCopy.isGameOver(); depth++)
			{
				// This advances soCopy with the action chosen by the option
				soCopy.advance(chosenOption.act(soCopy));
				newScore = score(soCopy);
				//newScore = soCopy.getGameScore();
				// Update option information and new score and, if needed, do
				// epsilon-greegy option selection
				chosenOption = updateOption(chosenOption, soCopy, this.previousState, newScore - oldScore, false);
				// set oldScore to current score
				oldScore = newScore;
				if(elapsedTimer.remainingTimeMillis() < 8.)
				{
					break outerloop;
				}
			}
		}

		// Restore current previousState to what it was before exploring
		this.previousState = firstPreviousState;
	}

	/** updates q values for newState. */
	public void updateQ(Option o, SimplifiedObservation newState, SimplifiedObservation oldState)
	{
		double maxAQ = getMaxQFromState(newState);
		SerializableTuple<SimplifiedObservation, Option> sop = 
			new SerializableTuple <SimplifiedObservation, Option>(oldState, o);
		// Update rule from
		// http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node65.html
		q.put(sop, q.get(sop) + ALPHA * (o.getReward() + GAMMA * 
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

	/** This function does:
	 * 1. update the option reward
	 * 2. check if the option is done
	 * 3. choose a new option if needed
	 * 4. update the Q-values
	 * FIXME: New state/old state somehow should work
	 */
	protected Option updateOption(Option option, StateObservation newState,
			StateObservation oldState, double score, boolean greedy)
	{
		// Add the new reward to this option
		option.addReward(score);
		if(option.isFinished(newState))
		{
			SimplifiedObservation simpleNewState = new SimplifiedObservation(newState);
			SimplifiedObservation simpleOldState = new SimplifiedObservation(oldState);
			// Update q values for the finished option
			updateQ(option, simpleNewState, simpleOldState);
			// get a new option
			if(greedy)
				option = greedyOption(simpleNewState, false);
			else
				option = epsilonGreedyOption(simpleNewState, EPSILON);
			// Change oldState to newState. 
			this.previousState = newState.copy();
		}
		// If a new option is selected, return the new option. Else the old
		// option will be returned
		return option;
	}
}
