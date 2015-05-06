package client.heuristic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import client.SearchAgent;
import client.node.Node;
import client.node.storage.LogicalAgent;
import client.node.storage.Box;
import client.node.storage.Goal;

public abstract class Heuristic implements Comparator< Node > {

	public SearchAgent agent;
	
	public HashMap<Node, Integer> hs =new HashMap<>();
	private HashSet<Goal> unassignedGoals = new HashSet<>();

	private static HashMap<Goal, Integer> agent_goal_bookkeeping = new HashMap<>();
	
	public Heuristic( SearchAgent agent ){
		this.agent = agent;
	}

	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}

	
	public int h ( Node n ) {
		//euclid distance from mover to box and from box to goal
		Integer tmpH=hs.get(n);
		if(tmpH==null){
			int gc=n.getGoals().size();
			for (Goal goal : n.getGoals()) {
				if(n.isGoalState(goal)){
					gc--;
				}
			}
			
//			int h=0;
			int h=gc*5;
			for (Goal subgoal : agent.subgoals) {
				int tmp=Integer.MAX_VALUE;
				for (Box box : n.getBoxes(subgoal.getType())) {
					if(!n.isGoalState(subgoal) && n.isGoal(box.row, box.col)!= box.getType() && n.distance(n.agents[agent.id], box)!=null){
//					if(!n.isGoalState(subgoal) && n.distance(n.agents[agent.id], box)!=null){
						tmp= Math.min(tmp,n.distance(n.agents[agent.id], box)-1+n.distance(box, subgoal)*2);
					}
				}
				if(tmp==Integer.MAX_VALUE){
					
					tmp=0;
				}
				h+=tmp;
			}
			hs.put(n, h);
			return h;
		}else{
			return tmpH;
		}
		
	}


	public abstract int f( Node n);



}