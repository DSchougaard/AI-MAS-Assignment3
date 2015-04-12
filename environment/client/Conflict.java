package client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.SearchAgent.Status;
import client.node.Node;

import client.node.storage.Box;
import client.node.storage.Base;
import client.SearchAgent;

import client.Command;
import client.Command.type;
import client.Command.dir;

import client.parser.RouteParser;

public class Conflict{

	public static ArrayList< LinkedList< Node > > solve(Node node, ArrayList< LinkedList< Node > > solutions, List< SearchAgent > agents){
		
		for (SearchAgent agent : agents) {
			if(agent.status == SearchAgent.Status.STUCK){
				System.err.println("Conflict found! Agent " + agent.id + " is stuck.");
				//TODO: find reason

				// input check
				System.err.println("Solution size: " + solutions.get(agent.id).size());

				ArrayList<Base> route = RouteParser.parse(solutions, agent.id);

				ArrayList<SearchAgent> agentsInTheWay = new ArrayList<>();
				ArrayList<Box> boxesInTheWay = new ArrayList<>();

				// Parse the route, storing what might be in the way
				// Sanity check on route!
				if( route.size() < 1 ){
					System.err.println("Fang den brevdue!");
				}

				System.err.println("Route: " + route.toString());

				for( Base b : route ){
					Object o = node.WTF(b);
					if( o instanceof SearchAgent ){
						System.err.println("Conflict handling:: Agent found in route!");
						agentsInTheWay.add( (SearchAgent)o );
					}else if( o instanceof Box ){
						boxesInTheWay.add( (Box)o );
					}
				}

				// Assumption: The agent can move out of the way.
				for( SearchAgent a : agentsInTheWay ){

					System.err.println("Agent " + a.id + " was found to be in the way. Attempting to move.");

					a.status = SearchAgent.Status.HELPING;
					Node getOut		= node.getExpandedNodes(a.id).get(0);
					Node noOpt		= getOut.ChildNode();
					noOpt.action 	= new Command(); // NoOP command
					Node getBack	= noOpt.ChildNode();
					getBack.action 	= getOut.action.reverseCommand( getOut.action );
					getBack.excecuteCommand(getBack.action, a.id);

					solutions.get(a.id).peek().parent = getBack;
					solutions.get(a.id).addFirst(getBack);
					solutions.get(a.id).addFirst(noOpt);
					solutions.get(a.id).addFirst(getOut);
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