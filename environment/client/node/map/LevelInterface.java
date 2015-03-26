package client.node.map;

import java.awt.Point;
import java.util.*;

import client.node.storage.*;
import client.node.Color;


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