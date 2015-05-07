package client.heuristic;

import client.Strategy;
import client.node.Node;

public class AStar extends Strategy {
	public Heuristic heuristic;
	public AStar(Heuristic heuristic) {
		super();
		this.heuristic=heuristic;
	}

	public int f( Node n) {
		return n.g() + heuristic.h( n );
	}

	public String toString() {
		return "A* Search using " + heuristic.toString();
	}
}