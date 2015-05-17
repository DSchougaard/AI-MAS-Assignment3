package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import client.SearchAgent.Status;
import client.heuristic.AStar;
import client.heuristic.ClearRouteHeuristic;
import client.heuristic.Greedy;
import client.heuristic.Heuristic;
import client.heuristic.OutOfTheWayHeuristic;
import client.heuristic.Proximity;
import client.node.GoalState;
import client.node.GoalState.ProximityGoalState;
import client.node.GoalState.RouteClearGoalState;
import client.node.GoalState.RouteClearOfAgentGoalState;
import client.node.Node;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.LogicalAgent;
import client.node.storage.SearchResult;
import client.node.storage.SearchResult.Result;
import client.parser.RouteParser;

import client.Settings;

public class Conflict{

	public static ArrayList< LinkedList< Node > > solve(Node node, ArrayList< LinkedList< Node > > solutions, List< SearchAgent > agents) throws Exception{
		if( Settings.Global.PRINT ){
		System.err.println("\n");
		System.err.println("Conflict :: Invoking Conflict Resolution.");
		System.err.println("            May the force be with you, young padawan.");
		System.err.println("\n");
		}

		Deque<SearchAgent> needs_help = new LinkedList<>();
		HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved = new HashMap<SearchAgent, ArrayList<LogicalAgent>>();
		HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved = new HashMap<SearchAgent, ArrayList<Box>>();
		ArrayList<SearchAgent> Sagents= new ArrayList<>(agents);
		//		Collections.shuffle(Sagents);
		for( SearchAgent agent : Sagents ){
			if( agent.status == SearchAgent.Status.STUCK || agent.status == SearchAgent.Status.STUCK_HELPING ){
				//examineRoute( SearchAgent sa, Node node, ArrayList<Base> route, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved ){
				needs_help.addFirst(agent);
				examineRoute(agent, node, RouteParser.parse(solutions, agent.id), needs_agents_moved, needs_boxes_moved);

				
			}
		}		


		while( !needs_help.isEmpty() ){
			if( Settings.Global.PRINT ){
				System.err.println("Conflict :: Size of help queue: " + needs_help.size() + "\n");
			}

			SearchAgent needingHelp = needs_help.pollFirst();

			if( Settings.Global.PRINT ){
			System.err.println("Conflict :: Agent " + needingHelp.id + " receiving help.");
			}

			ArrayList<Box> move_boxes = needs_boxes_moved.get(needingHelp);
			needs_boxes_moved.remove(needingHelp);
			if( move_boxes == null )
				move_boxes = new ArrayList<Box>();

			ArrayList<LogicalAgent> move_agents = needs_agents_moved.get(needingHelp);
			needs_agents_moved.remove(needingHelp);
			if( move_agents == null ){
				move_agents = new ArrayList<LogicalAgent>();
			}

			if( move_boxes.isEmpty() && Settings.Global.PRINT)
				System.err.println("Conflict :: No boxes on route.");

			for( Box box : move_boxes ){
				if( box == null )
					System.err.println("Conflict :: Box was NULL.");
				
				if( Settings.Global.PRINT ){
					System.err.println("Conflict :: Moving box @ [" + box.row + ", " + box.col + "].");			
				}
				
				resolveBoxConflict(needingHelp, box, node, agents, solutions, needs_help, needs_agents_moved, needs_boxes_moved);
			}

			for( LogicalAgent la : move_agents ){
				if( agents.get(la.id).status == Status.HELPING ){

					if( Settings.Global.PRINT ){
						System.err.println("Agent " + la.id + " is status HELPING.");
					}
					continue;
				}

				if( solutions.get(la.id).size() < Settings.Conflict.REMAINING_AGENT_PLAN_THRESHOLD && solutions.get(la.id).size() > 0)
					continue;

				if( Settings.Global.PRINT ){
					System.err.println("Conflict :: Moving agent " + la.id + " out of agent " + needingHelp.id + "'s route.");
				}
				resolveAgentConflict(solutions, node, needingHelp, agents.get(la.id), RouteParser.parse(solutions.get(needingHelp.id), needingHelp.id), needs_help, needs_agents_moved, needs_boxes_moved);
			}

			needs_boxes_moved.remove(needingHelp);
		}
		return solutions;
	}


