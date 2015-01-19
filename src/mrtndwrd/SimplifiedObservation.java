package mrtndwrd;

import core.game.StateObservation;
import tools.Vector2d;

/** Class that enables simple hashing with state observation abstraction. It is
 * possible that two SimplifiedObservations are seen as equal, while the same
 * StateObservation would have been unequal */
public class SimplifiedObservation
{
	int code = 0;

	public SimplifiedObservation(StateObservation so)
	{
		// Initialize with some data. Later more initialize functions could be
		// added
		someDataInit(so);
	}

	/** Initialize using some of the observed data. Excluded are: The entire
	 * observation grid, getFromAvatarSprites and getImmovablePositions */
	private void someDataInit(StateObservation so)
	{
		// FIXME: Should check for NULL
		Vector2d avatarPosition = so.getAvatarPosition();
		code = 3 * code + avatarPosition.hashCode();
		code = 5 * code + so.getAvatarOrientation().hashCode();
		code = 7 * code + so.getAvatarResources().hashCode();
		// Get NPC positions, ordered by distance from the PC
		code = 11 * code + so.getNPCPositions(avatarPosition).hashCode();
		//code = 13 * code + so.getMovablePositions(avatarPosition).hashCode();
		//code = 17 * code + so.getResourcesPositions(avatarPosition).hashCode();
		code = 19 * code + so.getPortalsPositions(avatarPosition).hashCode();
		// apparantly the key in zelda is an immovable position...
		code = 23 * code + so.getImmovablePositions(avatarPosition).hashCode();
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
}
