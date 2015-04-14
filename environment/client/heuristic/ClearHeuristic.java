package client.heuristic;

import java.util.ArrayList;

import client.Heuristic;

import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.LogicalAgent;

import client.SearchAgent;

import client.node.Node;



public class ClearHeuristic extends Heuristic{
	private ArrayList<Base> route;
	//private Box box;
	private int numObstructions;

	public ClearHeuristic(SearchAgent agent, int numObstructions, ArrayList<Base> route){
		super(agent);
		//this.box = box;
		this.numObstructions = numObstructions;
		this.route = new ArrayList<Base>();
		this.route.addAll(route);
	}

	@Override
	public int h(Node n){
		LogicalAgent a = n.agents[agent.id];

		int f = 0;

		for( Base b : route ){
			Object o = n.objectAt(b);	
			if( o instanceof Box || o instanceof LogicalAgent )
				f++;
		}

		return numObstructions - f;
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