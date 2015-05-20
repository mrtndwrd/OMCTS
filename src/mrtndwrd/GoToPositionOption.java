package mrtndwrd;

import ontology.Types;
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
	
	/** The last planned path, as long as this is followed, no replanning has to
	 * be done */
	ArrayList<SerializableTuple<Integer, Integer>> currentPath;

	public GoToPositionOption(double gamma, Vector2d goal)
	{
		this(gamma, AbstractAgent.aStar.vectorToBlock(goal));
	}

	public GoToPositionOption(double gamma, SerializableTuple<Integer, Integer> goal)
	{
		super(gamma);
		this.goal = goal;
		this.currentPath = new ArrayList<SerializableTuple<Integer, Integer>>();
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
		SerializableTuple<Integer, Integer> avatarPosition = AbstractAgent.aStar.vectorToBlock(so.getAvatarPosition());
		// Do nothing if we're already on the goal
		if(avatarPosition.equals(this.goal))
		{
			this.step++;
			return Types.ACTIONS.ACTION_NIL;
		}

		int index = currentPath.indexOf(avatarPosition);
		if(index < 0)
		{
			// Plan a new path
			this.currentPath = AbstractAgent.aStar.aStar(avatarPosition, goal);
			// current location is at the end of the path
			index = currentPath.size()-1;
		}
		// Increase step counter to keep track of everything.
		this.step++;
		//System.out.println("Increasing step in GoToPositionOption to " + step + ", goal: " + this.goal + ", position: " + avatarPosition);
		//System.out.printf("Using path %s at position %s\n", currentPath, avatarPosition);
		//System.out.printf("Returning action %s to get from %s to %s\n", 
		//		AbstractAgent.aStar.neededAction(avatarPosition, currentPath.get(index -1)),
		//		avatarPosition,
		//		AbstractAgent.aStar.vectorToBlock(goal));

		// Return the action that is needed to get to the next path index.
		return AbstractAgent.aStar.neededAction(avatarPosition, currentPath.get(index -1));
	}

	/** This option is finished if the avatar's position is the same as
	 * the goal location or if the game has ended (avatarPosition = -1, -1)
	 */
	public boolean isFinished(StateObservation so)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		// Option is also finished when the game is over
		if(avatarPosition.x == -1 && avatarPosition.y == -1)
		{
			return true;
		}
		return this.goal == AbstractAgent.aStar.vectorToBlock(avatarPosition);
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
