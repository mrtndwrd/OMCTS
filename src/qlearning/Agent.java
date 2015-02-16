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
				<SimplifiedObservation, Types.ACTIONS>, Double>) o;
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

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		// Create simplified observation:
		explore(so, elapsedTimer, EXPLORATION_DEPTH);
		return greedyAction(so, true);
	}

	/** write q to file */
	@Override
	public final void teardown()
	{
		Lib.writeHashMapToFile(q, filename);
		super.teardown();
	}
}
