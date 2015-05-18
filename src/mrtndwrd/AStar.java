package mrtndwrd;

import core.game.StateObservation;
import core.game.Observation;
import tools.Vector2d;
import ontology.Types;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Comparator;

public class AStar
{
	/** The size of (movement) blocks in the current game */
	private int blockSize;

	/** The set of walls extracted from the first state observation */
	private Set<Tuple<Integer, Integer>> walls;

	/** Max x coordinate (min is assumed to be 0) */
	public int maxX;

	/** Max y coordinate (min is assumed to be 0) */
	public int maxY;

	private Tuple<Integer, Integer> goal;

	private DefaultHashMap<Tuple<Integer, Integer>, Double> gScore = new DefaultHashMap<Tuple<Integer, Integer>, Double> (0.);

	private Map<Tuple<Integer, Integer>, Double> fScore = new HashMap<Tuple<Integer, Integer>, Double> ();

	/** Set of nodes to check out, ordered by fScore */
	private PriorityQueue<Tuple<Integer, Integer>> openSet;

	/** Used to find out the actual best path, when the goal is reached */
	private HashMap<Tuple<Integer, Integer>, Tuple<Integer, Integer>> cameFrom;

	/** Set of nodes that has been checked */
	HashSet<Tuple<Integer, Integer>> closedSet;

	/** Initialize the grid. Assumes 0's in the observationGrid are walls */
	public AStar(StateObservation so)
	{
		blockSize = so.getBlockSize();

		walls = new HashSet<Tuple<Integer, Integer>>();

		openSet = new PriorityQueue<Tuple<Integer, Integer>>(10, new TupleComparator());
		closedSet = new HashSet<Tuple<Integer, Integer>>();
		cameFrom = new HashMap<Tuple<Integer, Integer>, Tuple<Integer, Integer>>();

		ArrayList<Observation> observationGrid[][] = so.getObservationGrid();
		Vector2d position;
		// Extract walls from observationGrid
		for (int i = 0; i < observationGrid.length; i++)
		{
			for (int j = 0; j<observationGrid[i].length; j++)
			{
				for (Observation obs : observationGrid[i][j])
				{
					// This is assumed to be a wall
					if(obs.itype == 0)
					{
						// This means that x is the horizontal coordinate from the
						// left up corner, and y is the vertical coordinate
						// from the left upper corner
						//walls.add(new Tuple<Integer, Integer>((int) obs.position.x/blockSize, (int) obs.position.y/blockSize));
						walls.add(vectorToBlock(obs.position));
					}
				}
			}
		}
		// X being vertical coordinates, is the inner array
		maxX = observationGrid.length-1;
		// Y, the horizontal coordinates, is the outer array
		maxY = observationGrid[0].length-1;
		//System.out.println(this);
	}

	/** Creates a tuple of integers representing block coordinates from a
	 * vector2d representing field coordinates 
	 */
	public SerializableTuple<Integer, Integer> vectorToBlock(Vector2d vector)
	{
		return new SerializableTuple<Integer, Integer>((int) vector.x/blockSize, (int)vector.y/blockSize);
	}

	public ArrayList<SerializableTuple<Integer, Integer>> aStar (Vector2d start, Vector2d goal)
	{
		return aStar (vectorToBlock(start), vectorToBlock(goal));
	}

	/** Run a* with cartesian coordinates. Block size should already be
	 * eliminated from these positions */
	public ArrayList<SerializableTuple<Integer, Integer>> aStar(Tuple<Integer, Integer> start, Tuple<Integer, Integer> goal)
	{
		// Reset everything:
		openSet.clear();
		closedSet.clear();
		gScore.clear();
		fScore.clear();
		cameFrom.clear();
		//System.out.println("Starting a* from " + start + " to " + goal);
		// We can't go to walls!
		if(walls.contains(goal))
			return new ArrayList<SerializableTuple<Integer, Integer>>();
		this.goal = goal;
		openSet.add(start);
		gScore.put(start, 0.);
		fScore.put(start, fScore(start));
		Tuple<Integer, Integer> current;
		while(openSet.size() != 0)
		{
			// current := the node in openset having the lowest f_score[] value
			current = openSet.poll();
			if(current.equals(goal))
			{
				return reconstructPath(current);
			}
			closedSet.add(current);
			double currentGScore = gScore.get(current);
			for(Tuple<Integer, Integer> neighbour : getNeighbours(current))
			{
				if(closedSet.contains(neighbour))
					continue;
				double tentativeGScore = currentGScore
					+ distance(current, neighbour);
				if(!openSet.contains(neighbour) || tentativeGScore < currentGScore)
				{
					cameFrom.put(neighbour, current);
					gScore.put(neighbour, tentativeGScore);
					fScore.put(neighbour, fScore(neighbour));
					if(!openSet.contains(neighbour))
						openSet.add(neighbour);
				}
			}
		}
		// Nothing found...
		return new ArrayList<SerializableTuple<Integer, Integer>>();
	}

