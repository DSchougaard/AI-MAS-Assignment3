package client.parser;

import java.util.ArrayList;
import java.util.LinkedList;

import client.Command.type;
import client.node.Node;
import client.node.storage.Base;

public class RouteParser {
	public static ArrayList<Base> parse(LinkedList<Node> solution, int agentID){
		ArrayList<Base> route = new ArrayList<Base>();
		
		for( Node n : solution ){

			Base agentPosition = new Base(n.getAgents()[agentID].row, n.getAgents()[agentID].col);
			if (!route.contains(agentPosition)){
				route.add(agentPosition);
			}
			
			if (n.action.actType.equals(type.Push)) {	
				Base boxPosition = null;
				switch (n.action.dir2){
				case N:
					boxPosition = new Base((n.getAgents()[agentID].row - 1), n.getAgents()[agentID].col);
					break;
				case S:
					boxPosition = new Base((n.getAgents()[agentID].row + 1), n.getAgents()[agentID].col);
					break;
				case E:
					boxPosition = new Base(n.getAgents()[agentID].row, (n.getAgents()[agentID].col + 1));
					break;
				case W:
					boxPosition = new Base(n.getAgents()[agentID].row, (n.getAgents()[agentID].col - 1));
					break;
				default:
					break;
				}
				
				if (!route.contains(boxPosition)){
					route.add(boxPosition);
				}
			}

		}
		return route;
	}

	public static ArrayList<Base> parse(ArrayList<LinkedList<Node>> solution, int agentId) {
		
		return parse(solution.get(agentId), agentId);
	}
}