	private static void resolveBoxConflict(SearchAgent needingHelp, Box box, Node node, List<SearchAgent> agents,ArrayList< LinkedList< Node > > solutions, Deque<SearchAgent> needs_help, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved) throws IOException{
		if( Settings.Global.PRINT ){
			System.err.println("Conflict :: ResolveBoxConflict :: Attempting to resolve.");
		}

		SearchAgent helperAgent = findHelperAgent(needingHelp, box, node, agents);
		helperAgent.status = Status.HELPING;
		needs_boxes_moved.remove(helperAgent);

		if( Settings.Global.PRINT ){
			System.err.println("Conflict :: ResolveBoxConflict :: Agent " + helperAgent.id + " is helping");
		}

		// The solution to helping with moving a box.
		LinkedList<Node> helpSolution = new LinkedList<>();

		SearchResult moveToBoxResult = moveToBox(needingHelp, helperAgent, box, node, solutions, needs_help, needs_agents_moved, needs_boxes_moved);
		if(moveToBoxResult==null){
			return;
		}

		if( Settings.Global.PRINT ){
			System.err.println("Conflict :: ResolveBoxConflict :: Successfully made it to the box.");
		}

		Node moveStart = null;
		if( !moveToBoxResult.solution.isEmpty() ){
			moveStart = moveToBoxResult.solution.get(moveToBoxResult.solution.size()-1);
			// Apply the route TO the box, to the helping solution.
			helpSolution.addAll(moveToBoxResult.solution);
		}else{
			moveStart = node;
		}

		ArrayList<Base> routeToClear = RouteParser.parse(solutions, needingHelp.id);
		
		if( Settings.Global.PRINT ){
			System.err.println("Conflict :: ResolveBoxConflict :: Route:"+routeToClear);
		}
		clearRoute(needingHelp, helperAgent, box, node, moveStart, routeToClear, solutions, helpSolution, needs_help, needs_agents_moved, needs_boxes_moved);	
	}

	private static SearchAgent findHelperAgent(SearchAgent needingHelp, Box box, Node node, List<SearchAgent> agents){
		// PLUG AND PLAY AGENT SELECT

		// First we select the agent best suited for the job
		ArrayList<Integer> possibleAgentIDs = new ArrayList<>();
		for (int id : node.getAgentIDs(box.color)) {
			if(agents.get(id).status!=SearchAgent.Status.HELPING && agents.get(id).status!=SearchAgent.Status.STUCK_HELPING && node.distance(box, node.agents[id]) != null){
				possibleAgentIDs.add(id);
			}
		}

		if (possibleAgentIDs.isEmpty()) {
			for (int id : node.getAgentIDs(box.color)) {
				if(node.distance(box, node.agents[id]) != null){
					possibleAgentIDs.add(id);
				}
			}
		}

		int distanceClosest = Integer.MAX_VALUE, selectedAgent = -1;
		for( int id : possibleAgentIDs ){
			if(  node.distance(box, node.agents[id]) < distanceClosest ){
				selectedAgent = id;
				distanceClosest = node.distance(box, node.agents[id]);
			}
		}
		if( Settings.Global.PRINT ){
			System.err.println("Conflict :: ResolveBoxConflict :: FindHelperAgent :: Selected Agent " + selectedAgent);
		}
		return agents.get(selectedAgent);
	}

