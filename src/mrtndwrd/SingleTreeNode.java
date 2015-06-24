package mrtndwrd;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;

public class SingleTreeNode
{
	private static final double HUGE_NEGATIVE = -10000000.0;

	private static final double HUGE_POSITIVE =  10000000.0;

	/** mctsSearch continues until there are only so many miliseconds left */
	public static final int REMAINING_LIMIT = 3;

	public static double epsilon = 1e-6;

	public static double egreedyEpsilon = 0.05;

	public StateObservation state;

	public SingleTreeNode parent;

	public SingleTreeNode[] children;

	public double totValue;

	public int nVisits;

	private ArrayList<Option> possibleOptions;

	private HashSet<Integer> optionObsIDs;

	/** The option that is chosen in this node. This option is followed until it
	 * is finished, thereby representing a specific subtree in the whole */
	private Option chosenOption;

	public static Random random;
	/** The depth in the rollout of this node (initialized as parent.node+1) */
	public int nodeDepth;

	protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

	/** Root node constructor */
	public SingleTreeNode(ArrayList<Option> possibleOptions, HashSet<Integer> optionObsIDs, Random rnd) 
	{
		this(null, null, null, possibleOptions, optionObsIDs, rnd);
	}

	/** normal constructor */
	public SingleTreeNode(StateObservation state, SingleTreeNode parent, Option chosenOption, ArrayList<Option> possibleOptions, HashSet<Integer> optionObsIDs, Random rnd)
	{
		this.state = state;
		this.parent = parent;
		this.random = rnd;
		this.possibleOptions = possibleOptions;
		this.optionObsIDs = optionObsIDs;
		// Create the possibility of chosing new options
		if(chosenOption == null || chosenOption.isFinished(state) || Agent.OPTION_BREAKING)
		{
			children = new SingleTreeNode[possibleOptions.size()];
			this.chosenOption = null;
		}
		// The only child is the continuation of this option.
		else
		{
			children = new SingleTreeNode[1];
			this.chosenOption = chosenOption;
		}

		totValue = 0.0;
		if(parent != null)
			nodeDepth = parent.nodeDepth+1;
		else
			nodeDepth = 0;
	}

	public void mctsSearch(ElapsedCpuTimer elapsedTimer) 
	{
		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long remaining = elapsedTimer.remainingTimeMillis();
		int numIters = 0;

		while(remaining > 2*avgTimeTaken && remaining > REMAINING_LIMIT)
		{
			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
			// Select the node to explore (either expanding unexpanded node, or
			// selecting the best one with UCT)
			//System.out.printf("Remaining before treePolicy: %d\n", elapsedTimer.remainingTimeMillis());
			SingleTreeNode selected = treePolicy();
			// Get node value using a max-depth rollout
			//System.out.printf("Remaining before rollOut: %d\n", elapsedTimer.remainingTimeMillis());
			double delta = selected.rollOut();
			// Set values for parents of current node, using new rollout value
			//System.out.printf("Remaining before rollOut: %d\n", elapsedTimer.remainingTimeMillis());
			backUp(selected, delta);
			
			//System.out.printf("Remaining after backUp: %d\n", elapsedTimer.remainingTimeMillis());

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;

			avgTimeTaken  = acumTimeTaken/numIters;
			remaining = elapsedTimer.remainingTimeMillis();
			//System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
		}
		//System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
	}

	/** Expand the current treenode, if it's not fully expanded. Else, return
	 * the best node using uct
	 */
	public SingleTreeNode treePolicy() 
	{
		SingleTreeNode cur = this;

		while (!cur.state.isGameOver() && cur.nodeDepth < Agent.ROLLOUT_DEPTH)
		{
			// TODO: This always fully expands, we don't necessarily want
			// that...
			if (cur.notFullyExpanded()) 
			{
				return cur.expand();
			} 
			else 
			{
				SingleTreeNode next = cur.uct();
				//SingleTreeNode next = cur.egreedy();
				cur = next;
			}
		}

		return cur;
	}


	public SingleTreeNode expand() 
	{
		Option nextOption;
		int bestOption = 0;
		// If there's no chosenOption, we'll have to choose a new one
		if(this.chosenOption == null)
		{
			double bestValue = -1;

			// Select random option with index that isn't taken yet.
			for (int i = 0; i < children.length; i++) 
			{
				double x = random.nextDouble();
				if (x > bestValue && children[i] == null) 
				{
					bestOption = i;
					bestValue = x;
				}
			}
			nextOption = this.possibleOptions.get(bestOption).copy();
		}
		// Else, this node will just expand the chosenOption into child 0 (its
		// only child) until it's done! 
		else
		{
			bestOption = 0;
			nextOption = chosenOption.copy();
		}

		StateObservation nextState = state.copy();
		Types.ACTIONS action = nextOption.act(nextState);
		// Step 1: Follow the option:
		nextState.advance(action);
		// Step 2: get the new option set
		ArrayList<Option> newOptions = (ArrayList<Option>) this.possibleOptions.clone();
		HashSet<Integer> newOptionObsIDs = (HashSet<Integer>) this.optionObsIDs.clone();
		Agent.setOptions(nextState, newOptions, newOptionObsIDs);
		// Step 3: create a child node
		SingleTreeNode tn = new SingleTreeNode(nextState, this, nextOption, newOptions, newOptionObsIDs, this.random);
		children[bestOption] = tn;
		// Step 4: Check if the walls are still up to date
		AStar.checkForWalls(state, action, nextState);
		return tn;
	}


