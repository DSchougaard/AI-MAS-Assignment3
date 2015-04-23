package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.Deque;


import client.heuristic.Proximity;
import client.heuristic.ClearHeuristic;
import client.heuristic.*;
import client.SearchAgent.Status;
import client.Strategy.StrategyBestFirst;
import client.node.Node;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.LogicalAgent;
import client.node.storage.SearchResult;
import client.node.storage.SearchResult.Result;
import client.parser.RouteParser;
import client.node.GoalState;
import client.node.GoalState.*;
import java.util.Comparator;
// Searches

import client.heuristic.MoveToHeuristic;
import client.heuristic.OutOfTheWayHeuristic;

public class Conflict{

	/*private static class AgentEntry implements Comparator<AgentEntry>{
		private int id, weight;
		public AgentEntry(int id, int weight){
			this.id = id;
			this.weight = weight;
		}

		@Override
	    public int comparetor(AgentEntry ae1, AgentEntry ae2) {
	        return Integer.compare(ae1.weight, ae2.weight);
	    }
	}*/

	// Helping Agent -> Agent Receiving Help
	private static HashMap<SearchAgent, SearchAgent> helping = new HashMap<>();

	public static void doneHelping(SearchAgent helpingAgent){
		if(	helping.get(helpingAgent) != null ){
			helping.get(helpingAgent).status = Status.PLAN;
		}
	}

	private final int AGENT_IN_THE_ROUTE_THRESHOLD 				= 10;
	private final int BOX_IN_THE_ROUTE_THRESHOLD 				= 15;
	private final int DISTANCE_PLAN_THRESHOLD 					= 3;
	private final int COMPLETE_CURRENT_PLAN_THRESHOLD 			= 5;

	public static ArrayList< LinkedList< Node > > solve(Node node, ArrayList< LinkedList< Node > > solutions, List< SearchAgent > agents) throws IOException{
		
		Deque<SearchAgent> needs_help = new LinkedList<>();
		HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved = new HashMap<SearchAgent, ArrayList<LogicalAgent>>();
		HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved = new HashMap<SearchAgent, ArrayList<Box>>();

		for( SearchAgent sa : agents ){
			if( sa.status == SearchAgent.Status.STUCK || sa.status == SearchAgent.Status.STUCK_HELPING ){
				needs_help.addFirst(sa);

				// Get the stuck agents route
				ArrayList<Base> route = RouteParser.parse(solutions.get(sa.id), sa.id);

				// Lists to keep track of obstructions in route
				ArrayList<LogicalAgent> agentsInTheWay = new ArrayList<>();
				ArrayList<Box> boxesInTheWay = new ArrayList<>();
				int numBoxes = 0;

				// Loop over route, identifying obstructions in the route
				for( Base b : route ){
					Object o = node.objectAt(b);
					if( o instanceof LogicalAgent ){
						System.err.println("Conflict :: Agent found in route for agent " + sa.id + "!");
						// I know, I know. Ugly syntax. Get ID of LogicalAgent in the way
						// and insert corrosponding SearchAgent into list.
						agentsInTheWay.add( (LogicalAgent)o );	

					}else if( o instanceof Box ){
						System.err.println("Conflict :: Box found in route for agent " + sa.id + "!");
						if( sa.color != ((Box)o).color ){
							System.err.println("            Color of box: " + ((Box)o).color + ".");
							boxesInTheWay.add( (Box)o );
						}else{
							numBoxes++;
						}
					}
				}
				needs_agents_moved.put(sa, agentsInTheWay);
				needs_boxes_moved.put(sa, boxesInTheWay);
			}
		}		


		while( !needs_help.isEmpty() ){
			System.err.println("Dowop");
			SearchAgent sa = needs_help.pollFirst();
			
			ArrayList<Box> move_boxes = needs_boxes_moved.get(sa);
			needs_boxes_moved.remove(sa);
			if( move_boxes == null )
				move_boxes = new ArrayList<Box>();

			ArrayList<LogicalAgent> move_agents = needs_agents_moved.get(sa);
			needs_agents_moved.remove(sa);
			if( move_agents == null )
				move_agents = new ArrayList<LogicalAgent>();


			// Fucking dirty test
			/*if( Conflict.helping.containsKey(sa) ){
				SearchAgent receivingHelp = Conflict.helping.get(sa);

				if( receivingHelp.status == SearchAgent.Status.STUCK )
					receivingHelp.status = SearchAgent.Status.IDLE;
					solutions.get(receivingHelp.id).clear();
			}*/




			if( sa == null )
				System.err.println("Conflict :: SA was NULL.");

			if( needs_boxes_moved == null )
				System.err.println("Conflict :: Needs_Boxes_Moved was NULL.");

			if( needs_boxes_moved.size() == 0 )
				System.err.println("Conflict :: Needs_Boxes_Moved is empty.");

			if( !needs_boxes_moved.containsKey(sa) )
				System.err.println("Conflict :: SA needs no boxes.");


			for( Box b : move_boxes ){
				if( b == null )
					System.err.println("Conflict :: Box was NULL.");

				System.err.println("Conflict :: Moving box.");
				resolveBoxConflict(sa, b, needs_boxes_moved.size()+needs_agents_moved.size(), node, agents, solutions, needs_help, needs_agents_moved, needs_boxes_moved);
			}

			for( LogicalAgent la : move_agents ){
				if( agents.get(la.id).status == Status.HELPING )
						continue;
				System.err.println("Conflict :: Moving agent " + la.id + " out of agent " + sa.id + "'s route.");
				resolveAgentConflict(solutions, node, sa, agents.get(la.id), RouteParser.parse(solutions.get(sa.id), sa.id), needs_help, needs_agents_moved, needs_boxes_moved);
			}

		}

		return solutions;
	}

