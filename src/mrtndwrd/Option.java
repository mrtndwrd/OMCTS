package mrtndwrd;

import ontology.Types;
import core.game.StateObservation;

/** An option is a sequence of actions. This class enables taking a sequence of
 * actions and saving its result in the q table, enabling a more high-level
 * approach to reinforcement learning */
public abstract class Option 
{
	
	protected double gamma;
	protected int step;
	protected double cumulativeReward;

	/** Default constructor, creates option, instantiates variables */
	public Option(double gamma)
	{
		this.gamma = gamma;
		this.step = 0;
		// TODO
	}

	/** Choose the action to be taken now */
	public abstract Types.ACTIONS act(StateObservation so);

	/** True if the option is in a goal state */
	public abstract boolean isFinished();
	
	/** Resets the option values, enabling it to be called again next time
	 * without having interfering values saved in it. */
	public abstract void reset();

}
