package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.SearchAgent.Status;
import client.heuristic.Heuristic;
import client.node.Node;
import client.node.level.distancemap.FloydWarshallDistanceMap;
import client.node.storage.ExpansionStatus;
import client.node.storage.Goal;
import client.node.storage.SearchResult;
import client.parser.ArgumentParser;
import client.parser.LevelParser;
import client.parser.SettingsContainer;
import client.parser.StrategyParser;
import client.utils.History;
import client.Settings;

public class SearchClient {

	

	// History settings
	public static History history;


	static BufferedReader serverMessages = new BufferedReader( new InputStreamReader(System.in) );

	public static class Memory {
		public static Runtime runtime = Runtime.getRuntime();
		public static final float mb = 1024 * 1024;
		public static final float limitRatio = .9f;
		public static final int timeLimit = 300;

		public static float used() {
			return (runtime.totalMemory() - runtime.freeMemory()) / mb;
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
			return (used() / max() > limitRatio);
		}

		public static String stringRep() {
			return String.format("[Used: %.2f MB, Free: %.2f MB, Alloc: %.2f MB, MaxAlloc: %.2f MB]",
							used(), free(), total(), max());
		}
	}

	public static Node state = null;
	static ExpansionStatus expStatus = null;

	// all the active agents in sorted order
	public static List<SearchAgent> agents = new ArrayList<>();

	public static void init(BufferedReader serverMessages, SettingsContainer settings) throws Exception {

		state = LevelParser.parse(serverMessages, settings);
		for (int i = 0; i < state.agents.length; i++) {
			if (state.agents[i] != null) {
				agents.add(new SearchAgent( state.agents[i]));
			}
		}
	}

	public static void init(BufferedReader serverMessages) throws Exception {

		SettingsContainer settings = new SettingsContainer();
		settings.dm = new FloydWarshallDistanceMap();
		state = LevelParser.parse(serverMessages, settings);
		for (int i = 0; i < state.agents.length; i++) {
			if (state.agents[i] != null) {
				agents.add(new SearchAgent( state.agents[i]));
			}
		}
	}


	

	/**
	 * Executes the plans until done or some things goes wrong
	 * 
	 * @param solutions
	 * @param SearchClient
	 * @throws IOException
	 */
	public static void executePlans(ArrayList<LinkedList<Node>> solutions) throws Exception {
		StringBuilder builder = new StringBuilder();

		boolean done = false;
		while (!done) {
			// creates the string
			builder.append('[');
			for (int i = 0; i < solutions.size(); i++) {
				if (solutions.get(i).isEmpty()) {
					if(agents.get(i).status != SearchAgent.Status.STUCK_HELPING && agents.get(i).status != SearchAgent.Status.STUCK){
						agents.get(i).status=SearchAgent.Status.IDLE;
					}
					builder.append("NoOp");
				} else {
					builder.append(solutions.get(i).peek().action);
				}
				if (i != solutions.size() - 1) {
					builder.append(',');
				}
			}
			builder.append(']');
			
			// communicate with server
			System.out.println(builder.toString());
			builder.setLength(0);

			String response;
			do {
				response = serverMessages.readLine();
			} while (response.equals(""));

			// updates state
			String[] strings = response.split(",");
			ArrayList<Command> commands = new ArrayList<>();
			for (int i = 0; i < strings.length; i++) {
				if (strings[i].contains("false")) {
					// Illegal move (conflict)

					commands.add(new Command());
					solutions.get(i).clear();
				} else {
					if (!solutions.get(i).isEmpty()) {
						commands.add(solutions.get(i).pop().action);
					} else {
						commands.add(new Command());
					}
				}
			}

			state = state.excecuteCommands(commands);

			// Dirty hack to determine whether or not it is multiagent
			if( commands.size() > 1 ){
				// Plug Cycle Detection into this place
				for( int i = 0 ; i < agents.size() ; i++ ){
					history.add(i, state.agents[i]);

					if( history.occurances(i, state.agents[i]) > Settings.SearchClient.CYCLE_THRESHOLD ){
						if( Settings.Global.PRINT){
							System.err.println("SearchClient::ExecutePlans:: Cycle Detected!");
						}
						agents.get(i).status = Status.STUCK;
						//solutions.get(i).clear();
						solutions = Conflict.solve(state, solutions, agents);						

					}
				}
			}

			// evaluate if it should continue
			for (int i = 0; i < solutions.size(); i++) {
				if (solutions.get(i).isEmpty()) {
					// should it be empty
					if (!state.isGoalState(state.agents[i].color)) {
						agents.get(i).status = Status.IDLE;
						done = true;
					}
				}
			}
		}

	}

	public static void main(String[] args) {
		try {
			


		SettingsContainer settings = ArgumentParser.parse(args);
		expStatus = new ExpansionStatus();

		// Use stderr to print to console
		if( Settings.Global.PRINT){
			System.err.println("SearchClient initializing. I am sending this using the error output stream.");
		}
		// Read level and create the initial state of the problem
		init(serverMessages, settings);
		if( Settings.Global.PRINT){
			System.err.println("Level loaded");
		}

		// online planning loop
		ArrayList<LinkedList<Node>> solutions = new ArrayList<LinkedList<Node>>();
		for (@SuppressWarnings("unused")SearchAgent agent : agents) {
			solutions.add(new LinkedList<Node>());
		}

		history = new History(agents.size(), Settings.SearchClient.HISTORY_LENGTH);

		while (!state.isGoalState()) {

			if (agents.size() == 1) {
				SingleAgentPlaning(solutions);
			} else {
				MultiAgentPlaning( solutions);
			}
			
			if( Settings.Global.PRINT){
				System.err.println("-----------------------------------------------------------------");
			}
			
			executePlans(solutions);

		}
		if( Settings.Global.PRINT){
			System.err.println("Level Completed");
		}
		
		} catch (Exception e) {
			System.err.println("DAMM");
			e.printStackTrace();
		}
	}

