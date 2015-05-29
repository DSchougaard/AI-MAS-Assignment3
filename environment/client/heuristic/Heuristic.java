package client.heuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import client.SearchAgent;
import client.Settings;
import client.node.Node;
import client.node.storage.Box;
import client.node.storage.Goal;
import client.node.storage.LogicalAgent;



public class Heuristic {

	public SearchAgent agent;
	
	public HashMap<Node, Integer> hs =new HashMap<>();

	private static HashMap<Goal, Integer> agent_goal_bookkeeping = new HashMap<>();
	
	public Heuristic( SearchAgent agent ){
		this.agent = agent;
	}

	
	public int h ( Node n ) {
		//euclid distance from mover to box and from box to goal
		Integer tmpH=hs.get(n);

		HashSet<Box> usedBoxes = new HashSet<>();

		if(tmpH==null){
			int gc=n.getGoals().size();
			for (Goal goal : n.getGoals()) {
				if(n.isGoalState(goal)){
					gc--;
				}
			}
			
			//int h=0;
			int h=gc*Settings.Heuristic.goalPunishment;
			for (Goal subgoal : agent.subgoals) {
				
				int tmp=Integer.MAX_VALUE;
				Box tmpBox = null;

				for (Box box : n.getBoxes(subgoal.getType())) {
					if(!usedBoxes.contains(box) && !n.isGoalState(subgoal) && n.isGoal(box.row, box.col)!= box.getType() && n.distance(n.agents[agent.id], box)!=null){
					//if(!n.isGoalState(subgoal) && n.distance(n.agents[agent.id], box)!=null){
						tmp= Math.min(tmp,n.distance(n.agents[agent.id], box)-1+n.distance(box, subgoal)*2);
						tmpBox = box;
					}
				}
				if(tmp==Integer.MAX_VALUE){
					
					tmp=0;
				}
				h+=tmp;

				usedBoxes.add(tmpBox);

			}
			hs.put(n, h);
			return h;
		}else{
			return tmpH;
		}
		
	}




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
	
	public Goal selectGoalWithOutBookkeeping(Node node){
		Goal g = selectGoal_boxGoalDist(node);
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
				if(node.distance(agent, box) != null && ( node.distance(agent, box)-1 + node.distance(box, goal)*goal.importance ) < dist ){
					dist = node.distance(agent, box)-1 + node.distance(box, goal)*goal.importance;
					// Set the selects
					selectedGoal = goal;
					selectedBox = box;
				}
			}
		}

		return selectedGoal;
	}

	public static void reset() {
		agent_goal_bookkeeping=new HashMap<>();
		
	}
}