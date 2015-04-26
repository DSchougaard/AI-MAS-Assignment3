package client.heuristic;

import java.util.ArrayList;

import client.SearchAgent;
import client.node.Node;
import client.node.storage.Base;
import client.node.storage.LogicalAgent;



public class OutOfTheWayHeuristic extends Heuristic{

	private final int OUT_OF_THE_WAY_THRESHOLD = 10;

	private ArrayList<Base> route;
	private int initRow, initCol;

	public OutOfTheWayHeuristic(SearchAgent agent, ArrayList<Base> route, int initRow, int initCol){
		super(agent);
		this.initRow = initRow;
		this.initCol = initCol;
		this.route = new ArrayList<Base>();
		this.route.addAll(route);
	}

	@Override
	public int h(Node n){
		LogicalAgent a = n.agents[agent.id];
		return OUT_OF_THE_WAY_THRESHOLD - n.distance(initRow, initCol, a.row, a.col);
	}

	public int f(Node n){
		// A* Search
		//return n.g + h(n);
		// Greedy Search
		return h(n);
	}

	public String toString(){
		return "Clearing evaluation";
	}
}