	private static SearchResult moveToBox(SearchAgent needingHelp, SearchAgent helperAgent, Box box, Node node, ArrayList< LinkedList< Node > > solutions, Deque<SearchAgent> needs_help, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved) throws IOException{
		// Relaxed search
		Heuristic moveToBoxHeuristicRelaxed		= new Proximity(helperAgent, box);
		GoalState moveToBoxGSRelaxed	 		= new ProximityGoalState(helperAgent.id, box.row, box.col);
		Strategy moveToBoxStrategyRelaxed	 	= new AStar(moveToBoxHeuristicRelaxed);
		helperAgent.setState(node.subdomain(helperAgent.id));
		SearchResult moveToBoxResultRelaxed	 	= helperAgent.Search(moveToBoxStrategyRelaxed , moveToBoxGSRelaxed);

		// Normal search
		Heuristic moveToBoxHeuristic			= new Proximity(helperAgent, box);
		GoalState moveToBoxGS 					= new ProximityGoalState(helperAgent.id, box.row, box.col);
		Strategy moveToBoxStrategy 				= new AStar(moveToBoxHeuristic);
		helperAgent.setState(node);
		SearchResult moveToBoxResult 			= helperAgent.Search(moveToBoxStrategy, moveToBoxGS, moveToBoxResultRelaxed);

		if( moveToBoxResult.reason == Result.STUCK ){
			// We had no path to the box
			if( Settings.Global.PRINT ){
				System.err.println("Conflict :: ResolveBoxConflict :: No path from Agent " + helperAgent.id + " to the box.");
			}

			if( moveToBoxResultRelaxed.reason == Result.STUCK ){
				// No path available even in relaxed domain -- agent in seperate section.
				if( Settings.Global.PRINT ){
					System.err.println("Conflict :: ResolveBoxConflict :: You fucked.");
				}
				helperAgent.status = SearchAgent.Status.IDLE;
				return null;
			}

			// Route in relaxed subdomain found. Setting itself as stuck.
			solutions.get(helperAgent.id).clear();
			solutions.get(helperAgent.id).addAll(moveToBoxResultRelaxed.solution);
			helperAgent.status = SearchAgent.Status.STUCK_HELPING;

			if( Settings.Global.PRINT ){
				System.err.println("Conflict :: ResolveBoxConflict :: Added  " + helperAgent.id + " to needs_help.");
			}

			// Enter the original SA and the helping SA into needs_help
			needs_help.addFirst(needingHelp);
			examineRoute(needingHelp, node, RouteParser.parse(solutions, needingHelp.id), needs_agents_moved, needs_boxes_moved);
			needs_help.addFirst(helperAgent);
			examineRoute(helperAgent, node, RouteParser.parse(solutions, helperAgent.id), needs_agents_moved, needs_boxes_moved);

			return null;
		}

		return moveToBoxResult;
	}

