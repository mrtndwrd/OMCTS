package mrtndwrd;

import ontology.Types;
import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.util.ArrayList;

/** Option that represents planning a route using aStar.
 * The route is planned between walls, enemies and objects are not taken into
 * account. The static aStar variable in AbstractAgent is used. 
 */
public class GoToPositionOption extends Option implements Serializable
{
	/** The goal position of this option */
	protected SerializableTuple<Integer, Integer> goal;

	/** Specifies if this follows an NPC or a movable non-npc sprite */
	protected Lib.GETTER_TYPE type;

	/** Specifies the index in the getter of this type, e.g. getNPCPositions */
	protected int index;

	/** If this is true, the goal is a sprite that can be removed from the game.
	 * That means that this option is not possible anymore. If this is false,
	 * the goal is just an x/y location which is always possible to go to */
	protected boolean goalIsSprite;
	
	/** The last planned path, as long as this is followed, no replanning has to
	 * be done */
	ArrayList<SerializableTuple<Integer, Integer>> currentPath = new ArrayList<SerializableTuple<Integer, Integer>>();

	/** Initialize with a position to go to */
	public GoToPositionOption(double gamma, SerializableTuple<Integer, Integer> goal)
	{
		super(gamma);
		this.goal = goal;
		this.goalIsSprite = false;
	}

	/** Initialize with a position to go to. Goal is converted to block
	 * coordinates */
	public GoToPositionOption(double gamma, Vector2d goal)
	{
		this(gamma, Agent.aStar.vectorToBlock(goal));
	}

	/** Initialize with something that has to be followed */
	public GoToPositionOption(double gamma, Lib.GETTER_TYPE type, int index, int obsID, StateObservation so)
	{
		super(gamma);
		this.type = type;
		this.index = index;
		this.obsID = obsID;
		this.goal = getGoalLocationFromSo(so);
		this.goalIsSprite = true;
	}

	/** Initialize with something that has to be followed, including setting the
	 * goal location (so no StateObservation is needed) */
	public GoToPositionOption(double gamma, Lib.GETTER_TYPE type, int index, int obsID, SerializableTuple<Integer, Integer> goal)
	{
		super(gamma);
		this.type = type;
		this.index = index;
		this.obsID = obsID;
		this.goal = goal;
		this.goalIsSprite = true;
	}

	/** "Empty" constructor. Only use this if you'll set the goal in a subclass!
	 */
	protected GoToPositionOption(double gamma)
	{
		super(gamma);
		this.currentPath = new ArrayList<SerializableTuple<Integer, Integer>>();
	}

	/** Returns the next action to get to this.goal. This function only plans
	 * around walls, not around other objects or NPC's. Only plans a new path if
	 * the current location is not anymore in the currentPath. 
	 */
	public Types.ACTIONS act(StateObservation so)
	{
		if(!goalExists(so))
		{
			removeGoal();
		}
		SerializableTuple<Integer, Integer> avatarPosition = Agent.aStar.vectorToBlock(so.getAvatarPosition());
		// Do nothing if we're already on the goal
		if(this.goal == null || avatarPosition.equals(this.goal))
		{
			this.step++;
			return Types.ACTIONS.ACTION_NIL;
		}

		int index = currentPath.indexOf(avatarPosition);
		if(index < 0)
		{
			// Plan a new path
			this.currentPath = Agent.aStar.aStar(avatarPosition, goal);
			// current location is at the end of the path
			index = currentPath.size()-1;
		}
		// Increase step counter to keep track of everything.
		this.step++;
		if(currentPath.size() == 0 || index < 0)
		{
			// No path available, remove the goal.
			return Types.ACTIONS.ACTION_NIL;
		}
		//System.out.println("Increasing step in GoToPositionOption to " + step + ", goal: " + this.goal + ", position: " + avatarPosition);
		//System.out.printf("Using path %s at position %s\n", currentPath, avatarPosition);
		//System.out.printf("Returning action %s to get from %s to %s\n", 
		//		Agent.aStar.neededAction(avatarPosition, currentPath.get(index -1)),
		//		avatarPosition,
		//		goal);

		// Return the action that is needed to get to the next path index.
		return Agent.aStar.neededAction(avatarPosition, currentPath.get(index -1));
	}

