package client.node.map;

import java.util.ArrayList;
import java.util.HashMap;

import client.node.Color;
import client.node.storage.Base;
import client.node.storage.Goal;


public interface LevelInterface {

	// Goals
	ArrayList<Goal> getGoals(char chr);
	HashMap<Character, ArrayList<Goal>> getGoalMap();
	ArrayList<Goal> getAllGoals();
	ArrayList<Goal> getGoalsByColor(Color color);
	boolean isWall(int row, int col);

	int distance(int rowFrom, int colFrom, int rowTo, int colTo);
	int distance(Base from, Base to);
}