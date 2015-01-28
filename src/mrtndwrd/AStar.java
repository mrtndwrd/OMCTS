package mrtndwrd;

import core.game.StateObservation;
import core.game.Observation;
import tools.Vector2d;

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
						// This means that x is the vertical coordinate from the
						// left up corner, and y is the horizontal coordinate
						// from the left upper corner
						walls.add(new Tuple<Integer, Integer>((int) obs.position.x/blockSize, (int) obs.position.y/blockSize));
					}
				}
			}
		}
		maxY = observationGrid.length;
		maxX = observationGrid[0].length;
		System.out.println(this);
	}

	public ArrayList<Tuple<Integer, Integer>> aStar (Vector2d start, Vector2d goal)
	{
		return aStar (new Tuple<Integer, Integer>((int) start.x/blockSize, (int)start.y/blockSize), 
			new Tuple<Integer, Integer>((int) goal.x/blockSize, (int) goal.y/blockSize));
	}

	/** Run a* with cartesian coordinates. Block size should already be
	 * eliminated from these positions */
	public ArrayList<Tuple<Integer, Integer>> aStar(Tuple<Integer, Integer> start, Tuple<Integer, Integer> goal)
	{
		openSet.add(start);
		gScore.put(start, 0.);
		fScore.put(start, fScore(start));
		Tuple<Integer, Integer> current;
		while(openSet.size() != 0)
		{
			// current := the node in openset having the lowest f_score[] value
			current = openSet.poll();
			if(current == goal)
				return reconstructPath(current);
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
		return new ArrayList<Tuple<Integer, Integer>>();
	}

	// TODO
	private ArrayList<Tuple<Integer, Integer>> reconstructPath(Tuple<Integer, Integer> end)
	{
		return new ArrayList<Tuple<Integer, Integer>>();
	}

	/** Creates a collection of neighbours reachable from node 'node' */
	private Collection<Tuple<Integer, Integer>> getNeighbours(Tuple<Integer, Integer> node)
	{
		Collection<Tuple<Integer, Integer>> neighbours = 
			new ArrayList<Tuple<Integer, Integer>> (4);
		neighbours.add(new Tuple<Integer, Integer>(node.x + 1, node.y));
		neighbours.add(new Tuple<Integer, Integer>(node.x - 1, node.y));
		neighbours.add(new Tuple<Integer, Integer>(node.x, node.y + 1));
		neighbours.add(new Tuple<Integer, Integer>(node.x, node.y - 1));
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

	/** Print all the walls! */
	public String toString()
	{
		String s = "Walls: \n";
		for (int i=0; i<maxX; i++)
		{
			for (int j=0; j<maxY; j++)
			{
				if(walls.contains(new Tuple<Integer, Integer>(j, i)))
					s += "W";
				else
					s += " ";
			}
			s += "\n";
		}
		return s;
	}
}
