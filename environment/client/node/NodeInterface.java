package client.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import client.node.storage.Agent;
import client.node.storage.Box;
import client.node.storage.Goal;

public interface NodeInterface {
	// Index query
	public Object WTF(int row, int col);

	// Boxes
	public Box boxAt(int row, int col);
	public List<Box> getBoxes(char color);
	public HashMap<Character, ArrayList<Box>> getAllBoxes();

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