	private static void resolveBoxConflict(SearchAgent needingHelp, Box box, int obstructions, Node node, List<SearchAgent> agents,ArrayList< LinkedList< Node > > solutions, Deque<SearchAgent> needs_help, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved) throws IOException{
		
		// PLUG AND PLAY AGENT SELECT

		// First we select the agent best suited for the job
		ArrayList<Integer> possibleAgents = node.getAgentIDs(box.color);
		int distanceClosest = Integer.MAX_VALUE, selectedAgent = -1;
		for( int i : possibleAgents ){
			if( node.distance(box, node.agents[i]) < distanceClosest ){
				selectedAgent = i;
				distanceClosest = node.distance(box, node.agents[i]);
			}
		}
		System.err.println("Conflict :: ResolveBoxConflict :: Selected Agent " + selectedAgent);
		SearchAgent sa = agents.get(selectedAgent);
		sa.status = Status.HELPING;
		Conflict.helping.put(needingHelp, sa);
		// Agent Select end.


		// The solution to helping with moving a box.
		LinkedList<Node> helpSolution = new LinkedList<>();

		// Search for path to box
		Heuristic moveToBoxHeuristic		= new Proximity(sa, box);
		GoalState moveToBoxGS 				= new ProximityGoalState(sa.id, box.row, box.col);
		Strategy moveToBoxStrategy 			= new StrategyBestFirst(moveToBoxHeuristic);
		sa.setState(node);
		SearchResult moveToBoxResult 		= sa.CustomSearch(moveToBoxStrategy, moveToBoxGS);

		if( moveToBoxResult.reason == Result.STUCK ){
			// We had no path to the box
			System.err.println("Conflict :: ResolveBoxConflict :: No path from Agent " + sa.id + " to the box.");
			// Relax the subdomain, removing non-moveable boxes and other agents
			Node moveToBoxRelaxed = node.subdomain(sa.id);
			sa.setState(moveToBoxRelaxed);
			// Perform search on relaxed domain
			SearchResult moveToBoxResult_Relaxed = sa.CustomSearch(moveToBoxStrategy, moveToBoxGS);
			if( moveToBoxResult_Relaxed.reason == Result.STUCK ){
				// No path available even in relaxed domain -- agent in seperate section.
				System.err.println("Conflict :: ResolveBoxConflict :: You fucked.");
				sa.status = SearchAgent.Status.IDLE;
				return;
			}

			// Route in relaxed subdomain found. Setting itself as stuck.
			solutions.get(sa.id).clear();
			solutions.get(sa.id).addAll(moveToBoxResult_Relaxed.solution);
			sa.status = SearchAgent.Status.STUCK_HELPING;

			System.err.println("Conflict :: ResolveBoxConflict :: Added  " + sa.id + " to needs_help.");
			// Remove entry from Helping
			Conflict.helping.remove(sa);
			// Enter the original SA and the helping SA into needs_help
			needs_help.addFirst(needingHelp);
			examineRoute(needingHelp, node, RouteParser.parse(solutions, needingHelp.id), needs_agents_moved, needs_boxes_moved);
			needs_help.addFirst(sa);
			examineRoute(sa, node, RouteParser.parse(solutions, sa.id), needs_agents_moved, needs_boxes_moved);

			return;
		}

		System.err.println("Conflict :: ResolveBoxConflict :: Successfully made it to the box.");

		Node moveStart = null;
		if( !moveToBoxResult.solution.isEmpty() ){
			moveStart = moveToBoxResult.solution.get(moveToBoxResult.solution.size()-1);
			// Apply the route TO the box, to the helping solution.
			helpSolution.addAll(moveToBoxResult.solution);
		}else{
			moveStart = node;
		}
		ArrayList<Base> routeToClear = RouteParser.parse(solutions, needingHelp.id);
		System.err.println("Route:");
		System.err.println(routeToClear);

		//Heuristic clearHeuristic = new ClearHeuristic(sa, obstructions, routeToClear);
		Heuristic clearHeuristic = new ClearRouteHeuristic(sa, box.id, routeToClear);
		Strategy clearStrategy = new StrategyBestFirst(clearHeuristic);
		sa.setState(moveStart);
		//SearchResult moveBoxResult = sa.ClearRouteSearch(clearStrategy, obstructions, routeToClear);
		SearchResult moveBoxResult = sa.CustomSearch(clearStrategy, new RouteClearGoalState(sa.id, box.id, routeToClear));

		// Path with box, was found.
		if( moveBoxResult.reason != Result.STUCK ){
			// Apply the route of moving the box AWAY to the helping solution
			helpSolution.addAll(moveBoxResult.solution);

			// Since we found a solution to helping, we clear the current plan and apply the new one.
			solutions.get(sa.id).clear();
			solutions.get(sa.id).addAll(helpSolution);

			// Remove stuck flag from invoking agent. Makes it move instantly. 
			needingHelp.status = SearchAgent.Status.PLAN;

			// Inject dirty NoOpts into helping agent.
			injectNoOp(node, solutions.get(sa.id),  Math.abs(routeToClear.size()-moveBoxResult.solution.size()) + 2 , -1);

		}else{
			System.err.println("Conflict :: ResolveBoxConflict :: Had to relax domain, to move box.");

			Node clearRelaxed = moveStart.subdomain(sa.id);
			sa.setState(clearRelaxed);
			SearchResult clearRelaxedResult = sa.ClearRouteSearch(clearStrategy, obstructions, routeToClear);

			if( clearRelaxedResult.reason == Result.STUCK ){
				// No way to move the box, in the relaxed subdomain.
				System.err.println("Conflict :: ResolveBoxConflict :: Stuck on moving box.");
				return;
			}else{
				// Apply the relaxed route to the solution
				solutions.get(sa.id).addAll(clearRelaxedResult.solution);

				sa.status = SearchAgent.Status.STUCK_HELPING;
				// Add SA and helping SA to needs_help again
				needs_help.addFirst(needingHelp);
				examineRoute(needingHelp, node, RouteParser.parse(solutions, needingHelp.id), needs_agents_moved, needs_boxes_moved);
				needs_help.addFirst(sa);
				examineRoute(sa, node, RouteParser.parse(solutions, sa.id), needs_agents_moved, needs_boxes_moved);
			}

		}
	}

