package client;

import java.util.Comparator;
import java.util.HashMap;

public abstract class Heuristic implements Comparator< Node > {

	public Node initialState;
	
	public HashMap<Character, Point> goals =new HashMap<>();
	public HashMap<Node, Integer> hs =new HashMap<>();
	
	public Heuristic(Node initialState) {
		this.initialState = initialState;

		for (int i = 0; i < Node.MAX_COLUMN; i++) {
			for (int j = 0; j < Node.MAX_ROW; j++) {
				if(Character.isLetter(Node.goals[j][i])){
					
					goals.put(Node.goals[j][i], new Point(j,i));
				}
			
			}
		}
		
	}

	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}

	public int h( Node n ) {
		//euclid distance from mover to box and from box to goal
		Integer tmpH=hs.get(n);
		if(tmpH==null){

			int h=0;
			for (int i = 0; i < Node.MAX_COLUMN; i++) {
				for (int j = 0; j < Node.MAX_ROW; j++) {
					if(Character.isLetter(n.boxes[j][i])){
						if(Character.toLowerCase(n.boxes[j][i])!=Node.goals[j][i]){
						
							h+=goals.get(Character.toLowerCase(n.boxes[j][i])).dist(j, i)+Point.dist(j, i, n.agents[n.agent.id][0], n.agents[n.agent.id][1])-1;

						}
					}
				}
			}
			hs.put(n, h);
			return h;
		}else{
			return tmpH;
		}
		
	}

	public abstract int f( Node n );

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super( initialState );
		}

		public int f( Node n ) {
			return n.g() + h( n );
		}

		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState) {
			super( initialState );
			W = 5; // You're welcome to test this out with different values, but for the reporting part you must at least indicate benchmarks for W = 5
		}

		public int f( Node n ) {
			return n.g() + W * h( n );
		}

		public String toString() {
			return String.format( "WA*(%d) evaluation", W );
		}
	}

	public static class Greedy extends Heuristic {

		public Greedy(Node initialState) {
			super( initialState );
		}
		

		public int f( Node n ) {
			return h( n );
		}

		public String toString() {
			return "Greedy evaluation";
		}
	}
	

	
	public static class Point{
		
		public int y;
		public int x;

		public Point(int x, int y){
			this.x=x;
			this.y=y;
		}
		public static int dist(int x, int y, int x2, int y2){

			return (int) Math.sqrt(Math.pow(Math.abs(x-x2),2)+Math.pow(Math.abs(y-y2),2));
		}
		public int dist(int x, int y){
			return (int) Math.sqrt(Math.pow(Math.abs(this.x-x),2)+Math.pow(Math.abs(this.y-y),2));
		}

	}
}
