package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import client.Heuristic.*;
import client.Strategy.StrategyBestFirst;
import client.node.Node;
import client.node.map.Parser;
import client.node.storage.Agent;

public class SearchClient {


	

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

	public SearchClient( BufferedReader serverMessages ) throws Exception {
		
		state=Parser.parse(serverMessages);
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

	public LinkedList< Node > Search( Strategy strategy ) throws IOException {
		System.err.format( "Search starting with strategy %s\n", strategy );
		strategy.addToFrontier( this.state );

		int iterations = 0;
		while ( true ) {
			if ( iterations % 200 == 0 ) {
				System.err.println( strategy.searchStatus() );
			}
			if ( Memory.shouldEnd() ) {
				System.err.format( "Memory limit almost reached, terminating search %s\n", Memory.stringRep() );
				return new LinkedList<>();
			}
			if ( strategy.timeSpent() > 300 ) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep() );
				return new LinkedList<>();
			}

			if ( strategy.frontierIsEmpty() ) {
				System.err.println("empty");
				if(state.isGoalState(agent.color)){
					System.err.println("done");
					return new LinkedList<>();
				}else{
					System.err.println("conflict");
					return null;
				}

			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState(agent.color)) {
				System.err.println("Found a plan");
				return leafNode.extractPlan();
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



	/**
	 * Merges plans to an executable list of commands
	 * @param solutions
	 * @return
	 */
	public static String[] mergePlans(ArrayList< LinkedList< Node > > solutions){
		String[] solution;
		
		int size=0;
		for (int i = 0; i < solutions.size(); i++) {

			if(size<solutions.get(i).size()){
				size=solutions.get(i).size();
			}

		}
		solution = new String[size];
		for (int i = 0; i < solution.length; i++) {
			solution[i]="[";
		}

		for (int i = 0; i < solutions.size(); i++) {

			for (int k = 0; k < size; k++) {
				if(k<solutions.get(i).size()){
					if(solutions.get(i).get(k).action.actType==Command.type.NoOp){
						solution[k]+="NoOp";

					}else{
						solution[k]+=solutions.get(i).get(k).action.toString();
					}
					
				}else{
					solution[k]+="NoOp";
				}
				if(i!=solutions.size()-1){
					solution[k]+=",";
				}

			}
		}

		for (int i = 0; i < solution.length; i++) {
			solution[i]+="]";
		}

		System.err.println(Arrays.toString(solution));
		return solution;
	}



	public static void main( String[] args ) throws Exception {
		BufferedReader serverMessages = new BufferedReader( new InputStreamReader( System.in ) );

		

		// Use stderr to print to console
		System.err.println( "SearchClient initializing. I am sending this using the error output stream." );

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient( serverMessages );
		System.err.println("level loaded");
		//online planning loop
		while(!client.state.isGoalState()){
			
			ArrayList< LinkedList< Node > > solutions = new ArrayList<LinkedList<Node>>();
			boolean conflict=false;
			Strategy strategy = null;
			for (Agent agent : client.agents) {
				System.err.println("agent "+agent.id+" planing");
				SearchClient agentClient = new SearchClient( client.state, agent);
//				strategy = new StrategyBestFirst( new Greedy( agentClient.state, agent ) );
				strategy = new StrategyBestFirst( new AStar( agentClient.state, agent.id ) );
//				strategy = new StrategyBestFirst( new WeightedAStar( agentClient.state ) );
				
//				System.err.println(agentClient.state);
				LinkedList< Node > sol=agentClient.Search( strategy );
				
				if(sol==null){
					System.err.println("conflict!!!");
					conflict=true;
					agent.conflict=true;
					//what is the problem
//						agent / agent with box
//						box
					sol = new LinkedList<Node>();
					agentClient.state.action=new Command();
					sol.add(agentClient.state);
//					System.exit(0);
				}
//				System.err.println(sol);
				solutions.add(sol);

			}
			
			if( conflict){
				
				solutions=Conflict.solve(solutions, client.agents);
				System.err.println("!!!!!!!!!!!"+solutions.size());
			}
			
			
			String[] solution = mergePlans(solutions);
	

			if ( solution.length == 0 ) {
				System.err.println( "Unable to solve level" );
				System.exit( 0 );
			} else {
				System.err.println( "\nSummary for " + strategy );
				System.err.println( "Found solution of length " + solution.length );
				System.err.println( strategy.searchStatus() );

				int k=0;
				for ( String n : solution ) {
					System.out.println( n );
					//					System.err.println("execute "+n);
					String response;
					do{
						response = serverMessages.readLine();
					}while(response.equals(""));

					if ( response.contains( "false" ) ) {
						System.err.format( "Server responsed with %s to the inapplicable action: %s\n", response, n );
						System.err.format( "%s was attempted in \n%s\n", n, client.state );
						client.state=updateFailedState(client.state,solutions,k,response);
						break;
					}else{

						client.state=updateState(client.state,solutions,k);

					}
					k++;
				}
			}

		}

		System.err.println("done");
	}



	private static Node updateFailedState(Node n, ArrayList<LinkedList<Node>> solutions, int k, String response) {

		String[] strings=response.split(",");
		int size=0;
		for (int i = 0; i < solutions.size(); i++) {

			if(size<solutions.get(i).size()){
				size=solutions.get(i).size();
			}

		}
		ArrayList<Command> commands= new ArrayList<>();
		for (int i = 0; i < solutions.size(); i++) {
			if(k<solutions.get(i).size() && !strings[i].contains("false")){
				commands.add(solutions.get(i).get(k).action);
			}else{
				commands.add(null);
			}

		}
		return n.excecuteCommands(commands);
	}

	private static Node updateState(Node n, ArrayList<LinkedList<Node>> solutions, int k) {
		int size=0;
		for (int i = 0; i < solutions.size(); i++) {
			if(size<solutions.get(i).size()){
				size=solutions.get(i).size();
			}

		}
		ArrayList<Command> commands= new ArrayList<>();
		for (int i = 0; i < solutions.size(); i++) {
			if(k<solutions.get(i).size()){
				commands.add(solutions.get(i).get(k).action);
			}else{
				commands.add(null);
			}

		}
		return n.excecuteCommands(commands);
	}


}
