package client.parser;

import java.util.ArrayList;
import java.util.LinkedList;

import client.Command.type;
import client.node.Node;
import client.node.storage.Base;

public class RouteParser {
	public static ArrayList<Base> parse(ArrayList<LinkedList<Node>> solution, int agentId) {
		ArrayList<Base> rute = new ArrayList<Base>();
		for (int i = 0; i < solution.get(agentId).size(); i++) {
			Node curentNode = solution.get(agentId).get(i);
			Base agentPosition = new Base(curentNode.getAgents()[agentId].row, curentNode.getAgents()[agentId].col);
			if (!rute.contains(agentPosition)){
				rute.add(agentPosition);
			}
			
			// Only the PUSH action can move the box to a non agent visited point.
			if (curentNode.action.actType.equals(type.Push)) {	
				Base boxPosition = null;
				//System.err.println("Push Box: " + curentNode.action.dir2);
				switch (curentNode.action.dir2){
				case N:
					boxPosition = new Base((curentNode.getAgents()[agentId].row - 1), curentNode.getAgents()[agentId].col);
					break;
				case S:
					boxPosition = new Base((curentNode.getAgents()[agentId].row + 1), curentNode.getAgents()[agentId].col);
					break;
				case E:
					boxPosition = new Base(curentNode.getAgents()[agentId].row, (curentNode.getAgents()[agentId].col + 1));
					break;
				case W:
					boxPosition = new Base(curentNode.getAgents()[agentId].row, (curentNode.getAgents()[agentId].col - 1));
					break;
				default:
					break;
				}
				
				if (!rute.contains(boxPosition)){
					rute.add(boxPosition);
				}
			}
		}
		return rute;
	}
}