package client.node;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import client.node.storage.Agent;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.Goal;

public interface NodeInterface {
	// Index query
	public Object WTF(int row, int col);

	// Boxes
	public Box boxAt(int row, int col);
	public Box[] getBoxes();
	public ArrayList<Box> getBoxes(Color color);
	public ArrayList<Box> getBoxes(char type);

	// Agents
	public Agent[] getAgents();
	public Agent agentAt(int row, int col);

	// Cells
	public boolean cellIsFree(int row, int col);

	// Goals
	public boolean isGoalState();
	public boolean isGoalState(Color color);
	public boolean isGoalState(Goal goal);
	public boolean isGoalState(ArrayList<Goal> goals);
	
	// Relaxation
	public Node subdomain(Color color);
	public Node subdomain(ArrayList<Agent> agents);
	public Node subdomain(Color color, Agent agent);


	
	// Search functions
	public ArrayList< Node > getExpandedNodes(int agentID);
	public LinkedList<Node> extractPlan();
	public int g();
	
	public ArrayList<Base> getRoute(); 
}