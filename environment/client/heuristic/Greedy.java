package client.heuristic;

import client.Strategy;
import client.node.Node;

public class Greedy extends Strategy {

	public Heuristic heuristic;
	public Greedy (Heuristic heuristic) {
		super();
		this.heuristic=heuristic;
	}

	public int f( Node n) {
		return heuristic.h( n );
	}

	public String toString() {
		return "Greedy Search using " + heuristic.toString();
	}
}