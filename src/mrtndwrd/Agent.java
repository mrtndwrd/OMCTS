package mrtndwrd;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

	public static int ROLLOUT_DEPTH = 10;
	public static double K = Math.sqrt(2);
	public ArrayList<Option> possibleOptions = new ArrayList<Option>();

	/** AStar for searching for stuff */
	public static AStar aStar;

	/** Random generator for the agent. */
	private SingleMCTSPlayer mctsPlayer;
		
	/** list of actions for random action selection in rollout */
	public static Types.ACTIONS[] actions;

	/** The gamma of this algorithm */
	public final double GAMMA = .9;

	/**
	 * Public constructor with state observation and time due.
	 * @param so state observation of the current game.
	 * @param elapsedTimer Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		ArrayList<Types.ACTIONS> act = so.getAvailableActions();
		
		// Add the actions to the option set
		setOptionsForActions(act);
		
		// Create actions for rollout
		actions = new Types.ACTIONS[act.size()];
		for(int i = 0; i < actions.length; ++i)
		{
			actions[i] = act.get(i);
		}

		//Create the player.
		mctsPlayer = new SingleMCTSPlayer(new Random());
		Agent.aStar = new AStar(so);
	}

	protected void setOptionsForActions(ArrayList<Types.ACTIONS> actions)
	{
		for(Types.ACTIONS action : actions)
		{
			this.possibleOptions.add(new ActionOption(GAMMA, action));
		}
	}

	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		//Set the state observation object as the new root of the tree.
		mctsPlayer.init(stateObs, possibleOptions);

		//Determine the action using MCTS...
		int action = mctsPlayer.run(elapsedTimer);

		//... and return it.
		return actions[action];
	}

}