	private ArrayList<SerializableTuple<Integer, Integer>> reconstructPath(Tuple<Integer, Integer> end)
	{
		//System.out.println("Reconstructing path");
		ArrayList<SerializableTuple<Integer, Integer>> path = new ArrayList<SerializableTuple<Integer, Integer>>();
		Tuple<Integer, Integer> current = new SerializableTuple(end);
		do
		{
			path.add(new SerializableTuple(current));
			current = cameFrom.get(current);
		}
		while (current != null);
		return path;
	}

	/** Creates a collection of neighbours reachable from node 'node' */
	private Collection<Tuple<Integer, Integer>> getNeighbours(Tuple<Integer, Integer> node)
	{
		Collection<Tuple<Integer, Integer>> neighbours = 
			new ArrayList<Tuple<Integer, Integer>> (4);
		Tuple<Integer, Integer> nt;
		if(node.x < maxX)
		{
			nt = new Tuple<Integer, Integer>(node.x + 1, node.y);
			if(!walls.contains(nt))
				neighbours.add(nt);
		}
		if(node.x > 0)
		{
			nt = new Tuple<Integer, Integer>(node.x - 1, node.y);
			if(!walls.contains(nt))
				neighbours.add(nt);
		}
		if(node.y < maxY)
		{
			nt = new Tuple<Integer, Integer>(node.x, node.y + 1);
			if(!walls.contains(nt))
				neighbours.add(nt);
		}
		if(node.y > 0)
		{
			nt = new Tuple<Integer, Integer>(node.x, node.y - 1);
			if(!walls.contains(nt))
				neighbours.add(nt);
		}
		return neighbours;
	}

	/** Calculates fscore, which is the gScore + euclidian distance from node to
	 * goal */
	private double fScore(Tuple<Integer, Integer> node)
	{
		return gScore.get(node) + heuristic(node);
	}

	private double heuristic(Tuple<Integer, Integer> node)
	{
		return distance(node, goal);
	}

	public double distance(Tuple<Integer, Integer> node, 
		Tuple<Integer, Integer> goal)
	{
		// sqrt(x^2 + y^2)
		return Math.sqrt(Math.pow(node.x - goal.x, 2) + 
			Math.pow(node.y - goal.y, 2));
	}

	/** Compare tuples by fScore */
	public class TupleComparator implements 
		Comparator<Tuple<Integer, Integer>>
	{
		public int compare (Tuple<Integer, Integer> n1,
			Tuple<Integer, Integer> n2)
		{
			double f1 = fScore(n1);
			double f2 = fScore(n2);
			if(f1 > f2)
				return 1;
			else if(f2 > f1)
				return -1;
			return 0;
		}
	}

	/** Returns the action that leads from start to end. */
	public static Types.ACTIONS neededAction(Tuple<Integer, Integer> start, 
		Tuple<Integer, Integer> end)
	{
		if(start.x > end.x)
		{
			return Types.ACTIONS.ACTION_LEFT;
		}
		else if(start.x < end.x)
		{
			return Types.ACTIONS.ACTION_RIGHT;
		}
		else if(start.y > end.y)
		{
			return Types.ACTIONS.ACTION_UP;
		}
		else if(start.y < end.y)
		{
			return Types.ACTIONS.ACTION_DOWN;
		}
		System.out.println("Probably same location, returning action nil");
		return Types.ACTIONS.ACTION_NIL;
	}

	/** Print all the walls! */
	public String toString()
	{
		String s = "Walls: \n";
		// First, loop through possible y coordinates (since y is vertical)
		for (int y=0; y<=maxY; y++)
		{
			// Now loop through x (horizontal)
			for (int x=0; x<=maxX; x++)
			{
				if(walls.contains(new Tuple<Integer, Integer>(x, y)))
					s += "W";
				else
					s += " ";
			}
			s += "\n";
		}
		return s;
	}
}
