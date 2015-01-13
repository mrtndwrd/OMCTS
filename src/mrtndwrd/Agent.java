package mrtndwrd;

import controllers.Heuristics.StateHeuristic;
import controllers.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.game.Observation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * User: mrtndwrd
 * Date: 13-01-2015
 * @author Maarten de Waard
 */
public class Agent extends AbstractPlayer 
{
	private PythonAgent agentClass;

	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) 
	{
		PythonInterpreter pi = new PythonInterpreter();
		pi.exec("import sys");
		pi.exec("sys.path.append('src/mrtndwrd')");
		pi.exec("from agent import Agent");
		pi.exec("print 'hello world'");
		//agentClass = (PythonAgent) pi.get("Agent").__tojava__(PythonAgent.class);
		//agentClass.act();
		/*
		System.out.println("Observation:");
		// Loop through ArrayList<Observation>[][] getObservationGrid()
		for(ArrayList<Observation>[] obsArrayListArray : stateObs.getObservationGrid())
			for(ArrayList<Observation> obsArrayList : obsArrayListArray)
				for(Observation obs : obsArrayList)
					System.out.println(Lib.observationToString(obs));
		System.out.println("End of constructor");
		*/
	}

	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		return Types.ACTIONS.ACTION_NIL;
	}
}