	private static void examineRoute( SearchAgent sa, Node node, ArrayList<Base> route, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved ){
		// Lists to keep track of obstructions in route
		ArrayList<LogicalAgent> agentsInTheWay = new ArrayList<>();
		ArrayList<Box> boxesInTheWay = new ArrayList<>();
		int numBoxes = 0;

		// Loop over route, identifying obstructions in the route
		for( Base b : route ){
			Object o = node.objectAt(b);
			if( o instanceof LogicalAgent && ((LogicalAgent)o).id != sa.id ){
				System.err.println("Conflict :: ExamineRoute :: Agent found in route for agent " + sa.id + "!");
				// I know, I know. Ugly syntax. Get ID of LogicalAgent in the way
				// and insert corrosponding SearchAgent into list.
				agentsInTheWay.add( (LogicalAgent)o );	

			}else if( o instanceof Box ){
				System.err.println("Conflict :: ExamineRoute :: Box found in route for agent " + sa.id + "!");
				
				if( sa.color != ((Box)o).color || numBoxes > 0 ){
					System.err.println("            Color of box: " + ((Box)o).color + ".");
					boxesInTheWay.add( (Box)o );
				}else if( sa.color == ((Box)o).color && numBoxes == 0 ){
					numBoxes++;
				}
			}
		}
		needs_agents_moved.put(sa, agentsInTheWay);
		needs_boxes_moved.put(sa, boxesInTheWay);
	}

