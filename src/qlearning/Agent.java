package qlearning;

import controllers.Heuristics.StateHeuristic;
import controllers.Heuristics.SimpleStateHeuristic;
import controllers.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.game.Observation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

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
	protected final int INIT_EXPLORATION_DEPTH = 30;

	protected ArrayList<Option> possibleOptions;
	/** Mapping from State, Option (as index from above options array) to
	 * expected Reward (value), the "Q table" */
	protected DefaultHashMap<SerializableTuple<SimplifiedObservation, Option>, Double> q;

	public static Random random;

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
	public static double GAMMA = .9;
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

	/** orientation */
	public static Vector2d avatarOrientation;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) 
	{
		Agent.random = new Random();
		Agent.aStar = new AStar(so);
		stateHeuristic = new SimpleStateHeuristic(so);
		possibleOptions = new ArrayList<Option>();
		this.previousScore = score(so);
		// instantiate possibleOptions with actions
		setOptionsForActions(so.getAvailableActions());
		setOptions(so);
		
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
		setAvatarOrientation(so);
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
				setOptions(soCopy);
				
				//newScore = soCopy.getGameScore();
				// Update option information and new score and, if needed, do
				// epsilon-greegy option selection
				chosenOption = updateOption(chosenOption, soCopy, this.previousState, newScore - oldScore, false);
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
		q.put(sop, q.get(sop) + ALPHA * (o.getReward() + 
					Math.pow(GAMMA, o.getStep()) * maxAQ - q.get(sop)));
	}

	protected double getMaxQFromState(SimplifiedObservation newState)
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
			//if(greedy)
			//	System.out.println("Changed to option " + option);
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
	protected void setOptionsForActions(ArrayList<Types.ACTIONS> actions)
	{
		for(Types.ACTIONS action : actions)
		{
			this.possibleOptions.add(new ActionOption(GAMMA, action));
		}
	}

	protected void setOptions(StateObservation so)
	{
		// Holds the ObsIDs that were already present in this.optionObsIDs
		HashSet<Integer> keepObsIDs = new HashSet<Integer>();
		// Holds the new obsIDs
		HashSet<Integer> newObsIDs = new HashSet<Integer>();

		ArrayList<Types.ACTIONS> act = so.getAvailableActions();
		
		// Set options for all types of sprites that exist in this game. If they
		// don't exist, the getter will return null and no options will be
		// created.
		if(act.contains(Types.ACTIONS.ACTION_UP) &&
				act.contains(Types.ACTIONS.ACTION_DOWN) &&
				act.contains(Types.ACTIONS.ACTION_LEFT) &&
				act.contains(Types.ACTIONS.ACTION_RIGHT))
		{
			if(so.getNPCPositions() != null)
				createOptions(so.getNPCPositions(), Lib.GETTER_TYPE.NPC, so, keepObsIDs, newObsIDs);
			if(so.getMovablePositions() != null)
				createOptions(so.getMovablePositions(), Lib.GETTER_TYPE.MOVABLE, so, keepObsIDs, newObsIDs);
			if(so.getImmovablePositions() != null)
				createOptions(so.getImmovablePositions(), Lib.GETTER_TYPE.IMMOVABLE, so, keepObsIDs, newObsIDs);
			if(so.getResourcesPositions() != null)
				createOptions(so.getResourcesPositions(), Lib.GETTER_TYPE.RESOURCE, so, keepObsIDs, newObsIDs);
			if(so.getPortalsPositions() != null)
				createOptions(so.getPortalsPositions(), Lib.GETTER_TYPE.PORTAL, so, keepObsIDs, newObsIDs);

			//// Remove all "old" obsIDs from this.optionObsIDs. optionObsIDs will
			//// then only contain obsolete obsIDs
			//this.optionObsIDs.removeAll(keepObsIDs);
			//// Now remove all options that have the obsIDs in optionObsIDs.
			//// We use the iterator, in order to ensure removing while iterating is
			//// possible
			//for (Iterator<Option> it = this.possibleOptions.iterator(); it.hasNext();)
			//{
			//	Option option = it.next();
			//	// Remove the options that are still in optionObsIDs.
			//	if(this.optionObsIDs.contains(option.getObsID()))
			//	{
			//		it.remove();
			//	}
			//}
			//// Now all options are up-to-date. this.optionObsIDs should be updated
			//// to represent the current options list:
			//this.optionObsIDs = newObsIDs;
		}

		// Wait and shoot option:
		for(int range=0; range<7; range+=2)
		{
			if(so.getNPCPositions() != null
				&& act.contains(Types.ACTIONS.ACTION_USE))
			{
				for(ArrayList<Observation> ao : so.getNPCPositions())
				{
					if(ao.size() > 0)
					{
						WaitAndShootOption o = new WaitAndShootOption(GAMMA, 
								ao.get(0).itype, range);
						if(!this.possibleOptions.contains(o))
							this.possibleOptions.add(o);
					}
				}
			}
		}

		// AvoidNearest option: 
		AvoidNearestNpcOption anno = new AvoidNearestNpcOption(GAMMA);
		if(so.getNPCPositions() == null || so.getNPCPositions().length == 0)
			this.possibleOptions.remove(anno);
		else if(!this.possibleOptions.contains(anno))
		{
			this.possibleOptions.add(anno);
		}

		// We use the iterator, in order to ensure removing while iterating is
		// possible
		for(Iterator<Option> it = this.possibleOptions.iterator(); it.hasNext();)
		{
			Option option = it.next();
			// Remove the options that are still in optionObsIDs.
			if(option.isFinished(so))
			{
				//System.out.println("Option " + option + " is finished");
				it.remove();
			}
		}
	}

	/** Adds new obsIDs to newObsIDs the set and ID's that should be kept to
	 * keepObsIDs, based on the ID's in the ArrayList observations
	 * Also creates options for all new obsIDs
	 */
	protected void createOptions(ArrayList<Observation>[] observations,
			Lib.GETTER_TYPE type,
			StateObservation so,
			HashSet<Integer> keepObsIDs,
			HashSet<Integer> newObsIDs)
	{
		// Loop through all types of NPCs
		for(ArrayList<Observation> observationType : observations)
		{
			// Loop through all the NPC's of this type
			for(Observation observation : observationType)
			{
				// ignore walls
				if(observation.itype == 0)
					continue;
				// Check if this is a new obsID
				if(! this.optionObsIDs.contains(observation.obsID))
				{
					Option o;
					// Create option for this obsID
					if(type == Lib.GETTER_TYPE.NPC || type == Lib.GETTER_TYPE.MOVABLE)
					{
						o = new GoToMovableOption(GAMMA, 
							type, observation.itype, observation.obsID, so);
						Option o2 = new GoNearMovableOption(GAMMA, 
							type, observation.itype, observation.obsID, so);
						if(!this.possibleOptions.contains(o2))
							this.possibleOptions.add(o2);
					}
					else
					{
						o = new GoToPositionOption(GAMMA, 
							type, observation.itype, observation.obsID, so);
					}
					if(!this.possibleOptions.contains(o))
						this.possibleOptions.add(o);
				}
				//else
				//	// Add to the list of options that should be kept
				//	keepObsIDs.add(observation.obsID);
				//newObsIDs.add(observation.obsID);
			}
			//System.out.print("]\n");
		}
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
			System.out.println("SSO: " + sso);
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
		//System.out.println(q);
		super.teardown();
	}
	private void setAvatarOrientation(StateObservation so)
	{
		if(so.getAvatarOrientation().x == 0. && 
				so.getAvatarOrientation().y == 0.)
			Agent.avatarOrientation = Lib.spriteFromAvatarOrientation(so);
		else
			Agent.avatarOrientation = so.getAvatarOrientation();
	}

	public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		setAvatarOrientation(so);
		setOptions(so);
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
			SimplifiedObservation newState = new SimplifiedObservation(so);
			// Always choose a new option, in order to not get stuck
			currentOption = greedyOption(newState);
		}
		//System.out.println("Astar:\n" + aStar);
		//System.out.println("Possible options" + this.possibleOptions);
		//System.out.println("Following option " + currentOption);
		this.previousScore = newScore;
		return currentOption.act(so);
	}
}
