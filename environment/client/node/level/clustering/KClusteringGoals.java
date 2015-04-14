package client.node.level.clustering;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;


import client.node.level.Level;
import client.node.storage.*;



public class KClusteringGoals{
		
	private HashMap<Integer, ArrayList<Goal>> clusters;

	public KClusteringGoals(LogicalAgent[] parsedAgents, Level level){
		this.clusters = new HashMap<Integer, ArrayList<Goal>>();
		// Pre load all active agents into the cluster
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for( LogicalAgent a : parsedAgents ){
			if( a != null )
				agents.add(a.id);
		}

		// Preload goals. Takes time. Sorry.
		ArrayList<Goal> allGoals = level.getGoals();
		int agentCount = agents.size();
		int goalCount = allGoals.size();
		int startGoal = (new Random(System.nanoTime())).nextInt(goalCount-1);

		ArrayList<Goal> picked = new ArrayList<Goal>();
		picked.add(allGoals.get(startGoal));
		allGoals.remove(startGoal);

		// Apply a greedy k-centering algorithm, with k = number of agents
		int i = 1, minDist, maxDist;
		while( i < agentCount ){
			maxDist = 0;

			Goal nextGoal = null;
			for( Goal g1 : allGoals ){
				minDist = Integer.MAX_VALUE;
				for( Goal g2 : picked ){
					if( minDist > level.distance(g1, g2) )
						minDist = level.distance(g1, g2);
				}
				if( minDist > maxDist ){
					nextGoal = g1;
					maxDist = minDist;
				}
			}

			picked.add(nextGoal);
			allGoals.remove(nextGoal);
			i++;
		}

		if( picked.size() != agentCount )
			System.err.println("Something went wrong during cluseering");

		// For each selected cluster center, we apply it to it's own list.
		HashMap<Goal, ArrayList<Goal>> clustersByCenter = new HashMap<Goal, ArrayList<Goal>>();
		for( Goal g : picked ){
			ArrayList<Goal> t = new ArrayList<Goal>();
			t.add(g);
			clustersByCenter.put(g, t);
		}

		// For each of the remaining goals, deposit them into the CLOSEST cluster center's list.
		int distanceToCenter;
		for( Goal g : allGoals ){
			distanceToCenter = Integer.MAX_VALUE;
			Goal selectedCenter = null;
			for( Goal center : picked){
				if( distanceToCenter > level.distance(g, center) ){
					selectedCenter = center;
					distanceToCenter = level.distance(g, center);
				}
			}
			clustersByCenter.get(selectedCenter).add(g);
		}

		// Greedy pairing of agents and cluster centers
		for( Goal g : clustersByCenter.keySet() ){
			int centerToAgentDist = Integer.MAX_VALUE;
			Integer selectedAgent = null;
			for( Integer a : agents ){
				if( centerToAgentDist > level.distance(g, parsedAgents[a.intValue()]) ){
					centerToAgentDist = level.distance(g, parsedAgents[a.intValue()]);
					selectedAgent = a;
				}
			}

			ArrayList<Goal> clusterGoals = new ArrayList<Goal>();
			clusterGoals.add(g);
			clusterGoals.addAll( clustersByCenter.get(g) );
			this.clusters.put(selectedAgent, clusterGoals );
			agents.remove(selectedAgent);
		}
	}


	public ArrayList<Goal> getCluster(Integer agent){
		return this.clusters.get(agent);
	}

	public ArrayList<Goal> getCluster(int agentID){
		return this.clusters.get(new Integer(agentID));
	}

	public HashMap<Integer, ArrayList<Goal>> getClusters(){
		return this.clusters;
	}

	public void printCluster(){
		for( Integer i : this.clusters.keySet() ){
			System.err.print("[ " + i.intValue() + " ]: ");
			for( Goal g : this.clusters.get(i) )
				System.err.print(" {" + g.row + "," + g.col + "} ");
			System.err.println("");
		}
	}
}