package client.heuristic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import client.SearchAgent;
import client.node.Node;
import client.node.storage.LogicalAgent;
import client.node.storage.Box;
import client.node.storage.Goal;

public abstract class Heuristic implements Comparator< Node > {

	public SearchAgent agent;
	
	public HashMap<Node, Integer> hs =new HashMap<>();

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

			int h=0;
			for (Goal subgoal : agent.subgoals) {
				int tmp=Integer.MAX_VALUE;
				for (Box box : n.getBoxes(subgoal.getType())) {
					if(n.isGoal(box.row, box.col)!= box.getType()&& n.distance(n.agents[agent.id], box)!=null){
//					if(!box.isAt(subgoal.row, subgoal.col) && n.distance(n.agents[agent.id], box)!=null){
						tmp= Math.min(tmp,(int)( n.distance(n.agents[agent.id], box)+n.distance(box, subgoal)*1.5));
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



	public void finishedWithGoal(Goal g){
		Heuristic.agent_goal_bookkeeping.remove(g);
	}


	public Goal selectGoal(Node node){
		Goal g = selectGoal_boxGoalDist(node);
		if(g!=null){
			Heuristic.agent_goal_bookkeeping.put(g, this.agent.id);
		}
		return g;
	}

	private boolean goalInUse(Goal g){
		return ( Heuristic.agent_goal_bookkeeping.get(g) != null /*|| Heuristic.agent_goal_bookkeeping.get(g).intValue != this.agentID */);
	}

	@SuppressWarnings("unused")
	private Goal selectGoal_goalDist(Node node){
		LogicalAgent agent = node.agents[this.agent.id];
		ArrayList<Goal> goals = node.getCluster(agent.id);
		Goal selectedGoal = goals.get(0);
		
		for( Goal goal : goals ){
			if( goalInUse(goal) )
				continue;

			if( node.distance(agent, goal) < node.distance(agent, selectedGoal) )
				selectedGoal = goal;
		}
		return selectedGoal;
	}

	private Goal selectGoal_boxGoalDist(Node node){
		LogicalAgent agent = node.agents[this.agent.id];
		ArrayList<Goal> goals = node.getCluster(agent.id);

		// Selected Values
		Goal selectedGoal = null;
		@SuppressWarnings("unused")
		Box selectedBox = null;
		int dist = Integer.MAX_VALUE;

		for( Goal goal : goals ){
			if( goalInUse(goal) )
				continue;
			if(node.distance(goal, agent)== null){
				continue;
			}
			
			for( Box box : node.getBoxes(goal.getType()) ){
				if(node.distance(agent, box) != null && ( node.distance(agent, box) + node.distance(box, goal)*goal.importance ) < dist ){
					dist = node.distance(agent, box) + node.distance(box, goal)*goal.importance;
					// Set the selects
					selectedGoal = goal;
					selectedBox = box;
				}
			}
		}

		return selectedGoal;
	}
}