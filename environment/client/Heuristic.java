package client;

import java.util.Comparator;
import java.util.HashMap;

import client.node.Node;
import client.node.storage.Agent;
import client.node.storage.Box;
import client.node.storage.Goal;

public abstract class Heuristic implements Comparator< Node > {

	public Node initialState;
	public int agentID;
	
	public HashMap<Node, Integer> hs =new HashMap<>();
	
	public Heuristic(Node initialState, int agentID) {
		this.initialState = initialState;
		this.agentID=agentID;
		
	}

	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}

	public int h( Node n ) {
		//euclid distance from mover to box and from box to goal
		Integer tmpH=hs.get(n);
		if(tmpH==null){

			int h=0;
//			ArrayList<Box> boxs= n.getBoxes();
			
			Box[] boxs=n.getBoxes();
			for (Box box : boxs) {
				Goal goal=n.getGoals(box.getType()).get(0);
				if(goal.type == box.getType()){
					h+=n.distance(box, goal)+n.distance(n.agents[agentID], goal);
				}
				
				
			}
			hs.put(n, h);
			return h;
		}else{
			return tmpH;
		}
		
	}

	public abstract int f( Node n);

	public static class AStar extends Heuristic {
		public AStar(Node initialState, int agentID) {
			super( initialState, agentID);
		}

		public int f( Node n) {
			return n.g() + h( n );
		}

		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, int agentID) {
			super( initialState, agentID);
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

		public Greedy(Node initialState, int agentID) {
			super( initialState, agentID);
		}
		

		public int f( Node n ) {
			return h( n );
		}

		public String toString() {
			return "Greedy evaluation";
		}
	}
	


}
