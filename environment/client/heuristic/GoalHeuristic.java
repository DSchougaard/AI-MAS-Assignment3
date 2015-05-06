package client.heuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import client.SearchAgent;
import client.node.Node;
import client.node.level.Level;
import client.node.level.clustering.KClusteringGoals;
import client.node.storage.Box;
import client.node.storage.Goal;
import client.node.storage.LogicalAgent;

public class GoalHeuristic {

	
	private HashMap<Goal, ArrayList<Integer>> goalAgentsGrouping= new HashMap<>();
	private HashMap<Integer, ArrayList<Goal>> agentGoalsGrouping= new HashMap<>();

	private HashMap<Goal, Integer> agent_goal_bookkeeping = new HashMap<>();
	
	// Clusters
	private HashMap<Integer, ArrayList<Goal>> clusters;
	
	public GoalHeuristic( LogicalAgent[] agents, Level level ){
		calculateCluster(agents, level, true);
		ArrayList<Goal> goals = level.getGoals();
		for (Goal goal : goals) {
			goalAgentsGrouping.put(goal, new ArrayList<>());
		}
		for (LogicalAgent LogicalAgent : agents) {
			if(LogicalAgent!=null)
			agentGoalsGrouping.put(LogicalAgent.id, new ArrayList<>());
		}
		
		
		for (LogicalAgent logicalAgent : agents) {
			if(logicalAgent!=null)
			for (Goal goal : level.getGoals(logicalAgent.color)) {
				if(level.distance(logicalAgent, goal)!=null){
					goalAgentsGrouping.get(goal).add(logicalAgent.id);
					agentGoalsGrouping.get(logicalAgent.id).add(goal);
				}
			}
		}
	}

	
	

	public void finishedWithGoal(Goal g){
		agent_goal_bookkeeping.remove(g);
	}


	public Goal selectGoal(SearchAgent agent, Node node){
		Goal g = selectGoal_boxGoalDist(agent, node, clusters.get(agent.id));
		if(g!=null){
			remove(g);
			agent_goal_bookkeeping.put(g, agent.id);
		}else if(!agentGoalsGrouping.get(agent.id).isEmpty()){
			//steal goal from someone else
			
			g=selectGoal_boxGoalDist(agent, node,agentGoalsGrouping.get(agent.id));
			agent_goal_bookkeeping.put(g, agent.id);
		}

		return g;
	}
	
	public void remove(Goal goal){
		ArrayList<Integer> agentsID= goalAgentsGrouping.get(goal);
		goalAgentsGrouping.remove(goal);
		for (Integer id : agentsID) {
			agentGoalsGrouping.get(id).remove(goal);
		}
	}
	
	private boolean goalInUse(Goal g){
		return ( agent_goal_bookkeeping.get(g) != null /*|| Heuristic.agent_goal_bookkeeping.get(g).intValue != this.agentID */);
	}

	@SuppressWarnings("unused")
	private Goal selectGoal_goalDist(SearchAgent agent, Node node){
		LogicalAgent lagent = node.agents[agent.id];
		ArrayList<Goal> goals = clusters.get(agent.id);
		Goal selectedGoal = goals.get(0);
		
		for( Goal goal : goals ){
			if( goalInUse(goal) )
				continue;

			if( node.distance(lagent, goal) < node.distance(lagent, selectedGoal) )
				selectedGoal = goal;
		}
		return selectedGoal;
	}

	private Goal selectGoal_boxGoalDist(SearchAgent agent, Node node,ArrayList<Goal> goals){
		LogicalAgent lAgent = node.agents[agent.id];

		// Selected Values
		Goal selectedGoal = null;
		@SuppressWarnings("unused")
		Box selectedBox = null;
		int dist = Integer.MAX_VALUE;

		for( Goal goal : goals ){
			if( goalInUse(goal) )
				continue;
			if(node.distance(goal, lAgent)== null){
				continue;
			}
			
			for( Box box : node.getBoxes(goal.getType()) ){
				if(node.distance(lAgent, box) != null && ( node.distance(lAgent, box)-1 + node.distance(box, goal)*goal.importance*2 ) < dist ){
					dist = node.distance(lAgent, box)-1 + node.distance(box, goal)*goal.importance;
					// Set the selects
					selectedGoal = goal;
					selectedBox = box;
				}
			}
		}

		return selectedGoal;
	}
	public void calculateCluster(LogicalAgent[] agents, Level level, boolean kcluster){
		if(kcluster){
			KClusteringGoals kcg = new KClusteringGoals(agents, level);
			this.clusters=kcg.getClusters();
		}else{
			for( int i = 0 ; i < agents.length ; i++ ){
				if( agents[i] != null ){
					ArrayList<Goal> goals=new ArrayList<>();
					for (Goal goal : level.getGoals(agents[i].color)) {
						if (level.distance(agents[i], goal)!=null) {
							goals.add(goal);
						}
					}
					this.clusters.put( agents[i].id, goals );
				}
					
			}
		}

	}




	public ArrayList<Goal> getCluster(int id) {
		
		return clusters.get(id);
	}




	public HashMap<Integer, ArrayList<Goal>> getClusters() {
		
		return clusters;
	}

}