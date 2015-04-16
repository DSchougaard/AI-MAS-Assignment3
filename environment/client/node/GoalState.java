package client.node;

import java.util.ArrayList;
import java.util.HashMap;

import client.node.storage.*;


import client.node.Node;

public abstract class GoalState{

	abstract public boolean eval(Node node);

	public static class ObstructionGoalState extends GoalState{
		
		private int agentID;
		private int obstructionCount;
		private ArrayList<Base> route;

		public ObstructionGoalState(int agentID, int obstructionCount, ArrayList<Base> route){
			this.agentID = agentID;
			this.obstructionCount = obstructionCount;
			this.route = route;
		}

		public boolean eval(Node node){
		LogicalAgent agent = node.agents[agentID];

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
				if( a.row == b. row && a.col == b.col )
					return false;
			}
			return true;
		}
	}

}