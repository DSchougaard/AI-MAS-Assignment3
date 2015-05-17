package client.node.level.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import client.node.Color;
import client.node.level.Level;
import client.node.storage.Goal;
import client.node.storage.LogicalAgent;



public class KClusteringGoals{

	private HashMap<Integer, ArrayList<Goal>> clusters;


	public KClusteringGoals(LogicalAgent[] AgentIDs, Level level){
		this.clusters = new HashMap<Integer, ArrayList<Goal>>();
		// Pre load all active agents into the cluster
		ArrayList<Integer> agentIDs = new ArrayList<Integer>();
		for( LogicalAgent a : AgentIDs ){
			if( a != null )
				agentIDs.add(a.id);
		}

		// Preload goals. Takes time. Sorry.
		ArrayList<Color> colors=level.getColors();

		for (Color color : colors) {
			ArrayList<LogicalAgent> agents = new ArrayList<>();
			for (LogicalAgent logicalAgent : AgentIDs) {
				if(logicalAgent!=null && logicalAgent.color==color){
					agents.add(logicalAgent);
				}
			}
			KClusteringGoal(agents, level.getGoals(color), level);
		}

	}
	public void KClusteringGoal(ArrayList<LogicalAgent> agents, ArrayList<Goal> goals, Level level){

		int centers=numberOfCenters(agents, goals, level);
		
		int startGoal = (new Random(System.nanoTime())).nextInt(goals.size());

		ArrayList<Goal> picked = new ArrayList<>();
		ArrayList<Goal> remaining = new ArrayList<>(goals);
		picked.add(remaining.get(startGoal));
		remaining.remove(startGoal);

		// Apply a greedy k-centering algorithm, with k = number of agents
		int minDist, maxDist;
		for(int i =0; i<centers-1;i++){
			maxDist = 0;

			Goal nextGoal = null;
			for( Goal g1 : remaining ){
				minDist = Integer.MAX_VALUE;
				for( Goal g2 : picked ){
					if(level.distance(g1, g2)!= null && minDist > level.distance(g1, g2) )
						minDist = level.distance(g1, g2);
				}
				if( minDist > maxDist ){
					nextGoal = g1;
					maxDist = minDist;
				}
			}

			picked.add(nextGoal);
			remaining.remove(nextGoal);

		}


		// For each selected cluster center, we apply it to it's own list.
		HashMap<Goal, ArrayList<Goal>> clustersByCenter = new HashMap<Goal, ArrayList<Goal>>();
		for( Goal g : picked ){
			ArrayList<Goal> t = new ArrayList<Goal>();
			t.add(g);
			clustersByCenter.put(g, t);

		}

		// For each of the remaining goals, deposit them into the CLOSEST cluster center's list.
		int distanceToCenter;
		for( Goal g : remaining	 ){
			distanceToCenter = Integer.MAX_VALUE;
			Goal selectedCenter = null;
			for( Goal center : picked){
				if(level.distance(g, center)!= null &&  distanceToCenter > level.distance(g, center) ){
					selectedCenter = center;
					distanceToCenter = level.distance(g, center);
				}
			}

			clustersByCenter.get(selectedCenter).add(g);
		}

		// Greedy pairing of agents and cluster centers
		for( Goal g : clustersByCenter.keySet() ){
			int centerToAgentDist = Integer.MAX_VALUE;
			LogicalAgent selectedAgent = null;
			for( LogicalAgent agent : agents ){
				if(level.distance(g, agent)!= null &&  centerToAgentDist > level.distance(g, agent) ){
					centerToAgentDist = level.distance(g, agent);
					selectedAgent = agent;
				}
			}

			ArrayList<Goal> clusterGoals = new ArrayList<Goal>();
			clusterGoals.addAll( clustersByCenter.get(g) );

			this.clusters.put(selectedAgent.id, clusterGoals );
			agents.remove(selectedAgent);


		}

	}
	
	private int numberOfCenters(ArrayList<LogicalAgent> agents, ArrayList<Goal> goals, Level level){
		
		ArrayList<Goal> regions= new ArrayList<>();

		for (Goal goal : goals) {
			boolean reachable =false;
			for (Goal goal2 : regions) {
				if(level.distance(goal, goal2)!=null){
					reachable=true;
					break;
				}
			}
			if(!reachable){

				regions.add(goal);
			}
		}
		int centers=agents.size();
		for (LogicalAgent agent : agents) {
			boolean isolated=true;
			for (Goal goal : regions) {
				if (level.distance(agent, goal)!=null) {
					isolated=false;
					break;
				}
			}
			if (isolated) {
				centers--;
			}
		}

		return centers;
	}
	
	public ArrayList<Goal> getCluster(int agentID){
		return this.clusters.get(agentID);
	}

	public HashMap<Integer, ArrayList<Goal>> getClusters(){
		return this.clusters;
	}

	public void printCluster(){
		if( Settings.Global.PRINT ){
			for( int i : this.clusters.keySet() ){
					System.err.print("[ " + i + " ]: ");
				

				for( Goal g : this.clusters.get(i) )
					System.err.println(g);
				System.err.println("");
			}
		}
	}
}