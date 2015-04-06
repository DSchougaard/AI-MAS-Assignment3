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
import client.node.storage.Agent.Status;
import client.node.storage.Goal;
import client.node.storage.SearchResult;
import client.node.storage.SearchResult.Result;
import client.ArgumentParser;

public class SearchClient {


	static BufferedReader serverMessages = new BufferedReader( new InputStreamReader( System.in ) );

	// Auxiliary static classes
	public static void error( String msg ) throws Exception {
		throw new Exception( "GSCError: " + msg );
	}

	public static int searchMaxOffset = 2;

	public static class Memory {
		public static Runtime runtime = Runtime.getRuntime();
		public static final float mb = 1024 * 1024;
		public static final float limitRatio = .9f;
		public static final int timeLimit = 300;

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

	//all the active agents in sorted order
	public List< Agent > agents = new ArrayList< Agent >();


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


	public SearchClient(Node initialState) {
		Node n=initialState.CopyNode();
		n.parent=null;
		this.state=n;
	}

	public SearchResult Search( Strategy strategy, int agentID) throws IOException {
		return Search(strategy, agentID, this.state.getGoals(state.agents[agentID].color), null);
	}

	public SearchResult Search( Strategy strategy, int agentID, ArrayList<Goal> goals) throws IOException {
		return Search(strategy, agentID, goals , null);
	}


	public SearchResult Search( Strategy strategy, int agentID, SearchResult preResult  ) throws IOException {
		return Search(strategy, agentID, this.state.getGoals(state.agents[agentID].color), preResult );
	}

	public SearchResult Search( Strategy strategy, int agentID, ArrayList<Goal> goals , SearchResult preResult ) throws IOException {
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
			if ( strategy.timeSpent() > Memory.timeLimit ) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep() );
				return new SearchResult(SearchResult.Result.TIME, new LinkedList<>());
			}


			if ( strategy.frontierIsEmpty() ) {
				if(state.isGoalState(goals)){
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>());
				}
				else if (preResult != null){
					return new SearchResult(SearchResult.Result.IMPOSIBLE, new LinkedList<>());
				}
				else{
					return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
				}

			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( preResult != null && leafNode.g() > ( preResult.solution.size() * searchMaxOffset ) ){
				return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
			}

			if ( leafNode.isGoalState(goals)) {
				return new SearchResult(SearchResult.Result.PLAN,leafNode.extractPlan());
			}

			strategy.addToExplored( leafNode );

			for ( Node n : leafNode.getExpandedNodes(agentID) ) {
				if ( !strategy.isExplored( n ) && !strategy.inFrontier( n ) ) {

					strategy.addToFrontier( n );
				}
			}
			iterations++;
		}
	}

	/**
	 * Executes the plans until done or some things goes wrong
	 * 
	 * @param solutions
	 * @param client
	 * @throws IOException
	 */
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

			client.state=client.state.excecuteCommands(commands);


			// evaluate if it should continue
			for (int i = 0; i < solutions.size(); i++) {
				if(solutions.get(i).isEmpty()){
					//should it be empty
					if(!client.state.isGoalState(client.state.agents[i].color)){
						client.state.agents[i].status=Status.IDLE;
						done=true;
					}
				}
			}
		}

	}




	public static void main( String[] args ) throws Exception {

		SettingsContainer settings = ArgumentParser.parse(args);

		// Use stderr to print to console
		System.err.println( "SearchClient initializing. I am sending this using the error output stream." );

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient( serverMessages, settings );
		System.err.println("level loaded");


		//online planning loop
		ArrayList< LinkedList< Node > > solutions = new ArrayList<LinkedList<Node>>();
		for (@SuppressWarnings("unused") Agent agent : client.agents) {
			solutions.add(new LinkedList< Node >());
		}
		while(!client.state.isGoalState()){

			if (client.agents.size()==1) {
				SingleAgentPlaning(client, solutions);
			}else{
				MultiAgentPlaning(client, solutions);
			}
		

			executePlans(solutions, client);

		}

		System.err.println("done");
	}


	private static void SingleAgentPlaning(SearchClient client, ArrayList< LinkedList< Node > > solution) throws IOException{
		Agent agent= client.agents.get(0);
		System.err.println("agent "+agent.id+" planing");	

		SearchClient agentClient = new SearchClient( client.state);

		//normal search setup
		Heuristic heuristic = new AStar( agentClient.state, agent.id );
		Strategy strategy = new StrategyBestFirst( heuristic );	


		//find a subgoal(s) which should be solved
		Goal subgoal=heuristic.selectGoal();
		ArrayList<Goal> subgoals= new ArrayList<>();
		subgoals.add(subgoal);


		SearchResult result=agentClient.Search( strategy, agent.id, subgoals );


		solution.get(agent.id).addAll(result.solution);

	}
	
	private static void MultiAgentPlaning(SearchClient client, ArrayList< LinkedList< Node > > solutions) throws IOException{
		boolean stuck=false;
		Strategy strategy = null;
		Strategy relaxedStrategy = null;
		for (Agent agent : client.agents) {
			//only plan if there is not already a plan
			if(solutions.get(agent.id).isEmpty()){
				System.err.println("agent "+agent.id+" planing");	

				SearchClient agentClient = new SearchClient( client.state);

				//relaxed search setup
				Node relaxed =agentClient.state.subdomain(agent.color, agent);
				Heuristic relaxedHeuristic = new AStar( relaxed, agent.id );
				relaxedStrategy = new StrategyBestFirst( relaxedHeuristic );	


				//normal search setup
				Heuristic heuristic = new AStar( agentClient.state, agent.id );
				strategy = new StrategyBestFirst( heuristic );	


				//find a subgoal(s) which should be solved
				Goal subgoal=heuristic.selectGoal();
				ArrayList<Goal> subgoals= new ArrayList<>();
				subgoals.add(subgoal);




				SearchResult relaxedResult=agentClient.Search( relaxedStrategy, agent.id, subgoals );

				SearchResult result=agentClient.Search( strategy, agent.id, subgoals, relaxedResult );


				if(result.reason==Result.STUCK){
					agent.status=Status.STUCK;

					System.err.println("agent "+agent.id+" is stuck");
					System.err.println( "\nSummary for " + relaxedStrategy );
					System.err.println( "Found solution of length " + relaxedResult.solution.size() );
					System.err.println( relaxedStrategy.searchStatus() );
					stuck=true;
					solutions.get(agent.id).addAll(relaxedResult.solution);
				}else{

					System.err.println( "\nSummary for " + strategy );
					System.err.println( "Found solution of length " + result.solution.size() );
					System.err.println( strategy.searchStatus() );
					solutions.get(agent.id).addAll(result.solution);
				}




			}else{
				System.err.println("agent "+agent.id+" using old plan");
			}
		}

		if(stuck){
			// solve stuck agents
			solutions=Conflict.solve(solutions, client.agents);
			//				System.err.println("!!!!!!!!!!!"+solutions.size());
		}
	}
}
