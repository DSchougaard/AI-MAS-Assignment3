package client;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

import client.SearchClient.Memory;
import client.node.Node;

public abstract class Strategy  implements Comparator<Node> {
	private PriorityQueue< Node > queue; 
	private HashSet< Node > contains;
	public HashSet< Node > explored;
	public long startTime = System.currentTimeMillis();

	public Strategy() {
		explored = new HashSet< Node >();
		queue = new PriorityQueue<>(this);
		contains = new HashSet< Node >();
	}

	public void addToExplored( Node n ) {
		explored.add( n );
	}

	public boolean isExplored( Node n ) {
		return explored.contains( n );
	}

	public int countExplored() {
		return explored.size();
	}

	public String searchStatus() {
		return String.format( "#Explored: %4d, #Frontier: %3d, Time: %3.2f s \t%s", countExplored(), countFrontier(), timeSpent(), Memory.stringRep() );
	}
	
	public float timeSpent() {
		return ( System.currentTimeMillis() - startTime ) / 1000f;
	}

	public Node getAndRemoveLeaf() {
		Node n =queue.poll();
		contains.remove(n);
		return n;
	}

	public void addToFrontier( Node n ) {
		queue.add(n);
		contains.add(n);
	}

	public int countFrontier() {
		return queue.size();
	}

	public boolean frontierIsEmpty() {
		return queue.isEmpty();
	}

	public boolean inFrontier( Node n ) {
		return contains.contains(n);
	}
	
	public abstract String toString();
	
	public abstract int f(Node n);
	
	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}



	
}
