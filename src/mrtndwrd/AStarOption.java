package mrtndwrd;

import ontology.Types;
import core.game.StateObservation;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

/** Option that represents going somewhere using aStar. The static aStar
 * variable in AbstractAgent is used. */
public class AStarOption extends Option implements Serializable
{
	private Vector2d goal;
	ArrayList<Tuple<Integer, Integer>> currentPath;

	public AStarOption(double gamma, Vector2d goal)
	{
		super(gamma);
		this.goal = goal;
		this.currentPath = new ArrayList<Tuple<Integer, Integer>>();
	}

	public Types.ACTIONS act(StateObservation so)
	{

	}

	public void reset()
	{
		this.step = 0;
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
		return new AStarOption(gamma, goal);
	}

	public String toString()
	{
		return "AStarOption(" + this.goal + ")";
	}

	public int hashCode()
	{
		return this.goal.hashCode();
	}

	public boolean equals(Object o)
	{
		if(o instanceof AStarOption)
		{
			AStarOption oa = (AStarOption) o;
			return this.goal== oa.goal;
		}
		return false;
	}
}