	private static void clearRoute(SearchAgent needingHelp, SearchAgent helperAgent, Box box, Node node, Node moveStart, ArrayList<Base> routeToClear, ArrayList< LinkedList< Node > > solutions, LinkedList<Node> helpSolution, Deque<SearchAgent> needs_help, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved) throws IOException{
		//relaxed 
		//Heuristic clearHeuristicRelaxed = new ClearHeuristic(helperAgent, routeToClear);
		Heuristic clearHeuristicRelaxed 	= new ClearRouteHeuristic(helperAgent, box.id, routeToClear);
		Strategy clearStrategyRelaxed 		= new AStar(clearHeuristicRelaxed);
		helperAgent.setState(moveStart.subdomain(helperAgent.id));
		SearchResult moveBoxResultRelaxed 	= helperAgent.Search(clearStrategyRelaxed, new RouteClearGoalState(helperAgent.id, box.id, routeToClear));

		//normal
		//Heuristic clearHeuristic = new ClearHeuristic(helperAgent, routeToClear);
		Heuristic clearHeuristic 			= new ClearRouteHeuristic(helperAgent, box.id, routeToClear);
		Strategy clearStrategy 				= new AStar(clearHeuristic);
		helperAgent.setState(moveStart);
		SearchResult moveBoxResult 			= helperAgent.Search(clearStrategy, new RouteClearGoalState(helperAgent.id, box.id, routeToClear), moveBoxResultRelaxed);
		
		// Path with box, was found.
		if( moveBoxResult.reason == Result.PLAN ){
			if( Settings.Global.PRINT ){
				System.err.println("Conflict :: ResolveBoxConflict :: ClearRoute :: Succesfully moved box.");
			}
			// Apply the route of moving the box AWAY to the helping solution
			helpSolution.addAll(moveBoxResult.solution);

			// Since we found a solution to helping, we clear the current plan and apply the new one.
			solutions.get(helperAgent.id).clear();
			solutions.get(helperAgent.id).addAll(helpSolution);


			// Remove stuck flag from invoking agent. Makes it move instantly. 
			needingHelp.status = SearchAgent.Status.PLAN;

			// Inject dirty NoOpts into helping agent.
			injectNoOp(node, solutions.get(helperAgent.id),  Math.abs(routeToClear.size()-moveBoxResult.solution.size()) + Settings.Conflict.NO_OP_CLEAR_ROUTE , -1);

		}else{
			if( Settings.Global.PRINT ){
				System.err.println("Conflict :: ResolveBoxConflict :: ClearRoute :: Had to relax domain, to move box.");
			}

			if( moveBoxResultRelaxed.reason == Result.STUCK ){
				// No way to move the box, in the relaxed subdomain.
				if( Settings.Global.PRINT ){
					System.err.println("Conflict :: ResolveBoxConflict :: ClearRoute :: Stuck on moving box.");
				}
				return;
			}else{

				// Apply the route of moving the box AWAY to the helping solution
				//helpSolution.addAll(moveBoxResult.solution);
				// Pretty sure you're supposed to insert the relaxed solution here?
				helpSolution.addAll(moveBoxResultRelaxed.solution);


				// Since we found a solution to helping, we clear the current plan and apply the new one.
				solutions.get(helperAgent.id).clear();
				solutions.get(helperAgent.id).addAll(helpSolution);

				helperAgent.status = SearchAgent.Status.STUCK_HELPING;
				// Add SA and helping SA to needs_help again
				needs_help.addFirst(needingHelp);
				examineRoute(needingHelp, node, RouteParser.parse(solutions, needingHelp.id), needs_agents_moved, needs_boxes_moved);
				needs_help.addFirst(helperAgent);
				examineRoute(helperAgent, node, RouteParser.parse(solutions, helperAgent.id), needs_agents_moved, needs_boxes_moved);
			}

		}
	}

	private static void examineRoute( SearchAgent agent, Node node, ArrayList<Base> route, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved ){
		// Lists to keep track of obstructions in route
		ArrayList<LogicalAgent> agentsInTheWay = new ArrayList<>();
		ArrayList<Box> boxesInTheWay = new ArrayList<>();

		// Loop over route, identifying obstructions in the route
		int numBoxes = 0;
		for( Base b : route ){
			Object o = node.objectAt(b);
			if( o instanceof LogicalAgent && ((LogicalAgent)o).id != agent.id ){
				if( Settings.Global.PRINT ){
					System.err.println("Conflict :: ExamineRoute :: Agent " + ((LogicalAgent)o).id + " found in route for Agent " + agent.id + "!");
				}
				// I know, I know. Ugly syntax. Get ID of LogicalAgent in the way
				// and insert corrosponding SearchAgent into list.
				agentsInTheWay.add( (LogicalAgent)o );	

			}else if( o instanceof Box ){


				//if( agent.color != ((Box)o).color ){
				//	System.err.println("Conflict :: ExamineRoute :: Box found in route for agent " + agent.id + "!");
				//	System.err.println("            Color of box: " + ((Box)o).color + ".");
				//	boxesInTheWay.add( (Box)o );
				//}

				// Why is this outcommented?..
				if( agent.color != ((Box)o).color /* || numBoxes > 0*/ ){
					if( Settings.Global.PRINT ){
						System.err.println("            Color of box: " + ((Box)o).color + ".");
					}
					boxesInTheWay.add( (Box)o );
				/*}else if( agent.color == ((Box)o).color && numBoxes == 0 ){
					numBoxes++;*/
				}
			}
		}
		needs_agents_moved.put(agent, agentsInTheWay);
		needs_boxes_moved.put(agent, boxesInTheWay);
	}

