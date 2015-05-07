package client.heuristic;

import client.heuristic.Heuristic;
import client.SearchAgent;
import client.node.Node;
import client.node.storage.Box;

public class Proximity extends Heuristic{
	private Box box;
	public Proximity(SearchAgent agent, Box box){
		super(agent);
		this.box = box;
	}


	public String toString(){
		return "Proximity A* Evaluation";
	}

	@Override
	public int h(Node n){
		return n.distance(n.agents[agent.id], box) - 1;
	}
}