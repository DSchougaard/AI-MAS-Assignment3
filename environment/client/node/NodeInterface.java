package client.node;

import java.awt.Point;
import java.util.*;

import client.node.storage.*;

public interface NodeInterface {
	// Boxes
	Box boxAt(int row, int col);
	List<Box> getBoxes(char color);
	HashMap<Character, ArrayList<Box>> getAllBoxes();

	// Agents
	List<Agent> getAgents();

	// Cells
	boolean cellIsFree(int row, int col);

	// Goals
	/*
	boolean isGoalState();
	boolean isGoalState(Color color);
	boolean isGoalState(Goal goal);
	*/


	// Search functions
	// getExpandedNodes();
	// extractPlan();
	// boolean isGoalState();
}