	private static void resolveAgentConflict(ArrayList< LinkedList< Node > > solutions, Node node, SearchAgent needingHelp, SearchAgent saInTheWay, ArrayList<Base> route, Deque<SearchAgent> needs_help, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved ) throws IOException{
		System.err.println("Conflict :: ResolveAgentConflict :: Initated.");
		int inject_help_at = 0;

		// Metrics for current plan
		int estimate = node.distance(node.agents[needingHelp.id], node.agents[saInTheWay.id]);

		/*if( solutions.get(saInTheWay.id).size() < 5 && solutions.get(saInTheWay.id).size() > 0 ){
			System.err.println("Conflict :: ResolveAgentConflict :: Skipping agent, because its plan is short.");
			return;
		}*/

		int row, col;
		row = node.agents[saInTheWay.id].row;
		col = node.agents[saInTheWay.id].col;

		Heuristic outOfTheWayHeuristic 		= new OutOfTheWayHeuristic(saInTheWay, route, row, col);
		GoalState outOfTheWayGS 			= new RouteClearOfAgentGoalState(saInTheWay.id, route);
		Strategy outOfTheWayStrategy 		= new StrategyBestFirst(outOfTheWayHeuristic);
		saInTheWay.setState(node);
		SearchResult outOfTheWayResult 		= saInTheWay.CustomSearch(outOfTheWayStrategy, outOfTheWayGS);

		if( outOfTheWayResult.reason == Result.STUCK ){
			System.err.println("Conflict :: ResolveAgentConflict :: Was unable to find a route out of harms way.");
			saInTheWay.status = SearchAgent.Status.STUCK_HELPING;

			// Relax the subdomain in hopes of finding a route.
			Node relaxed = node.subdomain(saInTheWay.id);
			saInTheWay.setState(relaxed);
			SearchResult relaxedResult = saInTheWay.CustomSearch(outOfTheWayStrategy, outOfTheWayGS);

			if( relaxedResult.reason == Result.STUCK ){
				System.err.println("Conflict :: ResolveAgentConflict :: Unable to find a route using a RELAXED domain. Truely, and awe-fucking-fully, stuck.");
				saInTheWay.status = SearchAgent.Status.STUCK;

				needs_help.addFirst(needingHelp);
				needs_help.addFirst(saInTheWay);

				return;
			}

			System.err.println("Conflict :: ResolveAgentConflict :: Agent " + saInTheWay.id + " found a route, using the relaxed domain.");
			solutions.get(saInTheWay.id).clear();
			solutions.get(saInTheWay.id).addAll( outOfTheWayResult.solution );
			examineRoute(saInTheWay, node, RouteParser.parse(solutions, saInTheWay.id), needs_agents_moved, needs_boxes_moved);
			
			return;
		}else{
			solutions.get(saInTheWay.id).clear();
			solutions.get(saInTheWay.id).addAll( outOfTheWayResult.solution );
			injectNoOp(node, solutions.get(saInTheWay.id), outOfTheWayResult.solution.size() + 1,  -1);
			needingHelp.status 	= SearchAgent.Status.PLAN;
			saInTheWay.status 	= SearchAgent.Status.PLAN;
		}
	}

	



	private static void injectNoOp(Node node, LinkedList<Node> target, int count, int at){
		// target.size()-1
		if( at == -1 ) at = target.size()-1;
		// Inject dirty NoOpts into helping agent.
		Node noOptParent = null;
		if( !target.isEmpty() ){
			noOptParent = target.get(at) ;
		}else{
			noOptParent = node;
		}

		for( int i = 0 ; i < count ; i++ ){
			Node noOpt = noOptParent.ChildNode();
			noOpt.action = new Command();
			target.addLast(noOpt);
			noOptParent = noOpt;
		}
	}













