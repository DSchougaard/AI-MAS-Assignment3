package client.heuristic;

import client.heuristic.Heuristic;
import client.SearchAgent;
import client.node.Node;

public class Greedy extends Heuristic {

	public Greedy(SearchAgent agent) {
		super(agent);
	}
	

	public int f( Node n ) {
		return h( n );
	}

	public String toString() {
		return "Greedy evaluation";
	}
}