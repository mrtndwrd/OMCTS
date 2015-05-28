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
public class GoToMovableOption extends GoToPositionOption implements Serializable
{

	public GoToMovableOption(double gamma, Lib.GETTER_TYPE type, int index, 
			int obsID, StateObservation so)
	{
		super(gamma, type, index, obsID, so);
	}

	/** Constructor mainly for use by copy. By supplying the current goal
	 * position, the need for a StateObservation vanishes. */
	public GoToMovableOption(double gamma, Lib.GETTER_TYPE type, int index, 
			int obsID, SerializableTuple<Integer, Integer> goal)
	{
		super(gamma, type, index, obsID, goal);
	}

	/** Returns the next action to get to this.goal. This function only plans
	 * around walls, not around other objects or NPC's. Only plans a new path if
	 * the current location is not anymore in the currentPath. 
	 */
	@Override
	public Types.ACTIONS act(StateObservation so)
	{
		// Save the calculation hassle if the goal is already set to null
		// unfortunately this speed-hack creates redundancy the rest of this
		// function
		if(this.goal == null && this.step > 0)
		{
			System.out.printf("Goal = null, step = %d, this = %s\n", step, this);
			this.step++;
			return Types.ACTIONS.ACTION_NIL;
		}
		// Get the location of the goal:
		SerializableTuple<Integer, Integer> goalLocation = getGoalLocationFromSo(so);
		// Check if the goal location is still the same:
		if(this.goal == null || (goalLocation != null && !this.goal.equals(goalLocation)))
		{
			// Set the new goal location
			this.goal = goalLocation;
			// Remove the old path
			this.currentPath = new ArrayList<SerializableTuple<Integer, Integer>>();
		}
		// Check if the goal still exists (for example, NPC's can be killed) 
		// else return ACTION_NIL
		if(this.goal == null)
		{
			this.step++;
			return Types.ACTIONS.ACTION_NIL;
		}
		// Plan the path
		return super.act(so);
	}

	public void reset()
	{
		super.reset();
		this.obsID = -1;
		this.index = -1;
		this.type = null;
	}

	@Override
	public Option copy()
	{
		return new GoToMovableOption(gamma, type, index, obsID, goal);
	}

	public String toString()
	{
		return String.format("GoToMovableOption(%s,%d,%d)", type, index, obsID);
	}

	public int hashCode()
	{
		int hash = 1;
		hash = hash * 37 + this.type.hashCode();
		hash = hash * 41 + this.index;
		hash = hash * 43 + this.obsID;
		return hash;
	}

	public boolean equals(Object o)
	{
		if(o instanceof GoToMovableOption)
		{
			GoToMovableOption oa = (GoToMovableOption) o;
			return this.type.equals(oa.type) && 
				this.index == oa.index && this.obsID == obsID;
		}
		return false;
	}
}