	/*
	public static ArrayList< LinkedList< Node > > _solve(Node node, ArrayList< LinkedList< Node > > solutions, List< SearchAgent > agents) throws IOException{
		
		for (SearchAgent agent : agents) {
			if(agent.status == SearchAgent.Status.STUCK){
				
				//  find reason
				 

				ArrayList<Base> route = RouteParser.parse(solutions, agent.id);

				ArrayList<LogicalAgent> agentsInTheWay = new ArrayList<>();
				ArrayList<Box> boxesInTheWay = new ArrayList<>();

				int dirty_test_count = 0;
				boolean dirty_test_flag = true;

				// Parse the route, storing what might be in the way
				// Sanity check on route!
				if( route.size() < 1 ){
					System.err.println("Conflict :: Route is empty.");
				}

				for( Base b : route ){
					Object o = node.objectAt(b);
					if(dirty_test_flag)dirty_test_count++;
					if( o instanceof LogicalAgent ){
						dirty_test_flag = false;
						System.err.println("Conflict :: Agent found in route for agent " + agent.id + "!");
						agentsInTheWay.add( (LogicalAgent)o );
					}else if( o instanceof Box ){
						dirty_test_flag = false;
						System.err.println("Conflict :: Box found in route for agent " + agent.id + "!");
						System.err.println("            Color of box: " + ((Box)o).color + ".");
						if( agent.color != ((Box)o).color )
							boxesInTheWay.add( (Box)o );
					}
				}

				
				 // find some one to solve the problem
				 
				
				// Call an agent that can move the box, and MOVE the fucking box.
				for( Box b : boxesInTheWay ){
					SearchAgent helpingAgent = agents.get( Node.colorMap.get(b.color).get(0) );

					System.err.println("Conflict:: Found agent to help. Asking Agent " + helpingAgent.id);

					Heuristic proximityHeuristic 	= new Proximity(agent, b);
					GoalState proxGoal 				= new ProximityGoalState(agent.id, b.row, b.col);
					Strategy helpingStrategy 		= new StrategyBestFirst(proximityHeuristic);
					helpingAgent.setState(node);
					SearchResult result = helpingAgent.CustomSearch(helpingStrategy, proxGoal);








					// Clear the Helping Agents plan
					if( result.reason != Result.STUCK ){
						solutions.get(helpingAgent.id).clear();
						solutions.get(helpingAgent.id).addAll(result.solution);
						helpingAgent.status = Status.HELPING;
						helping.put(helpingAgent, agent);


						// Figure out what to do
						Node moveStart = null;
						if( !result.solution.isEmpty() ){
							moveStart = result.solution.get(result.solution.size()-1) ;
						}else{
							moveStart = node;
						}

						Heuristic clearHeuristic = new ClearHeuristic(agent, agentsInTheWay.size()+boxesInTheWay.size(), route);
						Strategy clearStrategy = new StrategyBestFirst(clearHeuristic);
						helpingAgent.setState(moveStart);
						SearchResult result2 = helpingAgent.ClearRouteSearch(clearStrategy, agentsInTheWay.size()+boxesInTheWay.size(), route);

						if( result2.reason != Result.STUCK ){
							solutions.get(helpingAgent.id).clear();
							solutions.get(helpingAgent.id).addAll(result2.solution);

							// Remove stuck flag from invoking agent. Makes it move instantly. 
							agent.status = SearchAgent.Status.PLAN;

							// Inject dirty NoOpts into helping agent.
							Node noOptParent = null;
							if( !result2.solution.isEmpty() ){
								noOptParent = result2.solution.get(result2.solution.size()-1) ;
							}else{
								noOptParent = node;
							}

							for( int i = 0 ; i < dirty_test_count ; i++ ){
								Node noOpt = noOptParent.ChildNode();
								noOpt.action = new Command();
								solutions.get(helpingAgent.id).addLast(noOpt);
								noOptParent = noOpt;
							}

						}else{
							// Impossible to help?!
						}

						System.err.println("Conflict :: Agent " + helpingAgent.id + " moving to help.");
					}else{
						System.err.println("Conflict :: No path to help was found.");
						System.err.println(node);
					}
				}

				// Assumption: The agent can move out of the way.
				// Make any agents in route move out of the way.
				for( LogicalAgent a : agentsInTheWay ){

					if( agents.get(a.id).status == Status.HELPING )
						continue;

					System.err.println("Agent " + a.id + " was found to be in the way. Attempting to move.");

					SearchAgent sa = agents.get(a.id);

					sa.status = SearchAgent.Status.HELPING;
					ArrayList< Node > moves = node.getExpandedNodes(sa.id);
					if( moves.size() == 0 ){
						// If there's no possible moves available, report stuck and skip
						agents.get(a.id).status = Status.STUCK;
						continue;
					}

					Node getOut		= moves.get(0);

					Node noOpt		= getOut.ChildNode();
					noOpt.action 	= new Command(); // NoOP command
					Node getBack	= noOpt.ChildNode();
					getBack.action 	= getOut.action.reverseCommand( getOut.action );
					getBack.excecuteCommand(getBack.action, sa.id);

					if( !solutions.get(sa.id).isEmpty() ) {
						solutions.get(sa.id).peek().parent = getBack;
					}

					solutions.get(sa.id).addFirst(getBack);
					solutions.get(sa.id).addFirst(noOpt);
					solutions.get(sa.id).addFirst(getOut);
				}


			}
		}
		return solutions;
	}
	*/

}