package client;

import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;

import client.node.Node;
import client.node.storage.Box;
import client.node.storage.Goal;
import client.node.storage.Agent;

public abstract class Heuristic implements Comparator< Node > {

	public Node initialState;
	public int agentID;
	
	public HashMap<Node, Integer> hs =new HashMap<>();

	//private static Goal[] agent_goal_bookkeeping;
	//private static HashMap<Agent, Goal> agent_goal_bookkeeping;
	private static HashMap<Goal, Integer> agent_goal_bookkeeping;
	
	public Heuristic(Node initialState, int agentID) {
		this.initialState = initialState;
		this.agentID=agentID;

		this.agent_goal_bookkeeping = new HashMap<Goal, Integer>();
	}

	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}

	public int h( Node n ) {
		//euclid distance from mover to box and from box to goal
		Integer tmpH=hs.get(n);
		if(tmpH==null){

			int h=0;
//			ArrayList<Box> boxes = n.getBoxes();
			
			Box[] boxes=n.getBoxes();
			for (Box box : boxes) {
				Goal goal=n.getGoals(box.getType()).get(0);
				if(goal.type == box.getType()){
					if (n.distance(box, goal) > 0){						
						h+=n.distance(box, goal) +1 ;
					}
					h+=n.distance(n.agents[agentID], goal);
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
	

	public void finishedWithGoal(Goal g){
		Heuristic.agent_goal_bookkeeping.remove(g);
	}


	public Goal selectGoal(){
		Goal g = selectGoal_boxGoalDist();
		//Heuristic.agent_goal_bookkeeping.put( new Integer(this.agentID), g);
		Heuristic.agent_goal_bookkeeping.put(g, new Integer(this.agentID));
		return g;
	}

	private boolean goalInUse(Goal g){
		return ( Heuristic.agent_goal_bookkeeping.get(g) != null /*|| Heuristic.agent_goal_bookkeeping.get(g).intValue != this.agentID */);
	}

	private Goal selectGoal_goalDist(){
		Agent a = this.initialState.agents[this.agentID];
		ArrayList<Goal> goals = this.initialState.getCluster(a);
		Goal selectedGoal = goals.get(0);
		
		for( Goal g : goals ){
			//if( Heuristic.agent_goal_bookkeeping.containsValue(g) && Heuristic.get(new Integer(this.agentID)) != g )
			if( goalInUse(g) )
				continue;

			if( this.initialState.distance(a, selectedGoal) < this.initialState.distance(a, g) )
				selectedGoal = g;
		}
		return selectedGoal;
	}

	private Goal selectGoal_boxGoalDist(){
		Agent a = this.initialState.agents[this.agentID];
		ArrayList<Goal> goals = this.initialState.getCluster(a);
		ArrayList<Box> boxes = null;

		// Selected Values
		Goal selectedGoal = null;
		Box selectedBox = null;
		int dist = Integer.MAX_VALUE;

		for( Goal g : goals ){
			if( goalInUse(g) )
				continue;

			boxes = initialState.getBoxes(g.getType());
			for( Box b : boxes ){
				if( b.getType() == g.getType() && ( this.initialState.distance(a, b) + this.initialState.distance(b, g) ) < dist ){
					dist = this.initialState.distance(a, b) + this.initialState.distance(b, g);
					// Set the selects
					selectedGoal = g;
					selectedBox = b;
				}
			}
		}

		if( selectedGoal == null )
			throw new NullPointerException("No more goals.");
		if( selectedBox == null )
			throw new NullPointerException("No available boxes for selected goal.");

		return selectedGoal;
	}




































}
