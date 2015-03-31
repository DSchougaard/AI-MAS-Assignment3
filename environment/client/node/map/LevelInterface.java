package client.node.map;

import java.util.ArrayList;

import client.node.Color;
import client.node.storage.Base;
import client.node.storage.Goal;


public interface LevelInterface {

	// Goals
	ArrayList<Goal> getGoals();
	ArrayList<Goal> getGoals(char chr);
	ArrayList<Goal> getGoals(Color color);
	boolean isWall(int row, int col);

	int distance(int rowFrom, int colFrom, int rowTo, int colTo);
	int distance(Base from, Base to);

	void calculateCluster();
	HashMap<Agent, ArrayList<Goal>> getClusters();
	ArrayList<Goal> getCluster(Agent agent);

}