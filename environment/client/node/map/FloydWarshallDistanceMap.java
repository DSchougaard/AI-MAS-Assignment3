package client.node.map;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.awt.Point;

import client.node.map.DistanceMap;
import client.node.map.Level;

/*
	DistanceMap based on the Floyd Warshall algorithm.
	http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm
*/
public class FloydWarshallDistanceMap extends DistanceMap{

	private HashMap<Point, Integer> index;
	private int id;

	int[][] distance;

	public FloydWarshallDistanceMap(){
		index = new HashMap<Point, Integer>();
		id = 0;
	}

	private int getIndex(Point p1){
		return index.get(p1).intValue();
	}

	private int getIndex(int row, int col){
		return getIndex(new Point(row, col));
	}

	public void initialize(Level level){
		long start_time = System.currentTimeMillis();

		HashMap<Point, ArrayList<Point>> map = explore(level);
		int size = map.size();

		System.err.println("Initialized FloydWarshall matrix to size " + size + "x" + size + ".");

		distance = new int[size][size];
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				distance[i][j]=999;
			}
		}
		
		
		for( int i = 0 ; i < size ; i++ )
			distance[i][i] = 0;

		for( Point p : map.keySet() ){
			for( Point n : map.get(p) ){
				distance[getIndex(p)][getIndex(n)] = 1;
			}
		}

		for( int k = 0 ; k < size ; k++ ){
			for( int i = 0 ; i < size ; i++ ){
				for( int j = 0 ; j < size ; j++ ){
					if( distance[i][j] > distance[i][k] + distance[k][j] )
						distance[i][j] = distance[i][k] + distance[k][j];
				}
			}
		}

		long end_time = System.currentTimeMillis();
		long difference = end_time-start_time;
		System.err.println("Initialization time for " + name() + " took " + difference + " ms."); 

	}


	private HashMap<Point, ArrayList<Point>> explore(Level level){
		Point start = level.getGoals().get(0).getPoint();
		ArrayDeque<Point> frontier = new ArrayDeque<Point>();
		frontier.push(start);
		HashMap<Point, ArrayList<Point>> visited = new HashMap<Point, ArrayList<Point>>();

		while( !frontier.isEmpty() ){
			Point p = frontier.pop();

			if( !index.containsKey(p) ){
				// I have absolutely no idea why this is necesary
				index.put( p, new Integer(id) );
				id++;
			}

			ArrayList<Point> neighbours = new ArrayList<Point>();
			Point[] move 	= new Point[4];
			move[0] 		= new Point(p.x-1, p.y);
			move[1] 		= new Point(p.x+1, p.y);
			move[2]			= new Point(p.x, p.y-1);
			move[3]			= new Point(p.x, p.y+1);
			for( int i = 0 ; i < move.length ; i++ ){
				if( !level.isWall(move[i].x, move[i].y) ){
					neighbours.add(move[i]);
				}

				if( !level.isWall(move[i].x, move[i].y) && !visited.containsKey(move[i]) )
					frontier.push(move[i]);
			}
			visited.put(p, neighbours);
		}
		return visited;
	}

	public int distance(Point p1, Point p2){
		return distance[getIndex(p1)][getIndex(p1)];	
	}


	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return distance[getIndex(rowFrom, colFrom)][getIndex(rowTo, colTo)];
	}


	public String name(){
		return "FloydWarshallDistanceMap";
	}
}