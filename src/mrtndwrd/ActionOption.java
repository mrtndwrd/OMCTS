package mrtndwrd;

import ontology.Types;
import core.game.StateObservation;

/** Option that represents (one of the) normal actions */
public class ActionOption extends Option
{
	/** The action that will (always) be taken by this option */
	private Types.ACTIONS action;

	public ActionOption(double gamma, Types.ACTIONS action)
	{
		super(gamma);
		this.action = action;
	}

	/** Return the action that this option represents */
	public Types.ACTIONS act(StateObservation so)
	{
		// Set finished to true, since this option has to take the action once
		// and then will be finished
		this.step++;
		return this.action;
	}

	public boolean isFinished()
	{
		return this.step > 0;
	}

	/** Reset all values */
	public void reset()
	{
		this.step = 0;
		this.finished = false;
	}
}
