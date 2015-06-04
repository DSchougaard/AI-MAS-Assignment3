package client.node.level;

import java.util.ArrayList;
import java.util.HashMap;

import client.node.Color;
import client.node.storage.Base;
import client.node.storage.Goal;
import client.node.storage.LogicalAgent;


public interface LevelInterface {
	// General Level interaction
	public boolean isWall(int row, int col);

	// Goals
	public ArrayList<Goal> getGoals();
	public ArrayList<Goal> getGoals(char chr);
	public ArrayList<Goal> getGoals(Color color);

	// Distance Methods
	public Integer distance(int rowFrom, int colFrom, int rowTo, int colTo);
	public Integer distance(Base from, Base to);

	// Clusters
	public void calculateCluster(LogicalAgent[] agents, boolean kcluster);
	public HashMap<Integer, ArrayList<Goal>> getClusters();
	public ArrayList<Goal> getCluster(int agentID);

}