	private void runOption(StateObservation nextState, Option option)
	{
		while(!option.isFinished(nextState))
		{
			nextState.advance(option.act(nextState));
		}
	}

	public SingleTreeNode uct() 
	{
		// For speeding up the situration where an option is being followed, and
		// just 1 child exists
		if(this.children.length == 1)
			return this.children[0];

		SingleTreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;
		for (SingleTreeNode child : this.children)
		{
			double hvVal = child.totValue;
			double childValue =  hvVal / (child.nVisits + this.epsilon);

			childValue = Utils.normalise(childValue, bounds[0], bounds[1]);

			double uctValue = childValue +
					Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon));

			// small sampleRandom numbers: break ties in unexpanded nodes
			uctValue = Utils.noise(uctValue, this.epsilon, this.random.nextDouble());	 //break ties randomly

			// small sampleRandom numbers: break ties in unexpanded nodes
			if (uctValue > bestValue) 
			{
				selected = child;
				bestValue = uctValue;
			}
		}

		if (selected == null)
		{
			throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length);
		}

		return selected;
	}

	/** Perform a rollout with random actions on the current node of maximally
	 * Agent.ROLLOUT_DEPTH. 
	 * @return Delta is the "simpleValue" of the last state the rollOut
	 * arrives in. 
	 */
	public double rollOut()
	{
		StateObservation rollerState = state.copy();
		int thisDepth = this.nodeDepth;

		while (!finishRollout(rollerState,thisDepth)) 
		{
			Types.ACTIONS action;
			// First, follow this node's option, then follow a random policy
			if(chosenOption != null && !chosenOption.isFinished(rollerState))
				action = chosenOption.act(rollerState);
			else
				action = Agent.actions[random.nextInt(Agent.actions.length)];
			rollerState.advance(action);
			thisDepth++;
		}

		double delta = Lib.simpleValue(rollerState);

		if(delta < bounds[0])
			bounds[0] = delta;

		if(delta > bounds[1])
			bounds[1] = delta;

		return delta;
	}

	public boolean finishRollout(StateObservation rollerState, int depth)
	{
		if(depth >= Agent.ROLLOUT_DEPTH)	  //rollout end condition.
			return true;

		if(rollerState.isGameOver())			   //end of game
			return true;

		return false;
	}

	public void backUp(SingleTreeNode node, double result)
	{
		SingleTreeNode n = node;
		while(n != null)
		{
			n.nVisits++;
			n.totValue += result;
			n = n.parent;
		}
	}


	public int mostVisitedAction() 
	{
		int selected = -1;
		double bestValue = -Double.MAX_VALUE;
		boolean allEqual = true;
		double first = -1;

		for (int i=0; i<children.length; i++) {

			if(children[i] != null)
			{
				if(first == -1)
					first = children[i].nVisits;
				else if(first != children[i].nVisits)
				{
					allEqual = false;
				}

				double childValue = children[i].nVisits;
				childValue = Utils.noise(childValue, this.epsilon, this.random.nextDouble());	 //break ties randomly
				if (childValue > bestValue) {
					bestValue = childValue;
					selected = i;
				}
			}
		}

		if (selected == -1)
		{
			System.out.println("Unexpected selection!");
			selected = 0;
		}else if(allEqual)
		{
			//If all are equal, we opt to choose for the one with the best Q.
			selected = bestAction();
		}
		return selected;
	}

	public int bestAction()
	{
		int selected = -1;
		double bestValue = -Double.MAX_VALUE;

		for (int i=0; i<children.length; i++) 
		{
			if(children[i] != null) 
			{
				double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
				childValue = Utils.noise(childValue, this.epsilon, this.random.nextDouble());	 //break ties randomly
				if (childValue > bestValue)
				{
					bestValue = childValue;
					selected = i;
				}
			}
		}
		if (selected == -1)
		{
			System.out.println("Unexpected selection!");
			selected = 0;
		}
		return selected;
	}


	public boolean notFullyExpanded() 
	{
		for (SingleTreeNode tn : children) 
		{
			if (tn == null) 
			{
				return true;
			}
		}

		return false;
	}
}
