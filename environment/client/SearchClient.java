package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.Heuristic.*;
import client.parser.LevelParser;
import client.parser.ArgumentParser;
import client.parser.SettingsContainer;
import client.node.Node;
import client.node.storage.Goal;
import client.node.storage.SearchResult;
import client.node.level.distancemap.BasicManhattanDistanceMap;
import client.Strategy.StrategyBestFirst;
import client.SearchAgent.Status;

public class SearchClient {

	static BufferedReader serverMessages = new BufferedReader(
			new InputStreamReader(System.in));

	// Auxiliary static classes
	public static void error(String msg) throws Exception {
		throw new Exception("GSCError: " + msg);
	}


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
		settings.dm = new BasicManhattanDistanceMap();
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
	 * @param client
	 * @throws IOException
	 */
	public static void executePlans(ArrayList<LinkedList<Node>> solutions) throws IOException {
		StringBuilder builder = new StringBuilder();

		boolean done = false;
		while (!done) {
			// creates the string
			builder.append('[');
			for (int i = 0; i < solutions.size(); i++) {
				if (solutions.get(i).isEmpty()) {
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

	public static void main(String[] args) throws Exception {

		SettingsContainer settings = ArgumentParser.parse(args);

		// Use stderr to print to console
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");

		// Read level and create the initial state of the problem
		init(serverMessages, settings);
		System.err.println("level loaded");

		// online planning loop
		ArrayList<LinkedList<Node>> solutions = new ArrayList<LinkedList<Node>>();
		for (@SuppressWarnings("unused")
		SearchAgent agent : agents) {
			solutions.add(new LinkedList<Node>());
		}
		while (!state.isGoalState()) {

			if (agents.size() == 1) {
				SingleAgentPlaning(solutions);
			} else {
				MultiAgentPlaning( solutions);
			}
			System.err.println("-----------------------------------------------------------------");
			
			executePlans(solutions);

		}

		System.err.println("done");
	}

	private static void SingleAgentPlaning( ArrayList<LinkedList<Node>> solution) throws IOException {
		SearchAgent agent = agents.get(0);
		System.err.println("\nAgent " + agent.id + " planing");

		agent.setState(state);

		// normal search setup
		Heuristic heuristic = new AStar(agent);
		Strategy strategy = new StrategyBestFirst(heuristic);

		// find a subgoal(s) which should be solved
		Goal subgoal = heuristic.selectGoal(agent.state);
		if(subgoal!=null){
			agent.subgoals.add(subgoal);
			System.err.println("new subgoal "+subgoal.getType());
		}
		SearchResult result = agent.Search(strategy,  agent.subgoals);		

		solution.get(agent.id).addAll(result.solution);
	}

	private static void MultiAgentPlaning( ArrayList<LinkedList<Node>> solutions) throws IOException {
		boolean stuck = false;
		Strategy strategy = null;
		Strategy relaxedStrategy = null;
		for (SearchAgent agent : agents) {
			// only plan if there is not already a plan
			if (solutions.get(agent.id).isEmpty()) {
				if(agent.status == Status.HELPING){
					Conflict.doneHelping(agent);
					agent.status = Status.IDLE;
				}

				System.err.println("MultiAgentPlanning :: Agent " + agent.id + " planing");




				Heuristic heuristic = new Greedy(agent);


				// find a subgoal(s) which should be solved
				if(state.isGoalState(agent.subgoals)){
					Goal subgoal = heuristic.selectGoal(state);
					if(subgoal!=null){
						agent.subgoals.add(subgoal);
						System.err.println("new subgoal "+subgoal.getType());
					}
				}
				
				// relaxed search setup
				System.err.println("MA Planning :: Performing relaxed search");
				Node relaxed = state.subdomain(agent.id);
				Heuristic relaxedHeuristic = new Greedy(agent);
				relaxedStrategy = new StrategyBestFirst(relaxedHeuristic);
				agent.setState(relaxed);
				SearchResult relaxedResult = agent.Search(relaxedStrategy, agent.subgoals);
				System.gc();
				
				
				// normal search setup
				System.err.println("MA Planning :: Performing normal search");
				agent.setState(state);
				strategy = new StrategyBestFirst(heuristic);
				SearchResult result = agent.Search(strategy, agent.subgoals, relaxedResult);
				System.gc();
				
				switch (result.reason) {
				case STUCK:
					agent.status = Status.STUCK;

					System.err.println("MA Planning :: Agent " + agent.id + " is stuck.");
					System.err.println("\nSummary for " + relaxedStrategy);
					System.err.println("Found solution of length " + relaxedResult.solution.size());
					System.err.println(relaxedStrategy.searchStatus());
					stuck = true;
					solutions.get(agent.id).addAll(relaxedResult.solution);
					break;
				case DONE:
					agent.status=Status.DONE;
					break;
				default:
					agent.status=Status.PLAN;
					System.err.println("\nSummary for " + strategy);
					System.err.println("Found solution of length " + result.solution.size());
					System.err.println(strategy.searchStatus());
					solutions.get(agent.id).addAll(result.solution);
					break;
				}


			} else {
				System.err.println("agent " + agent.id + " using old plan");
			}
		}

		if (stuck) {
			// solve stuck agents
			solutions = Conflict.solve(state, solutions, agents);
			// System.err.println("!!!!!!!!!!!"+solutions.size());
		}
	}
}
