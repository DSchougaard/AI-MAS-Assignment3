package client.node.level.distancemap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import client.node.level.Level;
import client.node.storage.Base;

/*
	DistanceMap based on the Floyd Warshall algorithm.
	http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm
*/
public class FloydWarshallDistanceMap extends DistanceMap{

	private HashMap<Base, Integer> index;
	private int id;

	int[][] distance;

	public FloydWarshallDistanceMap(){
		index = new HashMap<Base, Integer>();
		id = 0;
	}

	private Integer getIndex(Base p){

		return index.get(p);
	}

	public void initialize(Level level){
		long start_time = System.currentTimeMillis();

		HashMap<Base, ArrayList<Base>> map = explore(level);
		int size = map.size();

		System.err.println("Initialized FloydWarshall matrix to size " + size + "x" + size + ".");

		distance = new int[size][size];
		
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				distance[i][j]=Integer.MAX_VALUE;
			}
		}
		
		for( int i = 0 ; i < size ; i++ )
			distance[i][i] = 0;

		for( Base p : map.keySet() ){
			for( Base n : map.get(p) ){
				distance[getIndex(p)][getIndex(n)] = 1;
			}
		}

		for( int k = 0 ; k < size ; k++ ){
			for( int i = 0 ; i < size ; i++ ){
				for( int j = 0 ; j < size ; j++ ){
					if( distance[i][j] > ((distance[i][k] == Integer.MAX_VALUE || distance[k][j]== Integer.MAX_VALUE)?Integer.MAX_VALUE:distance[i][k] + distance[k][j]) )
						distance[i][j] = distance[i][k] + distance[k][j];
				}
			}
		}

		long end_time = System.currentTimeMillis();
		long difference = end_time-start_time;
		System.err.println("Initialization time for " + name() + " took " + difference + " ms."); 
	}

	private HashMap<Base, ArrayList<Base>> explore(Level level){

		HashMap<Base, ArrayList<Base>> visited = new HashMap<Base, ArrayList<Base>>();

		for(Base base: level.getGoals()){
			HashMap<Base, ArrayList<Base>> visited2 =explore(level, base);
			if(visited2 != null){
				visited.putAll(visited2);
			}
		}
		
		return visited;
	}
	
	private HashMap<Base, ArrayList<Base>> explore(Level level, Base start){
		if(getIndex(start)!= null){
			return null;
		}
		
		
		ArrayDeque<Base> frontier = new ArrayDeque<Base>();
		frontier.push(start);
		HashMap<Base, ArrayList<Base>> visited = new HashMap<Base, ArrayList<Base>>();

		while( !frontier.isEmpty() ){
			Base p = frontier.pop();

			if( !index.containsKey(p) ){
				// I have absolutely no idea why this is necesary
				index.put( p, id );
				id++;
			}

			ArrayList<Base> neighbours = new ArrayList<Base>();
			Base[] move 	= new Base[4];
			move[0] 		= new Base(p.row-1, p.col);
			move[1] 		= new Base(p.row+1, p.col);
			move[2]			= new Base(p.row, p.col-1);
			move[3]			= new Base(p.row, p.col+1);
			for( int i = 0 ; i < move.length ; i++ ){
				if( !level.isWall(move[i].row, move[i].col) )
					neighbours.add(move[i]);

				if( !level.isWall(move[i].row, move[i].col) && !visited.containsKey(move[i]) )
					frontier.push(move[i]);
			}
			visited.put(p, neighbours);
		}
		return visited;
	}

	public int distance(Base p1, Base p2){
		try {
			return distance[getIndex(p1)][getIndex(p2)];
		} catch (Exception e) {
			//distance not found
			return Integer.MAX_VALUE;
			
		}
			
	}


	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){

		return distance(new Base(rowFrom, colFrom), new Base(rowTo, colTo));
	}

	public String name(){
		return "FloydWarshallDistanceMap";
	}
}