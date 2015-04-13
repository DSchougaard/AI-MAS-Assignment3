package client.node;

import java.util.ArrayList;
import java.util.LinkedList;

import client.node.storage.LogicalAgent;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.Goal;

public interface NodeInterface {
	// Index query
	public Object objectAt(int row, int col);

	// Boxes
	public Box boxAt(int row, int col);
	public Box[] getBoxes();
	public ArrayList<Box> getBoxes(Color color);
	public ArrayList<Box> getBoxes(char type);

	// Agents
	public LogicalAgent[] getAgents();
	public LogicalAgent agentAt(int row, int col);

	// Cells
	public boolean cellIsFree(int row, int col);

	// Goals
	public char isGoal(int row, int col);
	public boolean isGoalState();
	public boolean isGoalState(Color color);
	public boolean isGoalState(Goal goal);
	public boolean isGoalState(ArrayList<Goal> goals);
	
	// Relaxation
	public Node subdomain(Color color);
	public Node subdomain(ArrayList<Integer> agents);
	public Node subdomain(int agent);

	// Search functions
	public ArrayList< Node > getExpandedNodes(int agentID);
	public LinkedList<Node> extractPlan();
	public int g();
	
	// Route
	public ArrayList<Base> getRoute(); 
}