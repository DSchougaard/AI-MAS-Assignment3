package client.node.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.awt.Point;

import client.node.map.Level;

public class BruteForceDistanceMap extends DistanceMap{
	private HashMap<Point, HashMap<Point, Integer>> values;

	public BruteForceDistanceMap(){
		values = new HashMap<Point, HashMap<Point, Integer>>();
	}

	public int distance(int a, int b, int c, int d){
		return 0;
	}

	public int distance(Point p1, Point p2){
		return 0;
	}

	public String name(){
		return "BruteForceDistanceMap";
	}

	public void initialize(Level level){
		long start_time = System.currentTimeMillis();

		HashSet<Point> reachable = this.explore(level);
		System.err.println("Initializing BruteForceDistanceMap.");
		for(Point p : reachable ){
			values.put(p, calculateDistances(p, 0, new HashMap<Point, Integer>(), level));
		}

		long end_time = System.currentTimeMillis();
		long difference = end_time-start_time;
		System.err.println("Initialization time for " + name() + " took " + difference + " ms."); 
	}

	private HashSet<Point> explore(Level level){
		Point start = level.getGoals().get(0).getPoint();
		ArrayDeque<Point> frontier = new ArrayDeque<Point>();
		frontier.push(start);
		HashSet<Point> visited = new HashSet<Point>();

		while( !frontier.isEmpty() ){
			Point p = frontier.pop();
			Point[] move 	= new Point[4];
			move[0] 		= new Point(p.x-1, p.y);
			move[1] 		= new Point(p.x+1, p.y);
			move[2]			= new Point(p.x, p.y-1);
			move[3]			= new Point(p.x, p.y+1);
			for( int i = 0 ; i < move.length ; i++ ){
				if( !level.isWall(move[i].x, move[i].y) && !visited.contains(move[i]) )
					frontier.push(move[i]);
			}
			visited.add(p);
		}
		return visited;
	}

	private HashMap<Point, Integer> calculateDistances(Point p, int distance, HashMap<Point, Integer> visited, Level level){
		Point[] move 	= new Point[4];
		move[0] 		= new Point(p.x-1, p.y);
		move[1] 		= new Point(p.x+1, p.y);
		move[2]			= new Point(p.x, p.y-1);
		move[3]			= new Point(p.x, p.y+1);

		visited.put(p, new Integer(distance));

		for( int i = 0 ; i < move.length ; i++ ){
			if( level.isWall(move[i].x, move[i].y) )
				continue;
			if( !visited.containsKey(move[i]) || ( visited.containsKey(move[i]) && distance+1 < visited.get(move[i]).intValue() ) ){
				calculateDistances(move[i], distance+1, visited, level);
			}
		}
		return visited;
	}

	public void printMap(){
		for( Point p : values.keySet() ){
			System.out.println("[ " + p.x + ", " + p.y + " ]: ");
			HashMap<Point, Integer> distances = values.get(p);
			for( Point p2 : distances.keySet() ){
				System.out.println("    " + p2.x + ", " + p2.y + " -> " +  distances.get(p2).intValue());
			}
			System.out.println("");
		}
	}
}