package client.heuristic;

import java.util.ArrayList;

import client.heuristic.Heuristic;
import client.SearchAgent;

import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.LogicalAgent;

import client.node.Node;



public class ClearRouteHeuristic extends Heuristic{

	private ArrayList<Base> route;
	private int boxID;

	public ClearRouteHeuristic(SearchAgent agent, int boxID, ArrayList<Base> route){
		super(agent);
		this.boxID = boxID;
		this.route = new ArrayList<Base>();
		this.route.addAll(route);
	}

	@Override
	public int h(Node n){
		int f = 0;

		for( Base b : route ){
			Object o = n.objectAt(b);
			if( o instanceof LogicalAgent && ((LogicalAgent)o).id == agent.id ){
				f++;
			}else if( o instanceof Box && ((Box)o).id == this.boxID ){
				f++;
			}
		}

		return f;
		
//		int d = 0;
//		if(  n.distance(origin, n.agents[agent.id]) < n.distance(origin, n.getBoxesByID().get(this.boxID))){
//			d = n.distance(origin, n.agents[agent.id]);
//		}else{
//			d = n.distance(origin, n.getBoxesByID().get(this.boxID));
//		}
//
//
//		return RUN_AWAY_LITTLE_GIRL_RUN_AWAY - d;
	}



	public String toString(){
		return "Clearing evaluation";
	}
}