	private static void resolveAgentConflict(ArrayList< LinkedList< Node > > solutions, Node node, SearchAgent needingHelp, SearchAgent saInTheWay, ArrayList<Base> route, Deque<SearchAgent> needs_help, HashMap<SearchAgent, ArrayList<LogicalAgent>> needs_agents_moved, HashMap<SearchAgent, ArrayList<Box>> needs_boxes_moved ) throws IOException{
		if( Settings.Global.PRINT ){
			System.err.println("Conflict :: ResolveAgentConflict :: Initated.");
			System.err.println("                                    Only a Sith deals in absolutes!");
		}
		// Take care of the SA in the way
		saInTheWay.status 					= SearchAgent.Status.HELPING;
		needs_help.remove(saInTheWay);
		needs_boxes_moved.remove(saInTheWay);
		needs_agents_moved.remove(saInTheWay);

		int row, col;
		row = node.agents[saInTheWay.id].row;
		col = node.agents[saInTheWay.id].col;

		Heuristic outOfTheWayHeuristic 		= new OutOfTheWayHeuristic(saInTheWay, route, row, col);
		GoalState outOfTheWayGS 			= new RouteClearOfAgentGoalState(saInTheWay.id, route);
		Strategy outOfTheWayStrategy 		= new Greedy(outOfTheWayHeuristic);
		saInTheWay.setState(node);
		SearchResult outOfTheWayResult 		= saInTheWay.Search(outOfTheWayStrategy, outOfTheWayGS);

		if( outOfTheWayResult.reason == Result.STUCK ){
			if( Settings.Global.PRINT ){
				System.err.println("Conflict :: ResolveAgentConflict :: Was unable to find a route out of harms way.");
			}
			saInTheWay.status = SearchAgent.Status.STUCK_HELPING;

			// Relax the subdomain in hopes of finding a route.
			Node relaxed = node.subdomain(saInTheWay.id);
			saInTheWay.setState(relaxed);
			SearchResult relaxedResult = saInTheWay.Search(outOfTheWayStrategy, outOfTheWayGS);

			if( relaxedResult.reason == Result.STUCK ){
				if( Settings.Global.PRINT ){
					System.err.println("Conflict :: ResolveAgentConflict :: Unable to find a route using a RELAXED domain. Truely, and awe-fucking-fully, stuck.");
				}
				saInTheWay.status = SearchAgent.Status.STUCK;

				needs_help.addFirst(needingHelp);
				needs_help.addFirst(saInTheWay);

				return;
			}

			if( Settings.Global.PRINT ){
				System.err.println("Conflict :: ResolveAgentConflict :: Agent " + saInTheWay.id + " found a route, using the relaxed domain.");
			}
			solutions.get(saInTheWay.id).clear();
			solutions.get(saInTheWay.id).addAll( relaxedResult.solution );
			examineRoute(saInTheWay, node, RouteParser.parse(solutions, saInTheWay.id), needs_agents_moved, needs_boxes_moved);

			return;
		}else{
			solutions.get(saInTheWay.id).clear();
			solutions.get(saInTheWay.id).addAll( outOfTheWayResult.solution );
			injectNoOp(node, solutions.get(saInTheWay.id), outOfTheWayResult.solution.size() + Settings.Conflict.NO_OP_RESOLVE_AGENT_CONFLICT,  -1);

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
}