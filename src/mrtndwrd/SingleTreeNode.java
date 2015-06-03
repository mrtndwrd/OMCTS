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
	public static final int REMAINING_LIMIT = 5;

	public static double epsilon = 1e-6;

	public static double egreedyEpsilon = 0.05;

	public StateObservation state;

	public SingleTreeNode parent;

	public SingleTreeNode[] children;

	public double totValue;

	public int nVisits;

	private ArrayList<Option> possibleOptions;

	private HashSet<Integer> optionObsIDs;

	public static Random random;
	/** The depth in the rollout of this node (initialized as parent.node+1) */
	public int nodeDepth;

	protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

	/** Root node constructor */
	public SingleTreeNode(ArrayList<Option> possibleOptions, HashSet<Integer> optionObsIDs, Random rnd) 
	{
		this(null, null, possibleOptions, optionObsIDs, rnd);
	}

	/** normal constructor */
	public SingleTreeNode(StateObservation state, SingleTreeNode parent, ArrayList<Option> possibleOptions, HashSet<Integer> optionObsIDs, Random rnd)
	{
		this.state = state;
		this.parent = parent;
		this.random = rnd;
		this.possibleOptions = possibleOptions;
		children = new SingleTreeNode[possibleOptions.size()];
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
			SingleTreeNode selected = treePolicy();
			// Get node value using a max-depth rollout
			double delta = selected.rollOut();
			// Set values for parents of current node, using new rollout value
			backUp(selected, delta);

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
		int bestOption = 0;
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

		StateObservation nextState = state.copy();

		// Step 1: run this option untill it's finished
		runOption(nextState, this.possibleOptions.get(bestOption));
		// Step 2: get the new option set
		// setOptions.... TODO - for now, just use actionOptions
		ArrayList<Option> newOptions = (ArrayList<Option>) this.possibleOptions.clone();
		HashSet<Integer> newOptionObsIDs = (HashSet<Integer>) this.optionObsIDs.clone();
		Agent.setOptions(nextState, newOptions, newOptionObsIDs);
		// Step 3: create a child node
		SingleTreeNode tn = new SingleTreeNode(nextState, this, newOptions, newOptionObsIDs, this.random);
		children[bestOption] = tn;
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

	/** Not used, not edited to work with options: */
	// public SingleTreeNode egreedy() 
	// {
	// 	SingleTreeNode selected = null;
	// 	if(random.nextDouble() < egreedyEpsilon)
	// 	{
	// 		//Choose randomly
	// 		int selectedIdx = random.nextInt(children.length);
	// 		selected = this.children[selectedIdx];

	// 	}else{
	// 		//pick the best Q.
	// 		double bestValue = -Double.MAX_VALUE;
	// 		for (SingleTreeNode child : this.children)
	// 		{
	// 			double hvVal = child.totValue;
	// 			hvVal = Utils.noise(hvVal, this.epsilon, this.random.nextDouble());	 //break ties randomly
	// 			// small sampleRandom numbers: break ties in unexpanded nodes
	// 			if (hvVal > bestValue) {
	// 				selected = child;
	// 				bestValue = hvVal;
	// 			}
	// 		}
	// 	}
	// 	if (selected == null)
	// 	{
	// 		throw new RuntimeException("Warning! returning null: " + this.children.length);
	// 	}
	// 	return selected;
	// }


	/** Perform a rollout with random actions on the current node of maximally
	 * Agent.ROLLOUT_DEPTH */
	public double rollOut()
	{
		StateObservation rollerState = state.copy();
		int thisDepth = this.nodeDepth;

		while (!finishRollout(rollerState,thisDepth)) 
		{
			// TODO: Random action selection, or option selection (try both)
			int action = random.nextInt(Agent.actions.length);
			rollerState.advance(Agent.actions[action]);
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
