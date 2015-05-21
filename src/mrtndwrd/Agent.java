package mrtndwrd;

import controllers.Heuristics.StateHeuristic;
import controllers.Heuristics.SimpleStateHeuristic;
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
import java.util.HashSet;
import java.io.FileOutputStream;
import java.util.Iterator;

/**
 * User: mrtndwrd
 * Date: 13-01-2015
 * @author Maarten de Waard
 */
public class Agent extends AbstractPlayer
{
	/** Exploration depth for building q and v */
	private final int INIT_EXPLORATION_DEPTH = 30;

	protected ArrayList<Option> possibleOptions;
	/** Mapping from State, Option (as index from above options array) to
	 * expected Reward (value), the "Q table" */
	protected DefaultHashMap<SerializableTuple<SimplifiedObservation, Option>, Double> q;

	protected Random random = new Random();

	/** Saves the last non-greedy option timestep */
	protected boolean lastOptionGreedy = false;
	/** The option that is currently being followed */
	protected Option currentOption;
	/** The previous score, assumes scores always start at 0 (so no games should
	 * be present that have a score that decreases over time and starts > 0 or
	 * something) */
	protected double previousScore;
	/** The previous state */
	protected StateObservation previousState;

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
	public final double THETA = 1e-5;

	/** File to write q table to */
	protected String filename;

	/** Own state heuristic */
	protected StateHeuristic stateHeuristic;

	/** A set containing which obsId's already have options in this agent */
	protected HashSet<Integer> optionObsIDs = new HashSet<Integer>();

	/** AStar for searching for stuff */
	public static AStar aStar;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		Agent.aStar = new AStar(so);
		stateHeuristic = new SimpleStateHeuristic(so);
		possibleOptions = new ArrayList<Option>();
		this.previousScore = score(so);
		// instantiate possibleOptions with actions
		setOptionsForActions(so.getAvailableActions());
		// TODO: More options here!
		setGoToPositionOptions(so);
		setGoToMovableOptions(so);
		
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
		// Instantiate currentOption with an epsilon-greedy option
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

		// Create a shallow copy of the possibleObsIDs and to restore at the end
		HashSet<Integer> pObsIDs = (HashSet<Integer>) this.optionObsIDs.clone();
		ArrayList<Option> pOptions = (ArrayList<Option>) this.possibleOptions.clone();
		
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
				// Find new possible options
				setGoToMovableOptions(soCopy);
				
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

		// Restore current previousState, possibleOptions and possibleObsIDs to
		// what it was before exploring
		this.previousState = firstPreviousState;
		this.possibleOptions = pOptions;
		this.optionObsIDs = pObsIDs;
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

	/** Scores the state. This enables simple changing of the scoring method
	 * without having to change it everywhere
	 */
	protected double score(StateObservation so)
	{
		return Lib.simpleValue(so);
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

	/** Create aStar options to things in so */
	protected void setGoToPositionOptions(StateObservation so)
	{
		// TODO: Loop thrhough EVERY array/ArrayList
		//this.possibleOptions.add(new GoToPositionOption(GAMMA, so.getNPCPositions()[0].get(0).position));
	}

	protected void setGoToMovableOptions(StateObservation so)
	{
		int i = 0;
		// Holds the ObsIDs that were already present in this.optionObsIDs
		HashSet<Integer> keepObsIDs = new HashSet<Integer>();
		// Holds the new obsIDs
		HashSet<Integer> newObsIDs = new HashSet<Integer>();
		for(ArrayList<Observation> npcType : so.getNPCPositions())
		{
			//System.out.printf("NPC's with id %d:\n [", i);
			for(Observation npc : npcType)
			{
				//System.out.printf("%d, ", npc.obsID);
				if(! this.optionObsIDs.contains(npc.obsID))
				{
					// Create option for this obsID
					this.possibleOptions.add(new GoToMovableOption(GAMMA, 
						Lib.MOVABLE_TYPE.NPC, i, npc.obsID));
				}
				else
					// Add to the list of options that should be kept
					keepObsIDs.add(npc.obsID);
				newObsIDs.add(npc.obsID);
			}
			//System.out.print("]\n");
			i++;
		}
		//System.out.println("New observations: \n" + newObsIDs);
		//System.out.println("Keep observations: \n" + keepObsIDs);
		// Remove all "old" obsIDs from this.optionObsIDs. optionObsIDs will
		// then only contain obsolete obsIDs
		this.optionObsIDs.removeAll(keepObsIDs);
		// Now remove all options that have the obsIDs in optionObsIDs.
		// We use the iterator, in order to ensure removing while iterating is
		// possible
		// TODO: Check if this works
		for (Iterator<Option> it = this.possibleOptions.iterator(); it.hasNext();)
		{
			Option option = it.next();
			// Remove the options that are still in optionObsIDs.
			if(this.optionObsIDs.contains(option.getObsID()))
			{
				it.remove();
			}
		}
		// Now all options are up-to-date. this.optionObsIDs should be updated
		// to represent the current options list:
		this.optionObsIDs = newObsIDs;
		//System.out.println("Current option set:");
		//System.out.println(this.possibleOptions);
		//System.out.println("Current optionObsIDs:");
		//System.out.println(this.optionObsIDs);


	}

	/** Selects an epsilon greedy value based on the internal q table. Returns
	 * the optimal option as index of the this.actions array.
	 */
	protected Option epsilonGreedyOption(SimplifiedObservation sso, double epsilon)
	{
		// Either way we need to know WHAT the greedy option is, in order to
		// know whether we have taken a greedy option
		Option greedyOption = greedyOption(sso);
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
	 * @param sso The simplified state observation
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
			if(print)
				System.out.printf("Value %s = %f\n", o, value);
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

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		setGoToMovableOptions(so);
		double newScore = score(so);
		//double newScore = so.getGameScore();
		explore(so, elapsedTimer, EXPLORATION_DEPTH);
		// update option. This also updates this.previousState if needed
		// We can only update from step 2
		if(this.previousState == null)
		{
			// First time, initialize previous state to the current state and currentOption to
			// greedy option: From now on, stuff can happen!
			this.previousState = so;
			SimplifiedObservation newState = new SimplifiedObservation(so);
			currentOption = greedyOption(newState);
		}
		else
		{
			currentOption = updateOption(this.currentOption, so, this.previousState, newScore - previousScore, true);
		}
		
		this.previousScore = newScore;
		return currentOption.act(so);
	}

	/** Overload for backwards compatibility */
	protected Option greedyOption(SimplifiedObservation sso)
	{
		return greedyOption(sso, false);
	}

	/** write q to file */
	@Override
	public void teardown()
	{
		Lib.writeHashMapToFile(q, filename);
		System.out.println(q);
		super.teardown();
	}
}
