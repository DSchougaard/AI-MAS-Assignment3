package client.heuristic;

import client.heuristic.Heuristic;
import client.SearchAgent;
import client.node.Node;

public class AStar extends Heuristic {
	public AStar(SearchAgent agent) {
		super(agent);
	}

	public int f( Node n) {
		return n.g() + h( n );
	}

	public String toString() {
		return "A* evaluation";
	}
}