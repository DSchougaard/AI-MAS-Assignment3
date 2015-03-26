package client.node;

import java.awt.Point;
import java.util.*;

import client.node.storage.*;

public interface NodeInterface {
	// Index query
	public Object WTF(int row, int col);

	// Boxes
	public Box boxAt(int row, int col);
	public List<Box> getBoxes(char color);
	public Box[] getAllBoxes();

	// Agents
	public Agent[] getAgents();
	public Agent agentAt(int row, int col);

	// Cells
	public boolean cellIsFree(int row, int col);

	// Goals
	public boolean isGoalState();
	public boolean isGoalState(Color color);
	public boolean isGoalState(Goal goal);
	

	// Relaxation
	public Node subdomain(Color color);
	public Node subdomain(ArrayList<Agent> agents);
	public Node subdomain(Color color, Agent agent);
	


	// Search functions
	// public getExpandedNodes();
	// public extractPlan();
	// public boolean isGoalState();
}