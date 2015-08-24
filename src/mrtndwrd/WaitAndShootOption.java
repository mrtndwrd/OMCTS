//package mrtndwrd;
//
//import ontology.Types;
//import core.game.StateObservation;
//
//import java.io.Serializable;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.IOException;
//import java.lang.ClassNotFoundException;
//
///** An option that enables the agent to wait until a certain sprite ID
// * (typically an NPC sprite) is in a certain range (somewhere between 0 and 5)
// * and shoots then */
//public class WaitAndShootOption extends Option implements Serializable
//{
//	/** The range from when to shoot. 0 would result in shooting when a sprite
//	 * is in front of the agent, higher than that would result in shooting when
//	 * a sprite is this many blocks away along the axis that the agent is *not*
//	 * oriented in (so the agent is looking right, across the x-axis, the enemy
//	 * would have to be _range_ steps away on the y-axis
//	 */
//	int range = 0;
//
//	/** This option works for this kind of sprite */
//	int itype;
//
//	public WaitAndShootOption(double gamma, int itype, int range)
//	{
//		super(gamma);
//		this.range = range;
//		this.itype = itype;
//	}
//
//	public Types.ACTIONS act(StateObservation so)
//	{
//		
//	}
//
//	protected boolean isEnemyInDangerZone(StateObservation so)
//	{
//		Vector2d avatarOrientation = so.getAvatarOrientation();
//		ArrayList<Observation> npcPositions = so.getNPCPosition();
//		// TODO
//		if(avatarOrientation[0]
//	}
//
//}
