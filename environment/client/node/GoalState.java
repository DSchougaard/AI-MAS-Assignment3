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
		private Box box;

		public ProximityGoalState(int agentID, Box box){
			this.agentID = agentID;
			this.box = box;
		}

		public boolean eval(Node node){
			LogicalAgent a = node.agents[agentID];

			return (
				( box.row == a.row+1 && box.col == a.col ) ||
				( box.row == a.row-1 && box.col == a.col ) ||
				( box.row == a.row && box.col == a.col+1 ) ||
				( box.row == a.row && box.col == a.col-1 )
				);
		}

	}

}