	private static void SingleAgentPlaning( ArrayList<LinkedList<Node>> solution) throws IOException {
		SearchAgent agent = agents.get(0);
		if( Settings.Global.PRINT){
			System.err.println("\nAgent " + agent.id + " planing");
		}
		
		agent.setState(state);

		// normal search setup
		Heuristic heuristic =new Heuristic(agent);
		Strategy strategy = StrategyParser.parse(heuristic,"Greedy");

		// find a subgoal(s) which should be solved
		if(state.isGoalState(agent.subgoals)){
			agent.selectNewGoal();
		}
		SearchResult result = agent.Search(strategy,  agent.subgoals);
		
		if (!result.equals(null)||!result.expStatus.equals(null)){
			expStatus.add(result.expStatus);
			if( Settings.Global.PRINT){
				System.err.println(expStatus.toString());
			}
		}	

		solution.get(agent.id).addAll(result.solution);
	}

	private static void MultiAgentPlaning( ArrayList<LinkedList<Node>> solutions) throws Exception {
		boolean stuck = false;
		Strategy strategy = null;
		Strategy relaxedStrategy = null;

		for (SearchAgent agent : agents) {
			// only plan if there is not already a plan
			if (solutions.get(agent.id).isEmpty()) {
				//if( agent.status == Status.CYCLE_DETECTED )
				//	System.err.println("SearchClient::MultiAgentPlanning::Cycle status found!");


				if( agent.status == Status.HELPING || agent.status == Status.STUCK_HELPING ){
					agent.status = Status.IDLE;
				}
				
				if( Settings.Global.PRINT){
					System.err.println("SearchClient :: MultiAgentPlanning :: Agent " + agent.id + " planing,");
					System.err.println("SearchClient :: MultiAgentPlanning :: Subgoals "+ agent.subgoals);
				}
				
				
				
				// find a subgoal(s) which should be solved
				if(state.isGoalState(agent.subgoals)){
					agent.setState(state);
					agent.selectNewGoal();
				}
				
				// relaxed search setup
				if( Settings.Global.PRINT){
					System.err.println("SearchClient :: MultiAgentPlanning :: Performing relaxed search");
				}
				Node relaxed = state.subdomain(agent.id);
				Heuristic relaxedHeuristic =new Heuristic(agent);
				relaxedStrategy =  StrategyParser.parse(relaxedHeuristic,"Greedy");//new Greedy(relaxedHeuristic);
				agent.setState(relaxed);
				SearchResult relaxedResult;

				relaxedResult = agent.Search(relaxedStrategy, agent.subgoals);
				if (!relaxedResult.equals(null)||!relaxedResult.expStatus.equals(null)){
					expStatus.add(relaxedResult.expStatus);
					if( Settings.Global.PRINT){
						if(Settings.Global.EXPANDED_DEBUG){
							System.err.println(expStatus.toString());
						}
					}
				}
				System.gc();
				
				
				// normal search setup
				if( Settings.Global.PRINT){
					System.err.println("SearchClient :: MultiAgentPlanning :: Performing normal search");
				}
				agent.setState(state);

				Heuristic heuristic = new Heuristic(agent);
				strategy = StrategyParser.parse(heuristic,"Greedy");
				SearchResult result = agent.Search(strategy, agent.subgoals, relaxedResult);
				if (!result.equals(null)||!result.expStatus.equals(null)){
					expStatus.add(result.expStatus);
					if( Settings.Global.PRINT){
						if(Settings.Global.EXPANDED_DEBUG)System.err.println(expStatus.toString());
					}
				}
				System.gc();
				switch (result.reason) {
				case STUCK:
					
					agent.status = Status.STUCK;
					if( Settings.Global.PRINT){
						System.err.println("=======================================================================");
						System.err.println("SearchClient :: MultiAgentPlanning :: Agent " + agent.id + " is stuck.");
						System.err.println("=======================================================================");
						System.err.println("\nSummary for " + relaxedStrategy);
						System.err.println("Found solution of length " + relaxedResult.solution.size());
						if(Settings.Global.EXPANDED_DEBUG) System.err.println(relaxedStrategy.searchStatus());
					}
					
					stuck = true;
					solutions.get(agent.id).addAll(relaxedResult.solution);
					break;
				case DONE:
					agent.status=Status.DONE;
					if( Settings.Global.PRINT){
						System.err.println("Done");
					}
					break;
				case IMPOSIBLE:
					throw new Exception("Imposible");
				default:
					agent.status=Status.PLAN;
					if( Settings.Global.PRINT){
						System.err.println("\nSearchClient :: MultiAgentPlanning :: Summary for " + strategy);
						System.err.println("SearchClient :: MultiAgentPlanning :: Found solution of length " + result.solution.size());
						if(Settings.Global.EXPANDED_DEBUG)System.err.println(strategy.searchStatus());
					}
					solutions.get(agent.id).addAll(result.solution);
					break;
				}


			} else {
				if( Settings.Global.PRINT){
					System.err.println("SearchClient :: MultiAgentPlanning :: Agent " + agent.id + " continuing existing plan.");
				}
			}
		}

		if (stuck) {
			// solve stuck agents
			solutions = Conflict.solve(state, solutions, agents);
		}
	}
}
