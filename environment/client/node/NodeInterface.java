package client.node;

import java.awt.Point;
import java.util.*;

import client.node.storage.*;

public interface NodeInterface {
	// Index query
	Object WTF(int row, int col);

	// Boxes
	Box boxAt(int row, int col);
	List<Box> getBoxes(char color);
	HashMap<Character, ArrayList<Box>> getAllBoxes();

	// Agents
	Agent[] getAgents();
	Agent agentAt(int row, int col);

	// Cells
	boolean cellIsFree(int row, int col);

	// Goals
	/*
	boolean isGoalState();
	boolean isGoalState(Color color);
	boolean isGoalState(Goal goal);
	*/

	// Relaxation
	/*
	Node subdomain(Color color);
	Node subdomain(Agent agent, Color color);
	*/


	// Search functions
	// getExpandedNodes();
	// extractPlan();
	// boolean isGoalState();
}