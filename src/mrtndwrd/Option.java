package mrtndwrd;

import ontology.Types;
import core.game.StateObservation;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

/** An option is a sequence of actions. This class enables taking a sequence of
 * actions and saving its result in the q table, enabling a more high-level
 * approach to reinforcement learning */
public abstract class Option implements Serializable
{
	
	protected double gamma;
	protected int step;
	protected double cumulativeReward;
	/** If this option has no functionality anymore, we mark it as "finished".
	 * This includes when the destination is reached, when the goal is gone, or
	 * when there is no path to a goal. All actions returned after being
	 * "finished" are ACTION_NIL */
	protected boolean finished;

	/** Specifies the obsID of the movable/npc that is tracked by this option */
	protected int obsID = -1;

	/** Default constructor, creates option, instantiates variables */
	public Option(double gamma)
	{
		this.gamma = gamma;
		this.step = 0;
		this.cumulativeReward = 0;
	}

	/** Choose the action to be taken now. This should increment this.step! */
	public abstract Types.ACTIONS act(StateObservation so);

	/** True if the option so is a goal state */
	public abstract boolean isFinished(StateObservation so);

	/** True if the goal of this option still exists. For example a
	 * GoToMovableOption where the movable has been removed from the game would
	 * return false.
	 * By default this returns true, since simple options always have an
	 * existing goal
	 */
	public boolean goalExists(StateObservation so)
	{
		// For now, I wouldn't know how to check if it still exists, so just
		// return true:
		return true;
	}

	/** If this option has an obsID of an observation it tracks, that can be
	 * returned by this function. If there's no obsID (default) 'null' will be
	 * returned */
	public int getObsID()
	{
		return this.obsID;
	}
	
	/** Resets the option values, enabling it to be called again next time
	 * without having interfering values saved in it. */
	public abstract void reset();

	public abstract Option copy();

	/** Adds (discounted) reward to the cumulative reward. No need to discount
	 * in advance, that's done by this function */
	public void addReward(double reward)
	{
		if(step == 0)
			System.err.printf("WARNING! Adding reward to option that hasn't done anything yet! %s\n", this);
		this.cumulativeReward += Math.pow(gamma, step-1) * reward;
		// if(reward != 0)
			// System.out.printf("Set cumulative reward to %f\n", this.cumulativeReward);
	}

	public double getReward()
	{
		//if(cumulativeReward != 0)
		//	System.out.printf("Returning cumulative reward %f\n", this.cumulativeReward);
		return this.cumulativeReward;
	}

	/** Returns the step count of this option, representing how long the option
	 * has been in use
	 */
	public int getStep()
	{
		return this.step;
	}

	/** Sets this.finished to true */
	protected void setFinished()
	{
		this.finished = true;
	}


	protected void readObject(ObjectInputStream aInputStream) 
		throws ClassNotFoundException, IOException 
	{
		//always perform the default de-serialization first
		aInputStream.defaultReadObject();
	}

	protected void writeObject(ObjectOutputStream aOutputStream)
		throws IOException 
	{
		// perform the default serialization for all non-transient, non-static
		// fields
		aOutputStream.defaultWriteObject();
	}

	public abstract int hashCode();

	public abstract boolean equals(Object o);


}
