package mrtndwrd;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;

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
		//for(ArrayList<Observation>[] obsArrayListArray : stateObs.getObservationGrid())
		//	for(ArrayList<Observation> obsArrayList : obsArrayListArray)
		//		for(Observation obs : obsArrayList)
		//			System.out.println(Lib.observationToString(obs));
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
}
