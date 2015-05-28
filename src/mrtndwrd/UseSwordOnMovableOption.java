package mrtndwrd;

import ontology.Types;
import core.game.StateObservation;
import core.game.Observation;
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
public class UseSwordOnMovableOption extends GoToMovableOption implements Serializable
{
	public UseSwordOnMovableOption(double gamma, Lib.GETTER_TYPE type, int index, int obsID, StateObservation so)
	{
		super(gamma, type, index, obsID, so);
	}

	

}
