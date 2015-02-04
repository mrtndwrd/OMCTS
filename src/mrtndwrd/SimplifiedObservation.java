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
	private String code = "";

	private static final long serialVersionUID = 4382473219407506572L;

	public SimplifiedObservation(StateObservation so, AStar aStar)
	{
		// Initialize with some data. Later more initialize functions could be
		// added
		betterAStarDataInit(so, aStar);
		//aStarDataInit(so, aStar);
		//someDataInit(so);
		//preyInit(so);
		//preyAStarInit(so, aStar);
		//onlyAvatarPositionInit(so);
	}

	private void betterAStarDataInit(StateObservation so, AStar aStar)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		code = String.format("Orientation: %s\n Resources: %s\n NPCs: %s\n Movable: %s\n ResourcePositions: %s\n PortalsPositions: %s\n ImmovablePositions: %s",
			so.getAvatarOrientation().toString(),
			so.getAvatarResources().toString(),
			Lib.getPathLengthAndAStarAction(
				so.getNPCPositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getPathLengthAndAStarAction(
				so.getMovablePositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getPathLengthAndAStarAction(
				so.getResourcesPositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getPathLengthAndAStarAction(
				so.getPortalsPositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getPathLengthAndAStarAction(
				so.getImmovablePositions(avatarPosition), avatarPosition, aStar).toString());
	}

	private void aStarDataInit(StateObservation so, AStar aStar)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		code = String.format("Orientation: %s\n Resources: %s\n NPCs: %s\n Movable: %s\n ResourcePositions: %s\n PortalsPositions: %s\n ImmovablePositions: %s",
			so.getAvatarOrientation().toString(),
			so.getAvatarResources().toString(),
			Lib.getNearestDistanceAndAStarAction(
				so.getNPCPositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getNearestDistanceAndAStarAction(
				so.getMovablePositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getNearestDistanceAndAStarAction(
				so.getResourcesPositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getNearestDistanceAndAStarAction(
				so.getPortalsPositions(avatarPosition), avatarPosition, aStar).toString(),
			Lib.getNearestDistanceAndAStarAction(
				so.getImmovablePositions(avatarPosition), avatarPosition, aStar).toString());
	}

	/** Initialize using some of the observed data. Excluded are: The entire
	 * observation grid, getFromAvatarSprites and getImmovablePositions */
	private void someDataInit(StateObservation so)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		System.out.printf("avatarPosition: %s, divided by block size: %f, %f\n",
			avatarPosition, avatarPosition.x/so.getBlockSize(), avatarPosition.y/so.getBlockSize());
		code = String.format("Orientation: %s\n Resources: %s\n NPCs: %s\n Movable: %s\n ResourcePositions: %s\n PortalsPositions: %s\n ImmovablePositions: %s",
			so.getAvatarOrientation().toString(),
			so.getAvatarResources().toString(),
			Lib.getNearestDistanceAndDirection(
				so.getNPCPositions(avatarPosition), avatarPosition).toString(),
			Lib.getNearestDistanceAndDirection(
				so.getMovablePositions(avatarPosition), avatarPosition).toString(),
			Lib.getNearestDistanceAndDirection(
				so.getResourcesPositions(avatarPosition), avatarPosition).toString(),
			Lib.getNearestDistanceAndDirection(
				so.getPortalsPositions(avatarPosition), avatarPosition).toString(),
			Lib.getNearestDistanceAndDirection(
				so.getImmovablePositions(avatarPosition), avatarPosition).toString());
	}

	/** Simplified init for the prey testing game */
	private void preyInit(StateObservation so)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		code = String.format("Prey: %s", 
			Lib.getNearestDistanceAndDirection(
				so.getNPCPositions(avatarPosition), avatarPosition).toString());
		/*
		System.out.printf("Position: %s, Prey: %s, code: %d\n", 
			avatarPosition.toString(),
			Lib.getNearestDistanceAndDirection(
				so.getNPCPositions(avatarPosition), avatarPosition).toString(),
			this.code);
		*/
	}

	private void preyAStarInit(StateObservation so, AStar aStar)
	{
		Vector2d avatarPosition = so.getAvatarPosition();
		code = String.format("Orientation: %s, Prey: %s", 
			so.getAvatarOrientation().toString(),
			Lib.getPathLengthAndAStarAction(
				so.getNPCPositions(avatarPosition), avatarPosition, aStar).toString());
	}

	private void onlyAvatarPositionInit(StateObservation so)
	{
		code = so.getAvatarPosition().toString();
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
			{
				return equals(new SimplifiedObservation((StateObservation) o, null));
			}
			else
				return false; 
		}
		// Same class, compare hashcodes 
		return o.hashCode() == this.hashCode();
	}

	public String getCode() 
	{
		return this.code;
	}

	@Override
	public int hashCode()
	{
		return code.hashCode();
	}

	@Override
	public String toString()
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
