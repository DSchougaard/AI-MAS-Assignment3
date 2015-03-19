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

import client.Heuristic.AStar;
import client.Heuristic.Greedy;
import client.Heuristic.WeightedAStar;
import client.Strategy.StrategyBestFirst;
import client.map.Level;

public class SearchClient {


	public enum Color{
		blue,red,green,cyan,magenta,orage,pink,yellow,noColor
	}


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

	public SearchClient( BufferedReader serverMessages ) throws Exception {
		HashMap< Character, Color > colors = new HashMap<>();
		String line, color;

		int agentCol = -1, agentRow = -1;
		int colorLines = 0, levelLines = 0;

		// Read lines specifying colors
		while ( ( line = serverMessages.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
			line = line.replaceAll( "\\s", "" );
			String[] colonSplit = line.split( ":" );
			color = colonSplit[0].trim();

			for ( String id : colonSplit[1].split( "," ) ) {
				Color colorEnum;
				switch (color) {
				case "red":
					colorEnum=Color.red;
					break;
				case "blue":
					colorEnum=Color.blue;
					break;
				case "green":
					colorEnum=Color.green;
					break;
				case "magenta":
					colorEnum=Color.magenta;
					break;
				case "cyan":
					colorEnum=Color.cyan;
					break;
				case "orange":
					colorEnum=Color.orage;
					break;
				case "pink":
					colorEnum=Color.pink;
					break;
				default:
					colorEnum=Color.noColor;
					break;
				}
				colors.put( id.trim().charAt( 0 ), colorEnum );
			}
			colorLines++;
		}

		if ( colorLines > 0 ) {

			System.err.println("Coloured expirence!!!");
			colors.forEach(
					(l,c)->{
						System.err.println(l+" "+c);
					}
					);
			colors.forEach(
					(l,c)->{
						if(Character.isDigit(l)){
							agents.add(new Agent(Integer.parseInt(l+""), c));
						}else{
							//											box color
						}
					}
					);
			//			error( "Box colors not supported" );
		}

		state = new Node( null, colors);

		int max_column=0;
		ArrayList<String> tmpLines = new ArrayList<>();

		while(!line.equals("")){
			tmpLines.add(line);
			max_column=Math.max(line.length(), max_column);
			line=serverMessages.readLine();
		}

		state.init(tmpLines.size(), max_column);

		for (String string : tmpLines) {
			for (int i = 0; i < string.length(); i++) {
				char chr = string.charAt( i );
				if ( '+' == chr ) { // Walls
					Node.walls[levelLines][i] = true;
				} else if ( '0' <= chr && chr <= '9' ) { // Agents
					int id =Integer.parseInt(chr+"");
					if(!agents.stream().filter(a -> a.id == id).findAny().isPresent()){
//						System.err.println("new Agent");
						agents.add(new Agent(id));
					}
					
					state.agents[id][0]=levelLines;
					state.agents[id][1]=i;
//					if ( agentCol != -1 || agentRow != -1 ) {
//						System.err.println( "Not a single agent level" );
//					}
//					state.agentRow = levelLines;
//					state.agentCol = i;
				} else if ( 'A' <= chr && chr <= 'Z' ) { // Boxes
					if(!colors.keySet().contains(chr)){
						System.err.println(colors.keySet());
						colors.put(chr, Color.noColor);
					}
					state.boxes[levelLines][i] = chr;
				} else if ( 'a' <= chr && chr <= 'z' ) { // Goal cells
					Node.goals[levelLines][i] = chr;
				}
			}
			levelLines++;
		}

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
				return null;
			}
			if ( strategy.timeSpent() > 300 ) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep() );
				return null;
			}

			if ( strategy.frontierIsEmpty() ) {
				System.err.println("empty");
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState() ) {
				System.err.println("plan");
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
		strategy.addToFrontier( this.state );

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
				System.err.println("empty");
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState() ) {
				System.err.println("plan");
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


	/**
	 * Merges plans to an executable list of commands
	 * @param solutions
	 * @return
	 */
	public static String[] mergePlans(ArrayList< LinkedList< Node > > solutions){
		String[] solution;

		int size=0;
		for (int i = 0; i < solutions.size(); i++) {
			//			System.err.println(solutions.get(i));
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
					solution[k]+=solutions.get(i).get(k).action.toString();
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

		//online planning loop
		while(true){
			ArrayList< LinkedList< Node > > solutions = new ArrayList<LinkedList<Node>>();
	
			Strategy strategy = null;
			for (Agent agent : agents) {
	
				Node AgentInitialState= modAgentIntialState(client.state, agent);
	
				strategy = new StrategyBestFirst( new AStar( AgentInitialState) );
				//			strategy = new StrategyBestFirst( new WeightedAStar( AgentInitialState ) );
				//			strategy = new StrategyBestFirst( new Greedy( AgentInitialState ) );
				solutions.add(client.Search( strategy ));
				//			System.err.println(solutions.get(solutions.size()-1));
				//			System.err.println("-----------------------------------------------------");
	
			}
			//single agent
			if(agents.size()==0){
				strategy = new StrategyBestFirst( new Greedy( client.state ) );
				solutions.add(client.Search( strategy ));
				//			System.err.println(solutions.get(solutions.size()-1));
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
					System.err.println("execute "+n);
//					String response = serverMessages.readLine();
					String response;
					do{
						response = serverMessages.readLine();
					}while(response.equals(""));
				
					if ( response.contains( "false" ) ) {
						System.err.format( "Server responsed with %s to the inapplicable action: %s\n", response, n );
						System.err.format( "%s was attempted in \n%s\n", n, client.state );
//						TODO: update failed state
						client.state=updateFailedState(client.state,solutions,k,response);
						break;
					}else{

						client.state=updateState(client.state,solutions,k);
						System.err.println(client.state);
						
					}
					k++;
				}
			}
			
			System.err.println("done");
			System.exit(0);//tmp
		}
	}



	private static Node updateFailedState(Node n, ArrayList<LinkedList<Node>> solutions, int k, String response) {

		String[] strings=response.split(",");
		
		int size=0;
		for (int i = 0; i < solutions.size(); i++) {
			//			System.err.println(solutions.get(i));
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
			//			System.err.println(solutions.get(i));
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

	/**
	 * creates an relaxed world for each agent, by removing things 
	 * @param initialState
	 * @param agent
	 * @return
	 */
	private static Node modAgentIntialState(Node initialState, Agent agent) {
		// TODO Auto-generated method stub

		// TODO create new world by modifing intial state



		// TODO exchange wrong colored boxes with walls

		// TODO remove wrong colored goals 



		//		n.init(initialState.level);
		//		
		//		boolean[][] walls = initialState.walls;
		//		char[][] goals = initialState.goals;
		//		
		//		
		//		
		//		char[][] boxes = n.boxes; 
		//		
		initialState.agent=agent;
		return initialState;
	}
}