	protected void removeGoal()
	{
		this.goal = null;
		this.currentPath = new ArrayList<SerializableTuple<Integer, Integer>>();
	}

	/** This option is finished if the avatar's position is the same as
	 * the goal location or if the game has ended (avatarPosition = -1, -1)
	 */
	public boolean isFinished(StateObservation so)
	{
		// This class might have made this.goal null because the observation ID
		// does not exist anymore
		if (!this.goalExists(so) || currentPath.size() == 0)
			return true;
		Vector2d avatarPosition = so.getAvatarPosition();
		// Option is also finished when the game is over
		if(avatarPosition.x == -1 && avatarPosition.y == -1)
		{
			return true;
		}
		return this.goal == Agent.aStar.vectorToBlock(avatarPosition);
	}

	/** Returns the location of the thing that is tracked, based on type, index
	 * and obsID */
	protected SerializableTuple<Integer, Integer> getGoalLocationFromSo(StateObservation so)
	{
		ArrayList<Observation> observations;
		observations = getObservations(so, index);
		for (Observation o : observations)
		{
			if(o.obsID == this.obsID)
				return Agent.aStar.vectorToBlock(o.position);
		}
		//System.out.printf("WARNING: obsID %d not found!\n", this.obsID);
		// Probably this obs is already eliminated.
		return null;
	}

	/** Returns the observations from the right getter, according to this.type
	 * @param index the index inside the getter, of the observation type that is
	 * requested
	 */
	protected ArrayList<Observation> getObservations(StateObservation so, int index)
	{
		if(this.type == Lib.GETTER_TYPE.NPC)
			return so.getNPCPositions()[index];
		if(this.type == Lib.GETTER_TYPE.MOVABLE)
			return so.getMovablePositions()[index];
		if(this.type == Lib.GETTER_TYPE.IMMOVABLE)
			return so.getImmovablePositions()[index];
		if(this.type == Lib.GETTER_TYPE.RESOURCE)
			return so.getResourcesPositions()[index];
		if(this.type == Lib.GETTER_TYPE.PORTAL)
			return so.getPortalsPositions()[index];
		System.err.printf("WARNING: Type %s NOT known in %s!\n", this.type, this);
		return null;
	}

	public boolean goalExists(StateObservation so)
	{
		if(goalIsSprite && getGoalLocationFromSo(so) == null)
		{
			// Goal doesn't exist anymore, remove it.
			removeGoal();
			return false;
		}
		return true;
	}


	public void reset()
	{
		this.step = 0;
		this.goal = null;
		this.currentPath = new ArrayList<SerializableTuple<Integer, Integer>>();
	}

	protected void readObject(ObjectInputStream aInputStream) 
		throws ClassNotFoundException, IOException 
	{
		//always perform the default de-serialization first
		super.readObject(aInputStream);
	}

	protected void writeObject(ObjectOutputStream aOutputStream)
		throws IOException 
	{
		// perform the default serialization for all non-transient, non-static
		// fields
		aOutputStream.defaultWriteObject();
	}

	@Override
	public Option copy()
	{
		return new GoToPositionOption(gamma, goal);
	}

	public String toString()
	{
		return "GoToPositionOption(" + this.goal + ")";
	}

	public int hashCode()
	{
		return this.goal.hashCode();
	}

	public boolean equals(Object o)
	{
		if(o instanceof GoToPositionOption)
		{
			GoToPositionOption oa = (GoToPositionOption) o;
			return this.goal.equals(oa.goal);
		}
		return false;
	}
}
