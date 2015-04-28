package mrtndwrd;

import ontology.Types;
import core.game.StateObservation;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

/** Option that represents (one of the) normal actions */
public class ActionOption extends Option implements Serializable
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
	}

	protected void readObject(ObjectInputStream aInputStream) 
		throws ClassNotFoundException, IOException 
	{
		//always perform the default de-serialization first
		super.readObject(aInputStream);
		action = (Types.ACTIONS) aInputStream.readObject();
	}

	protected void writeObject(ObjectOutputStream aOutputStream)
		throws IOException 
	{
		// perform the default serialization for all non-transient, non-static
		// fields
		aOutputStream.defaultWriteObject();
		aOutputStream.writeObject(action);
	}

	@Override
	public Option copy()
	{
		return new ActionOption(gamma, action);
	}

}