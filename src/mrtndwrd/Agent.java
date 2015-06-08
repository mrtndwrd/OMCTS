package mrtndwrd;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

	public static int ROLLOUT_DEPTH = 15;
	public static double K = Math.sqrt(2);

	/** AStar for searching for stuff */
	public static AStar aStar;

	/** Random generator for the agent. */
	private SingleMCTSPlayer mctsPlayer;
		
	/** list of actions for random action selection in rollout */
	public static Types.ACTIONS[] actions;

	/** The gamma of this algorithm */
	public static final double GAMMA = .9;

	/** The set of all options that are currently available */
	public ArrayList<Option> possibleOptions = new ArrayList<Option>();

	/** A set containing which obsId's already have options in this agent */
	protected HashSet<Integer> optionObsIDs = new HashSet<Integer>();

	/** Currently followed option */
	private Option currentOption;

	/**
	 * Public constructor with state observation and time due.
	 * @param so state observation of the current game.
	 * @param elapsedTimer Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		Agent.aStar = new AStar(so);
		ArrayList<Types.ACTIONS> act = so.getAvailableActions();
		
		// Add the actions to the option set
		setOptionsForActions(act);
		//setOptions(so, this.possibleOptions, this.optionObsIDs);
		
		// Create actions for rollout
		actions = new Types.ACTIONS[act.size()];
		for(int i = 0; i < actions.length; ++i)
		{
			actions[i] = act.get(i);
		}

		//Create the player.
		mctsPlayer = new SingleMCTSPlayer(new Random());
	}

	// TODO: Make like setOptions
	public void setOptionsForActions(ArrayList<Types.ACTIONS> actions)
	{
		for(Types.ACTIONS action : actions)
		{
			this.possibleOptions.add(new ActionOption(GAMMA, action));
		}
	}

	// TODO: Move to lib
	public static void setOptions(StateObservation so, ArrayList<Option> possibleOptions, HashSet<Integer> optionObsIDs)
	{
		// Holds the ObsIDs that were already present in this.optionObsIDs
		HashSet<Integer> keepObsIDs = new HashSet<Integer>();
		// Holds the new obsIDs
		HashSet<Integer> newObsIDs = new HashSet<Integer>();
		
		// Set options for all types of sprites that exist in this game. If they
		// don't exist, the getter will return null and no options will be
		// created.
		if(so.getNPCPositions() != null)
		{
			//createOptions(so.getNPCPositions(), Lib.GETTER_TYPE.NPC, so, keepObsIDs, newObsIDs, possibleOptions, optionObsIDs);
			if(so.getAvailableActions().contains(Types.ACTIONS.ACTION_USE))
				createOptions(so.getNPCPositions(), Lib.GETTER_TYPE.NPC_KILL, so, keepObsIDs, newObsIDs, possibleOptions, optionObsIDs);
		}
		//if(so.getMovablePositions() != null)
		//	createOptions(so.getMovablePositions(), Lib.GETTER_TYPE.MOVABLE, so, keepObsIDs, newObsIDs, possibleOptions, optionObsIDs);
		//if(so.getImmovablePositions() != null)
		//	createOptions(so.getImmovablePositions(), Lib.GETTER_TYPE.IMMOVABLE, so, keepObsIDs, newObsIDs, possibleOptions, optionObsIDs);
		//if(so.getResourcesPositions() != null)
		//	createOptions(so.getResourcesPositions(), Lib.GETTER_TYPE.RESOURCE, so, keepObsIDs, newObsIDs, possibleOptions, optionObsIDs);
		//if(so.getPortalsPositions() != null)
		//	createOptions(so.getPortalsPositions(), Lib.GETTER_TYPE.PORTAL, so, keepObsIDs, newObsIDs, possibleOptions, optionObsIDs);

		// We can use a weapon! Try to make kill-options

		// Remove all "old" obsIDs from this.optionObsIDs. optionObsIDs will
		// then only contain obsolete obsIDs
		optionObsIDs.removeAll(keepObsIDs);
		// Now remove all options that have the obsIDs in optionObsIDs.
		// We use the iterator, in order to ensure removing while iterating is
		// possible
		for (Iterator<Option> it = possibleOptions.iterator(); it.hasNext();)
		{
			Option option = it.next();
			// Remove the options that are still in optionObsIDs.
			if(optionObsIDs.contains(option.getObsID()))
			{
				it.remove();
			}
		}
		// Now all options are up-to-date. this.optionObsIDs should be updated
		// to represent the current options list:
		optionObsIDs = newObsIDs;
	}

	/** Adds new obsIDs to newObsIDs and ID's that should be kept to
	 * keepObsIDs, based on the ID's in the ArrayList observations
	 * Also creates options for all new obsIDs in possibleOptions and
	 * optionObsIDs
	 */
	public static void createOptions(ArrayList<Observation>[] observations,
			Lib.GETTER_TYPE type,
			StateObservation so,
			HashSet<Integer> keepObsIDs,
			HashSet<Integer> newObsIDs,
			ArrayList<Option> possibleOptions,
			HashSet<Integer> optionObsIDs)
	{
		// Loop through all types of NPCs
		for(ArrayList<Observation> observationType : observations)
		{
			// Loop through all the NPC's of this type
			for(Observation observation : observationType)
			{
				// This is considered to be a wall
				if(observation.itype == 0)
					continue;
				// Check if this is a new obsID
				if(! optionObsIDs.contains(observation.obsID))
				{
						possibleOptions.add(new UseSwordOnMovableOption(GAMMA, 
							type, observation.itype, observation.obsID, so));
					// Create option for this obsID
					//if(type == Lib.GETTER_TYPE.NPC || type == Lib.GETTER_TYPE.MOVABLE)
					//	possibleOptions.add(new GoToMovableOption(GAMMA, 
					//		type, observation.itype, observation.obsID, so));
					//else if (type == Lib.GETTER_TYPE.NPC_KILL)
					//else
					//	possibleOptions.add(new GoToPositionOption(GAMMA, 
					//		type, observation.itype, observation.obsID, so));
				}
				else
					// Add to the list of options that should be kept
					keepObsIDs.add(observation.obsID);
				newObsIDs.add(observation.obsID);
			}
			//System.out.print("]\n");
		}
	}

	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) 
	{
		// Update options:
		//setOptions(stateObs, this.possibleOptions, this.optionObsIDs);
		System.out.println("Available options: " + this.possibleOptions);
		if(currentOption == null || currentOption.isFinished(stateObs))
		{
			//Set the state observation object as the new root of the tree.
			mctsPlayer.init(stateObs, this.possibleOptions, this.optionObsIDs);

			//Determine the action using MCTS...
			int option = mctsPlayer.run(elapsedTimer);

			//... and return a copy (don't adjust the options in the
			//possibleOption set. This can give trouble later).
			currentOption = this.possibleOptions.get(option).copy();
		}
		Types.ACTIONS action = currentOption.act(stateObs);
		//System.out.println("Using option " + currentOption);
		// System.out.println("Orientation: " + stateObs.getAvatarOrientation());
		// System.out.println("Location: " + stateObs.getAvatarPosition());
		// System.out.println("Action: " + action);
		return action;
	}

}
