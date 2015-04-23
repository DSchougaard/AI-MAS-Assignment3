package client;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import client.SearchClient.Memory;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;
import client.node.Node;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.ExpansionStatus;
import client.node.storage.Goal;
import client.node.storage.LogicalAgent;
import client.node.storage.SearchResult;

public class SearchAgent{
	
	public final int id;
	public final Color color;
	public enum Status{STUCK, PLAN, DONE, IDLE, HELPING}
	public Status status = Status.IDLE;
	public ArrayList<Goal> subgoals = new ArrayList<>();
	
	public Node state;
	public static int searchMaxOffset = 2;
	
	
	public SearchAgent(int name, Color color, int row, int col){
		this.id 	= name;
		if(color==null){
			this.color = Color.blue;
		}else{
			this.color = color;
		}
	}

	public SearchAgent(char name, int row, int col){
		this.id 	= (int)name;
		this.color 	= Color.blue;
	}

	
	public SearchAgent(SearchAgent agent) {
		this.id=agent.id;
		this.color=agent.color;
	}

	public SearchAgent(LogicalAgent agent) {
		this.id=agent.id;
		this.color=agent.color;
	}

	public void setState(Node state){
		Node n = state.CopyNode();
		n.parent = null;
		this.state = n;
	}
	
	public SearchResult Search(Strategy strategy) throws IOException {
		return Search(strategy,  this.state.getGoals(state.agents[id].color), null);
	}

	public SearchResult Search(Strategy strategy, ArrayList<Goal> goals) throws IOException {
		return Search(strategy, goals, null);
	}

	public SearchResult Search(Strategy strategy, SearchResult preResult) throws IOException {
		return Search(strategy, this.state.getGoals(state.agents[id].color), preResult);
	}

	public SearchResult Search(Strategy strategy, ArrayList<Goal> goals, SearchResult preResult) throws IOException {
		System.err.format("Search starting with strategy %s\n", strategy);
		strategy.addToFrontier(this.state);

		int iterations = 0;
		while (true) {

			if (iterations % 2000 == 0) {
				System.err.println(strategy.searchStatus());
			}
			if (Memory.shouldEnd()) {
				System.err.format("Memory limit almost reached, terminating search %s\n", Memory.stringRep());
				return new SearchResult(SearchResult.Result.MEMMORY, new LinkedList<>());
			}
			if (strategy.timeSpent() > Memory.timeLimit) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep());
				return new SearchResult(SearchResult.Result.TIME,
						new LinkedList<>());
			}

			if (strategy.frontierIsEmpty()) {
				if (state.isGoalState(goals)) {
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>());
				} else if (preResult != null) {
					return new SearchResult(SearchResult.Result.IMPOSIBLE, new LinkedList<>());
				} else {
					return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
				}
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if (preResult != null && leafNode.g() > (preResult.solution.size() * searchMaxOffset)) {
				if(preResult.reason==SearchResult.Result.DONE){
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>());
					
				}
				return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
			}

			if (leafNode.isGoalState(goals)) {
				ExpansionStatus expStatus = new ExpansionStatus(strategy);
				System.err.println(strategy.searchStatus());
				if(leafNode.isInitialState()){
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>(),expStatus);
				}
				return new SearchResult(SearchResult.Result.PLAN, leafNode.extractPlan(),expStatus);
			}

			strategy.addToExplored(leafNode);

			for (Node n : leafNode.getExpandedNodes(id)) {
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {

					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}
		
	}
	
	public SearchResult ProximitySearch(Strategy strategy, Box box) throws IOException {
		System.err.println("SearchClient :: Starting ProximitySearch.");
		strategy.addToFrontier(this.state);

		while(true){
			if (strategy.frontierIsEmpty()) {
				if (state.isGoalState(id, box)) {
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>());
				} else {
					return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
				}
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if (leafNode.isGoalState(id, box)) {
				if(leafNode.isInitialState()){
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>());
				}
				return new SearchResult(SearchResult.Result.PLAN, leafNode.extractPlan());
			}

			strategy.addToExplored(leafNode);

			for (Node n : leafNode.getExpandedNodes(id)) {
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
		}
	}

	
	public SearchResult ClearRouteSearch(Strategy strategy, int numObstructions, ArrayList<Base> route){
		System.err.println("SearchClient :: Starting Route Clearing Search");
		strategy.addToFrontier(this.state);

		while(true){
			if (strategy.frontierIsEmpty()) {
				if (state.isGoalState(id, numObstructions, route)) {
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>());
				} else {
					return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
				}
			}

			Node leafNode = strategy.getAndRemoveLeaf();
			System.err.println(leafNode);

			if (leafNode.isGoalState(id, numObstructions, route)) {
				return new SearchResult(SearchResult.Result.PLAN, leafNode.extractPlan());
			}

			strategy.addToExplored(leafNode);

			for (Node n : leafNode.getExpandedBoxNodes(id)) {
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
		}
	}
	
	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() ){
			return false;
		}


		SearchAgent b = (SearchAgent)obj;
		return ( this.id == b.id && this.color == b.color );
	}
	
	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 5;
		result = prime * result + this.id;
		return result;
	}
}
