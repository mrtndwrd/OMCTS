package mrtndwrd;

import core.game.StateObservation;
import tools.Vector2d;
import java.util.Arrays;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.io.IOException;

/** Class that enables simple hashing with state observation abstraction. It is
 * possible that two SimplifiedObservations are seen as equal, while the same
 * StateObservation would have been unequal */
public class SimplifiedObservation implements Serializable
{
	/** The hash code of all stuff in here
	 * @Serial
	 */
	int code = 0;

	private static final long serialVersionUID = 4382473219407506572L;

	public SimplifiedObservation(StateObservation so)
	{
		// Initialize with some data. Later more initialize functions could be
		// added
		//someDataInit(so);
		preyInit(so);
		//onlyAvatarPositionInit(so);
	}

	/** Initialize using some of the observed data. Excluded are: The entire
	 * observation grid, getFromAvatarSprites and getImmovablePositions */
	private void someDataInit(StateObservation so)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		code = 3 * code + avatarPosition.toString().hashCode();
		code = 5 * code + so.getAvatarOrientation().toString().hashCode();
		// avatarResources are a HashMap<int, int>. This hashCode should work
		// fine...
		code = 7 * code + so.getAvatarResources().hashCode();
		code = 11 * code + Lib.getNearestDistanceAndDirection(
			so.getNPCPositions(avatarPosition), avatarPosition).hashCode();
		code = 13 * code + Lib.getNearestDistanceAndDirection(
			so.getMovablePositions(avatarPosition), avatarPosition).hashCode();
		code = 17 * code + Lib.getNearestDistanceAndDirection(
			so.getResourcesPositions(avatarPosition), avatarPosition).hashCode();
		code = 19 * code + Lib.getNearestDistanceAndDirection(
			so.getPortalsPositions(avatarPosition), avatarPosition).hashCode();
		// apparantly the key in zelda is an immovable position...
		code = 23 * code + Lib.getNearestDistanceAndDirection(
			so.getImmovablePositions(avatarPosition), avatarPosition).hashCode();
	}

	/** Simplified init for the prey testing game */
	private void preyInit(StateObservation so)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		code = 3 * code + avatarPosition.toString().hashCode();
		code = 11 * code + Lib.getNearestDistanceAndDirection(
			so.getNPCPositions(avatarPosition), avatarPosition).hashCode();
		/*
		System.out.printf("Position: %s, Prey: %s, code: %d\n", 
			avatarPosition.toString(),
			Lib.getNearestDistanceAndDirection(
				so.getNPCPositions(avatarPosition), avatarPosition).toString(),
			this.code);
		*/
	}

	private void onlyAvatarPositionInit(StateObservation so)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		code = 3 * code + avatarPosition.toString().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		// If it's exactly the same
		if (o == this) 
			return true; 
		// Any class except for a StateObservation cannot be equal
		if (o == null || o.getClass() != this.getClass()) 
		{ 
			if(o instanceof StateObservation)
				return equals(new SimplifiedObservation((StateObservation) o));
			else
				return false; 
		}
		// Compare hashcodes
		return o.hashCode() == code;
	}

	@Override
	public int hashCode()
	{
		return code;
	}

	private void readObject(ObjectInputStream aInputStream) 
		throws ClassNotFoundException, IOException 
	{
		//always perform the default de-serialization first
		aInputStream.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream aOutputStream)
		throws IOException 
	{
		// perform the default serialization for all non-transient, non-static
		// fields
		aOutputStream.defaultWriteObject();
	}
}
