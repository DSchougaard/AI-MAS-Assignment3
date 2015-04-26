package client.heuristic;

import client.SearchAgent;
import client.node.Node;
import client.node.storage.LogicalAgent;


public class MoveToHeuristic extends Heuristic{
	private int row, col;

	public MoveToHeuristic(SearchAgent agent, int row, int col){
		super(agent);
		//this.box = box;
		this.row = row;
		this.col = col;
	}

	@Override
	public int h(Node n){
		LogicalAgent a = n.agents[agent.id];

		
		return n.distance(a.row, a.col, this.row, this.col);
	}

	public int f(Node n){
		// A* Search
		//return n.g + h(n);
		// Greedy Search
		return h(n);
	}

	public String toString(){
		return "Move To Heuristic";
	}
}