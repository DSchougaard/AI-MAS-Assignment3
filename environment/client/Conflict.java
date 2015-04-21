package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.Deque;


import client.Heuristic.Proximity;
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
// Searches

import client.heuristic.MoveToHeuristic;
import client.heuristic.OutOfTheWayHeuristic;

public class Conflict{

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
						if( sa.color != ((Box)o).color || numBoxes < 1 ){
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

			for( LogicalAgent la : needs_agents_moved.get(sa) ){
				System.err.println("Conflict :: Moving agent " + la.id + " out of agent " + sa.id + "'s route.");
				resolveAgentConflict(solutions, node, sa, agents.get(la.id), RouteParser.parse(solutions.get(sa.id), sa.id), needs_help, needs_agents_moved, needs_boxes_moved);
			}

		}

		return solutions;
	}

	private static void resolveBoxConflict(Node node, SearchAgent sa, Box box){

	}

	private static void examineRoute( SearchAgent sa, Node node, ArrayList<Base> route, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved ){
		// Lists to keep track of obstructions in route
		ArrayList<LogicalAgent> agentsInTheWay = new ArrayList<>();
		ArrayList<Box> boxesInTheWay = new ArrayList<>();
		int numBoxes = 0;

		// Loop over route, identifying obstructions in the route
		for( Base b : route ){
			Object o = node.objectAt(b);
			if( o instanceof LogicalAgent ){
				System.err.println("Conflict :: ExamineRoute :: Agent found in route for agent " + sa.id + "!");
				// I know, I know. Ugly syntax. Get ID of LogicalAgent in the way
				// and insert corrosponding SearchAgent into list.
				agentsInTheWay.add( (LogicalAgent)o );	

			}else if( o instanceof Box ){
				System.err.println("Conflict :: ExamineRoute :: Box found in route for agent " + sa.id + "!");
				if( sa.color != ((Box)o).color || numBoxes < 1 ){
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

	private static void resolveAgentConflict(ArrayList< LinkedList< Node > > solutions, Node node, SearchAgent sa, SearchAgent saInTheWay, ArrayList<Base> route, Deque<SearchAgent> needs_help, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved ) throws IOException{
		System.err.println("Conflict :: ResolveAgentConflict :: Initated.");
		int inject_help_at = 0;

		// Metrics for current plan
		int estimate = node.distance(node.agents[sa.id], node.agents[saInTheWay.id]);

		if( solutions.get(saInTheWay.id).size() < 5 && solutions.get(saInTheWay.id).size() > 0 ){
			System.err.println("Conflict :: ResolveAgentConflict :: Skipping agent, because its plan is short.");
			return;
		}
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

			Node relaxed = node.subdomain(saInTheWay.id);
			saInTheWay.setState(relaxed);
			SearchResult relaxedResult = saInTheWay.CustomSearch(outOfTheWayStrategy, outOfTheWayGS);

			if( relaxedResult.reason != Result.STUCK ){
				ArrayList<Base> saInTheWayRoute = RouteParser.parse(outOfTheWayResult.solution, saInTheWay.id);
				examineRoute(saInTheWay, node, saInTheWayRoute, needs_agents_moved, needs_boxes_moved);
			}else{
				System.err.println("Conflict :: ResolveAgentConflict :: Eeehhhhh....");
				// Eeehhh.......
			}
			needs_help.addFirst(saInTheWay);

			return;
		}else{
			solutions.get(saInTheWay.id).clear();
			solutions.get(saInTheWay.id).addAll( outOfTheWayResult.solution );
			injectNoOp(node, solutions.get(saInTheWay.id), outOfTheWayResult.solution.size() + 1,  -1);
			sa.status = SearchAgent.Status.PLAN;
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