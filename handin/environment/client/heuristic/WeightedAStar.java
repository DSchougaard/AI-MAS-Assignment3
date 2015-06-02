package client.heuristic;

import client.Strategy;
import client.node.Node;


public class WeightedAStar extends Strategy {
	private int W;
	Heuristic heuristic;
	public WeightedAStar(Heuristic heuristic) {
		super();
		this.heuristic=heuristic;
		W = 5; // You're welcome to test this out with different values, but for the reporting part you must at least indicate benchmarks for W = 5
	}

	public int f( Node n ) {
		return n.g() + W * heuristic.h( n );
	}

	public String toString() {
		return String.format( "WA*(%d) evaluation", W );
	}

}