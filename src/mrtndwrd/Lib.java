package mrtndwrd;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class Lib
{
	public static final double HUGE_NEGATIVE = -10000000.0;
	public static final double HUGE_POSITIVE =  10000000.0;

	public static String observationToString(Observation obs)
	{
		String s = "";
		s += "obsID: " + obs.obsID + "\n";
		s += "cagegory: " + obs.category + "\n";
		s += "itype: " + obs.itype + "\n";
		s += "position: " + obs.position + "\n";
		s += "reference: " + obs.reference + "\n";
		s += "sqDist: " + obs.sqDist + "\n\n";
		return s;
	}

	public static void printObservationGrid(ArrayList<Observation>[][] observationGrid)
	{
		for (int x = 0; x < observationGrid.length; x++)
		{
			for (int y = 0; y<observationGrid[x].length; y++)
			{
				for (Observation obs : observationGrid[x][y])
				{
					System.out.print(obs.itype);
				}
				System.out.print(" ");
			}
			System.out.println();
		}
	}

	/** Very simple state evaluation, taken from the MCTS code */
	public static double simpleValue(StateObservation a_gameState) 
	{
		boolean gameOver = a_gameState.isGameOver();
		Types.WINNER win = a_gameState.getGameWinner();
		double rawScore = a_gameState.getGameScore();

		if(gameOver && win == Types.WINNER.PLAYER_LOSES)
			rawScore += HUGE_NEGATIVE;

		if(gameOver && win == Types.WINNER.PLAYER_WINS)
			rawScore += HUGE_POSITIVE;

		return rawScore;
	}

	/** Returns the distance (sqDist) and the direction (as a string from {up,
	 * upright, right, rightdown, down, downleft, left, leftup}) of the nearest
	 * observation in an array of arraylists of observations. These ArrayList Arrays
	 * come from functions like getNPCPositions()
	 * @param obala Array of ArrayLists of Observations
	 * @param avatarPosition the current position of the avatar, used for
	 * calculating the direction. 
	 * Returns a Tuple of '0., ""' when there is no observation. The empty
	 * direction should help for disambiguating between something that is on the
	 * same spot (which should have '0., "same"')
	 */
	public static Tuple<Double, String> getNearestDistanceAndDirection(
		ArrayList<Observation>[] obala, Vector2d avatarPosition)
	{
		// Get the nearest observation:
		Observation ob = getNearestObservation(obala);
		if(ob == null)
			return new Tuple<Double, String>(0., "");
		// Get the direction of the nearest observation
		String direction = getDirection(ob.position, avatarPosition);
		return new Tuple<Double, String>(ob.sqDist, direction);
	}

	/** Get the nearest observation from an array of ArrayLists of observations,
	 * assuming that all ArrayLists are already ordered. These ArrayList Arrays
	 * come from functions like getNPCPositions()
	 */
	public static Observation getNearestObservation(ArrayList<Observation>[] obala)
	{
		// Sometimes obala isn't instantiated at all!
		if(obala == null)
			return null;
		// Hopefully this initializes to a not-null value
		Observation nearest = null;
		for(ArrayList<Observation> obal : obala)
		{
			// if there's observations in this entry
			if(obal.size() > 0)
				// If we don't have a nearest, make this observation nearest
				if(nearest != null)
					// Take the nearest observation of both
					if(nearest.sqDist > obal.get(0).sqDist)
						nearest = obal.get(0);
				else
					nearest = obal.get(0);
		}
		return nearest;
	}

	/** Returns the direction of the observation position obPosition, relative
	 * to the avatarPosition as a string from {up, upright, right, downright,
	 * down, downleft, left, upleft, same}
	 */
	public static String getDirection(Vector2d obPosition, Vector2d avatarPosition)
	{
		// Same x, either up or down
		if(obPosition.x == avatarPosition.x)
		{
			if(obPosition.y > avatarPosition.y)
				return "up";
			else if(obPosition.y < avatarPosition.y)
				return "down";
		}
		// Same y, either straight right or left
		else if(obPosition.y == avatarPosition.y)
		{
			if(obPosition.x > avatarPosition.x)
				return "right";
			else if(obPosition.x < avatarPosition.x)
				return "left";
		}
		// Different x and y, one of the combinations:
		else
		{
			if(obPosition.x > avatarPosition.x && obPosition.y > avatarPosition.y)
				return "upright";
			else if(obPosition.x > avatarPosition.x && obPosition.y < avatarPosition.y)
				return "downright";
			else if(obPosition.x < avatarPosition.x && obPosition.y > avatarPosition.y)
				return "upleft";
			else if(obPosition.x < avatarPosition.x && obPosition.y < avatarPosition.y)
				return "downleft";
		}
		// x == y
		return "same";
	}

	public static void writeHashMapToFile(HashMap h, String f)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(h);
			oos.close();
			System.out.println("HashMap written to file");
		}
		catch (Exception e)
		{
			System.out.println("Couldn't write the hash map to file!");
			e.printStackTrace();
		}
	}

	public static Object loadObjectFromFile(String f)
	{
		try
		{
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			return ois.readObject();
		}
		catch (Exception e)
		{
			System.out.println("Couldn't load the hash map from file!");
			e.printStackTrace();
			System.out.println("Returning null");
		}
		// if all else fails
		return null;
	}
}
