package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.Heuristic.*;
import client.Strategy.StrategyBestFirst;
import client.node.Node;
import client.node.map.BasicManhattanDistanceMap;
import client.node.map.Parser;
import client.node.storage.Agent;
import client.node.storage.SearchResult;
import client.node.storage.SearchResult.Result;
import client.ArgumentParser;

public class SearchClient {


	static BufferedReader serverMessages = new BufferedReader( new InputStreamReader( System.in ) );

	// Auxiliary static classes
	public static void error( String msg ) throws Exception {
		throw new Exception( "GSCError: " + msg );
	}

	public static class Memory {
		public static Runtime runtime = Runtime.getRuntime();
		public static final float mb = 1024 * 1024;
		public static final float limitRatio = .9f;
		public static final int timeLimit = 180;

		public static float used() {
			return ( runtime.totalMemory() - runtime.freeMemory() ) / mb;
		}

		public static float free() {
			return runtime.freeMemory() / mb;
		}

		public static float total() {
			return runtime.totalMemory() / mb;
		}

		public static float max() {
			return runtime.maxMemory() / mb;
		}

		public static boolean shouldEnd() {
			return ( used() / max() > limitRatio );
		}

		public static String stringRep() {
			return String.format( "[Used: %.2f MB, Free: %.2f MB, Alloc: %.2f MB, MaxAlloc: %.2f MB]", used(), free(), total(), max() );
		}
	}

	public Node state = null;
	
	public List< Agent > agents = new ArrayList< Agent >();

	private Agent agent;


	public SearchClient( BufferedReader serverMessages, SettingsContainer settings ) throws Exception {

		
		state=Parser.parse(serverMessages, settings);
		for (int i = 0; i < state.agents.length; i++) {
			if(state.agents[i]!=null){
				agents.add(state.agents[i]);
			}
		}
	}
	
	public SearchClient( BufferedReader serverMessages ) throws Exception {
		
		 SettingsContainer settings =new SettingsContainer();
		 settings.dm= new BasicManhattanDistanceMap();
		state=Parser.parse(serverMessages, settings);
		for (int i = 0; i < state.agents.length; i++) {
			if(state.agents[i]!=null){
				agents.add(state.agents[i]);
			}
		}
	}


	public SearchClient(Node initialState, Agent agent) {
		Node n=initialState.CopyNode();
		this.agent=agent;
		n.parent=null;
		this.state=n;
	}


	
	public SearchResult Search( Strategy strategy ) throws IOException {
		System.err.format( "Search starting with strategy %s\n", strategy );
		strategy.addToFrontier( this.state );

		int iterations = 0;
		while ( true ) {

			if ( iterations % 200 == 0 ) {
				System.err.println( strategy.searchStatus() );
			}
			if ( Memory.shouldEnd() ) {
				System.err.format( "Memory limit almost reached, terminating search %s\n", Memory.stringRep() );
				return new SearchResult(SearchResult.Result.MEMMORY, new LinkedList<>());
			}
			if ( strategy.timeSpent() > 300 ) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep() );
				return new SearchResult(SearchResult.Result.TIME, new LinkedList<>());
			}

			if ( strategy.frontierIsEmpty() ) {
				if(state.isGoalState(agent.color)){
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>());
				}else{
					return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
				}

			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState(agent.color)) {
				return new SearchResult(SearchResult.Result.PLAN,leafNode.extractPlan());
			}

			strategy.addToExplored( leafNode );

			for ( Node n : leafNode.getExpandedNodes(agent.id) ) {
				if ( !strategy.isExplored( n ) && !strategy.inFrontier( n ) ) {
					
					strategy.addToFrontier( n );
				}
			}
			iterations++;
		}
	}
	public static void executePlans(ArrayList< LinkedList< Node > > solutions, SearchClient client) throws IOException{
		StringBuilder builder= new StringBuilder();
		
		boolean done=false;
		while(!done){
			//creates the string
			builder.append('[');
			for (int i = 0; i < solutions.size(); i++) {
				if(solutions.get(i).isEmpty()){
					builder.append("NoOp");
				}else{
					builder.append(solutions.get(i).peek().action);
				}
				if(i!=solutions.size()-1){
					builder.append(',');
				}
			}
			builder.append(']');
			
			//communicate with server
			System.out.println(builder.toString());
			builder.setLength(0);
			
			String response;
			do{
				response = serverMessages.readLine();
			}while(response.equals(""));
			
			
			//updates state
			String[] strings=response.split(",");
			ArrayList<Command> commands= new ArrayList<>();
			for (int i = 0; i < strings.length; i++) {
				if(strings[i].contains("false")){
					//Illegal move (conflict)
					
					commands.add(new Command());
					solutions.get(i).clear();
				}else{
					if(!solutions.get(i).isEmpty()){
						commands.add(solutions.get(i).pop().action);
					}else{
						commands.add(new Command());
					}
				}
			}
			System.err.println(commands);
			client.state=client.state.excecuteCommands(commands);
			
			
			// evaluate if it should continue
			for (int i = 0; i < solutions.size(); i++) {
				if(solutions.get(i).isEmpty()){
					//should it be empty
					if(!client.state.isGoalState(client.state.agents[i].color)){
						done=true;
					}
				}
			}
		}
		
	}




	public static void main( String[] args ) throws Exception {
		
		SettingsContainer settings =ArgumentParser.parse(args);

		// Use stderr to print to console
		System.err.println( "SearchClient initializing. I am sending this using the error output stream." );

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient( serverMessages, settings );
		System.err.println("level loaded");
		//online planning loop
		ArrayList< LinkedList< Node > > solutions = new ArrayList<LinkedList<Node>>();
		for (Agent agent : client.agents) {
			solutions.add(new LinkedList< Node >());
		}
		while(!client.state.isGoalState()){
			
			
			boolean stuck=false;
			Strategy strategy = null;
			for (Agent agent : client.agents) {
				if(solutions.get(agent.id).isEmpty()){
					System.err.println("agent "+agent.id+" planing");
					SearchClient agentClient = new SearchClient( client.state, agent);
	//				strategy = new StrategyBestFirst( new Greedy( agentClient.state, agent.id ) );
					strategy = new StrategyBestFirst( new AStar( agentClient.state, agent.id ) );
	//				strategy = new StrategyBestFirst( new WeightedAStar( agentClient.state, agent.id ) );
					SearchResult result=agentClient.Search( strategy );

					
					if(result.reason==Result.STUCK){
						System.err.println("agent "+agent.id+" is stuck");
						stuck=true;

					}
					solutions.get(agent.id).addAll(result.solution);

				}else{
					System.err.println("agent "+agent.id+" using old plan");
				}
			}
			
			if( stuck){
				// solv stuck agents
//				solutions=Conflict.solve(solutions, client.agents);
//				System.err.println("!!!!!!!!!!!"+solutions.size());
			}
			
			
			executePlans(solutions, client);

		}

		System.err.println("done");
	}

	

}
