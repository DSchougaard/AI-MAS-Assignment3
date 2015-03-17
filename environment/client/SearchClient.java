package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import client.Heuristic.AStar;
import client.Heuristic.Greedy;
import client.Heuristic.WeightedAStar;
import client.Strategy.StrategyBFS;
import client.Strategy.StrategyBestFirst;
import client.Strategy.StrategyDFS;

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

	public Node initialState = null;

	public SearchClient( BufferedReader serverMessages ) throws Exception {
		Map< Character, String > colors = new HashMap< Character, String >();
		String line, color;

		int agentCol = -1, agentRow = -1;
		int colorLines = 0, levelLines = 0;

		// Read lines specifying colors
		while ( ( line = serverMessages.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
			line = line.replaceAll( "\\s", "" );
			String[] colonSplit = line.split( ":" );
			color = colonSplit[0].trim();

			for ( String id : colonSplit[1].split( "," ) ) {
				colors.put( id.trim().charAt( 0 ), color );
			}
			colorLines++;
		}
		
		if ( colorLines > 0 ) {
			error( "Box colors not supported" );
		}
		
		initialState = new Node( null );

		int max_column=0;
		ArrayList<String> tmpLines = new ArrayList<>();
		
		while(!line.equals("")){
			tmpLines.add(line);
			max_column=Math.max(line.length(), max_column);
			line=serverMessages.readLine();
		}

		initialState.init(tmpLines.size(), max_column);

		for (String string : tmpLines) {
			for (int i = 0; i < string.length(); i++) {
				char chr = string.charAt( i );
				if ( '+' == chr ) { // Walls
					Node.walls[levelLines][i] = true;
				} else if ( '0' <= chr && chr <= '9' ) { // Agents
					if ( agentCol != -1 || agentRow != -1 ) {
						error( "Not a single agent level" );
					}
					initialState.agentRow = levelLines;
					initialState.agentCol = i;
				} else if ( 'A' <= chr && chr <= 'Z' ) { // Boxes
					initialState.boxes[levelLines][i] = chr;
				} else if ( 'a' <= chr && chr <= 'z' ) { // Goal cells
					Node.goals[levelLines][i] = chr;
				}
			}
				levelLines++;
		}

	}

	public LinkedList< Node > Search( Strategy strategy ) throws IOException {
		System.err.format( "Search starting with strategy %s\n", strategy );
		strategy.addToFrontier( this.initialState );

		int iterations = 0;
		while ( true ) {
			if ( iterations % 200 == 0 ) {
				System.err.println( strategy.searchStatus() );
			}
			if ( Memory.shouldEnd() ) {
				System.err.format( "Memory limit almost reached, terminating search %s\n", Memory.stringRep() );
				return null;
			}
			if ( strategy.timeSpent() > 300 ) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep() );
				return null;
			}

			if ( strategy.frontierIsEmpty() ) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState() ) {
				return leafNode.extractPlan();
			}

			strategy.addToExplored( leafNode );
			for ( Node n : leafNode.getExpandedNodes() ) {
				if ( !strategy.isExplored( n ) && !strategy.inFrontier( n ) ) {
					strategy.addToFrontier( n );
				}
			}
			iterations++;
		}
	}
	
	public LinkedList< Node > Search( Strategy strategy, Agent agent) throws IOException {
		System.err.format( "Search starting with strategy %s\n", strategy );
		strategy.addToFrontier( this.initialState );

		int iterations = 0;
		while ( true ) {
			if ( iterations % 200 == 0 ) {
				System.err.println( strategy.searchStatus() );
			}
			if ( Memory.shouldEnd() ) {
				System.err.format( "Memory limit almost reached, terminating search %s\n", Memory.stringRep() );
				return null;
			}
			if ( strategy.timeSpent() > 300 ) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep() );
				return null;
			}

			if ( strategy.frontierIsEmpty() ) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState() ) {
				return leafNode.extractPlan();
			}

			strategy.addToExplored( leafNode );
			for ( Node n : leafNode.getExpandedNodes() ) {
				if ( !strategy.isExplored( n ) && !strategy.inFrontier( n ) ) {
					strategy.addToFrontier( n );
				}
			}
			iterations++;
		}
	}
	
	private static List< Agent > agents = new ArrayList< Agent >();
	
	
	public static void main( String[] args ) throws Exception {
		BufferedReader serverMessages = new BufferedReader( new InputStreamReader( System.in ) );
agents.add(new Agent('0', "red"));
		// Use stderr to print to console
		System.err.println( "SearchClient initializing. I am sending this using the error output stream." );

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient( serverMessages );

		Strategy strategy = null;
		strategy = new StrategyBFS();
		// Ex 1:
//		strategy = new StrategyDFS();
		
		// Ex 3:
//		strategy = new StrategyBestFirst( new AStar( client.initialState ) );
//		strategy = new StrategyBestFirst( new WeightedAStar( client.initialState ) );
		strategy = new StrategyBestFirst( new Greedy( client.initialState ) );

		ArrayList< LinkedList< Node > > solutions = new ArrayList<LinkedList<Node>>();
		
	
		for (Agent agent : agents) {
			solutions.add(client.Search( strategy , agent ));
		}
//		merge solutions
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
		int j=0;
		
		for (int i = 0; i < solutions.size(); i++) {
			for (int k = 0; k < solutions.get(i).size(); k++) {
				
				if(i!=solutions.size()-1){
					solution[k]+=solutions.get(i).get(k).action.toString()+",";
				}else{
					solution[k]+=solutions.get(i).get(k).action.toString();
				}
			}
		}
		for (int i = 0; i < solution.length; i++) {
			solution[i]+="]";
		}
		
		System.err.println(Arrays.toString(solution));
		if ( solution == null ) {
			System.err.println( "Unable to solve level" );
			System.exit( 0 );
		} else {
			System.err.println( "\nSummary for " + strategy );
			System.err.println( "Found solution of length " + solution.length );
			System.err.println( strategy.searchStatus() );

			for ( String n : solution ) {
				System.out.println( n );
				String response = serverMessages.readLine();
				if ( response.contains( "false" ) ) {
					System.err.format( "Server responsed with %s to the inapplicable action: %s\n", response, n );
					System.err.format( "%s was attempted in \n%s\n", n, n );
					break;
				}
			}
		}
	}
}
