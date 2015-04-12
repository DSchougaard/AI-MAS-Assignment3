package client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;


import java.io.IOException;

import client.SearchAgent.Status;
import client.node.Node;

import client.node.storage.Box;
import client.node.storage.Base;
import client.node.storage.LogicalAgent;
import client.node.storage.SearchResult;
import client.node.storage.SearchResult.Result;
import client.SearchAgent;

import client.node.Color;

import client.Command;
import client.Command.type;
import client.Command.dir;

import client.parser.RouteParser;

// Searches
import client.Heuristic;
import client.Heuristic.Proximity;
import client.SearchClient;
import client.Strategy;
import client.Strategy.StrategyBestFirst;

public class Conflict{

	// Helping Agent -> Agent Receiving Help
	private static HashMap<SearchAgent, SearchAgent> helping = new HashMap<>();

	public static void doneHelping(SearchAgent helpingAgent){
		if(	helping.get(helpingAgent) != null ){
			helping.get(helpingAgent).status = Status.PLAN;
		}
	}


	public static ArrayList< LinkedList< Node > > solve(Node node, ArrayList< LinkedList< Node > > solutions, List< SearchAgent > agents) throws IOException{
		
		for (SearchAgent agent : agents) {
			if(agent.status == SearchAgent.Status.STUCK){
				//TODO: find reason

				ArrayList<Base> route = RouteParser.parse(solutions, agent.id);

				ArrayList<LogicalAgent> agentsInTheWay = new ArrayList<>();
				ArrayList<Box> boxesInTheWay = new ArrayList<>();

				// Parse the route, storing what might be in the way
				// Sanity check on route!
				if( route.size() < 1 ){
					System.err.println("Conflict :: Route is empty.");
				}

				for( Base b : route ){
					Object o = node.WTF(b);
					if( o instanceof LogicalAgent ){
						System.err.println("Conflict :: Agent found in route for agent " + agent.id + "!");
						agentsInTheWay.add( (LogicalAgent)o );
					}else if( o instanceof Box ){
						System.err.println("Conflict :: Box found in route for agent " + agent.id + "!");
						System.err.println("            Color of box: " + ((Box)o).color + ".");
						if( agent.color != ((Box)o).color )
							boxesInTheWay.add( (Box)o );
					}
				}

				// Call an agent that can move the box, and MOVE the fucking box.
				for( Box b : boxesInTheWay ){
					SearchAgent a = agents.get( node.colorMap.get(b.color).get(0) );

					System.err.println("Conflict:: Found agent to help. Asking Agent " + a.id);

					Heuristic proximityHeuristic 	= new Proximity(agent, b);
					Strategy helpingStrategy 		= new StrategyBestFirst(proximityHeuristic);
					SearchClient helpingClient 		= new SearchClient(node);
					SearchResult result = helpingClient.ProximitySearch(helpingStrategy, a.id, b);

					// Clear the Helping Agents plan
					if( result.reason != Result.STUCK ){
						solutions.get(a.id).clear();
						solutions.get(a.id).addAll(result.solution);
						a.status = Status.HELPING;
						helping.put(a, agent);

						Node chaoticMove = null;
						if( !result.solution.isEmpty() ){
							chaoticMove = result.solution.get(result.solution.size()-1).getExpandedNodes(a.id).get(0);
						}else{
							chaoticMove = node.getExpandedNodes(a.id).get(0);
						}
						Node noOpt		= chaoticMove.ChildNode();
						noOpt.action 	= new Command(); // NoOP command

						solutions.get(a.id).addLast(chaoticMove);
						solutions.get(a.id).addLast(noOpt);

						System.err.println("Conflict :: Agent " + a.id + " moving to help.");
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
					Node getOut		= node.getExpandedNodes(sa.id).get(0);
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


				
				//TODO: find some one to solve the problem
				for (SearchAgent OtherAgent : agents) {
					if(!agent.equals(OtherAgent) && OtherAgent.status!=Status.HELPING){
						//TODO: find solution
					}
				}
			}
		}
		return solutions;
	}	
}