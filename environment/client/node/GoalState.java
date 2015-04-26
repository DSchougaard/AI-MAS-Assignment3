package client.node;

import java.util.ArrayList;

import client.node.storage.*;
import client.node.Node;

public abstract class GoalState{

	
	
	abstract public boolean eval(Node node);
	
	public static class GoalGoalState extends GoalState{
		ArrayList<Goal> goals;
		
		public GoalGoalState(ArrayList<Goal> goals) {
			this.goals=goals;
		}

		@Override
		public boolean eval(Node node) {
			return node.isGoalState(goals);
		}
		
		@Override
		public String toString(){
			return "GoalGoalState";
		}
		
	}
	public static class ObstructionGoalState extends GoalState{
		
		@SuppressWarnings("unused")
		private int agentID;
		private int obstructionCount;
		private ArrayList<Base> route;

		@Override
		public String toString(){
			return "ObstructionGoalState";
		}

		public ObstructionGoalState(int agentID, int obstructionCount, ArrayList<Base> route){
			this.agentID = agentID;
			this.obstructionCount = obstructionCount;
			this.route = route;
		}

		public boolean eval(Node node){

		int count = 0;

		for( Base b : route ){
			Object o = node.objectAt(b);	
			if( o instanceof Box || o instanceof LogicalAgent )
				count++;
		}

		return (count == (obstructionCount - 1));
		}
	}

	public static class ProximityGoalState extends GoalState{

		private int agentID;
		private int targetRow, targetCol;

		public ProximityGoalState(int agentID, int targetRow, int targetCol){
			this.agentID = agentID;
			this.targetCol = targetCol;
			this.targetRow = targetRow;
		}

		public boolean eval(Node node){
			LogicalAgent a = node.agents[agentID];

			return (
				( targetRow == a.row+1 && targetCol == a.col ) ||
				( targetRow == a.row-1 && targetCol == a.col ) ||
				( targetRow == a.row && targetCol == a.col+1 ) ||
				( targetRow == a.row && targetCol == a.col-1 )
				);
		}

		@Override
		public String toString(){
			return "Proximity Goal State";
		}
	}

	public static class RouteClearOfAgentGoalState extends GoalState{
		private int agentID;
		private ArrayList<Base> route;

		public RouteClearOfAgentGoalState(int agentID, ArrayList<Base> route){
			this.agentID = agentID;
			this.route = route;
		}

		public boolean eval(Node node){
			LogicalAgent a = node.agents[agentID];
			for( Base b : route ){
				if( a.row == b.row && a.col == b.col )
					return false;
			}
			return true;
		}
	}

	public static class RouteClearGoalState extends GoalState{
		private int agentID, boxID;
		private ArrayList<Base> route;

		public RouteClearGoalState(int agentID, int boxID, ArrayList<Base> route){
			this.agentID = agentID;
			this.boxID = boxID;
			// Copy the route.
			this.route = new ArrayList<>();
			this.route.addAll(route);
		}

		public boolean eval(Node node){
			LogicalAgent a = node.agents[this.agentID];
			Box b = null;
			if( this.boxID != -1 )
				b = node.getBoxesByID().get(this.boxID);

			for( Base base : route ){
				// If agent is found on route, it's NOT a goalstate
				if( a.row == base.row && a.col == base.col )
					return false;

				// If the box is found on the route, it's NOT a goalstate.
				if( b != null && b.row == base.row && b.col == base.col )
					return false;
			}
			return true;
		}
		
		public String toString(){
			return "RouteClearGoalState: "+agentID+":"+boxID;
		}
	}


	public static class MoveToGoalState extends GoalState{
		private int agentID;
		private int targetRow, targetCol;

		public MoveToGoalState(int agentID, int targetRow, int targetCol){
			this.agentID = agentID;
			this.targetCol = targetCol;
			this.targetRow = targetRow;
		}

		public boolean eval(Node node){
			LogicalAgent a = node.agents[agentID];
			return ( targetRow == a.row && targetCol == a.col );
		}
	}
}