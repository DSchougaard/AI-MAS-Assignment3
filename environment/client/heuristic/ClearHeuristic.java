package client.heuristic;

import java.util.ArrayList;

import client.heuristic.Heuristic;
import client.SearchAgent;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.LogicalAgent;
import client.node.Node;



public class ClearHeuristic extends Heuristic{
	private ArrayList<Base> route;
	//private Box box;
	private int numObstructions;

	private ArrayList<Base> points = new ArrayList<>();

	public ClearHeuristic(SearchAgent agent, int numObstructions, ArrayList<Base> route){
		super(agent);
		//this.box = box;
		this.numObstructions = numObstructions;
		this.route = new ArrayList<Base>();
		this.route.addAll(route);

		points.add(route.get(0));
		points.add(route.get(route.size()/2));
		points.add(route.get(route.size()-1));

	}

	
	public int h2(Node n){

		int obstructions = 0;

		for( Base b : route ){
			Object o = n.objectAt(b);	
			if( o instanceof Box || o instanceof LogicalAgent )
				obstructions++;
		}

		return obstructions;
	}

	public int h(Node n){
		Integer tmpH=hs.get(n);
		if(tmpH==null){

			int obstructions = 0;
			int h=0;

			ArrayList<Box> boxes= new ArrayList<>();
			for( Base b : route ){
				Object o = n.objectAt(b);	
				if( o instanceof Box && ((Box) o).color==agent.color){
					boxes.add((Box) o);
					obstructions++;
					int min= Integer.MAX_VALUE;
					for (Base base : points) {
						//min=Math.min(min, n.distance((Box) o, base));
						h+=9999-n.distance((Box) o, base);
					}
					//h-=min;
				}
			}

			for( Base b : route ){
				Object o = n.objectAt(b);	
				if( o instanceof LogicalAgent && ((LogicalAgent)o).id == agent.id ){
					obstructions++;
					LogicalAgent lAgent=(LogicalAgent) o;
					int sum=0;
					int min= Integer.MAX_VALUE;
					for (Box box : boxes) {			
						min=Math.min(min, n.distance(lAgent, box));

					}
					h=min;
					//sum+=min;
				}
			}

			hs.put(n, h);
			return h;
		}else{
			return tmpH;
		}
	}

	public int f(Node n){
		// A* Search
		return n.g() + h(n);
		// Greedy Search
		// return h(n);
	}

	public String toString(){
		return "Clearing evaluation";
	}
}