package client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.node.Node;
import client.node.storage.Agent;
import client.node.storage.Agent.Status;



public class Conflict{
	
	
	
	
	public static ArrayList< LinkedList< Node > > solve(ArrayList< LinkedList< Node > > solutions, List< Agent > agents){
		
		for (Agent agent : agents) {
			if(agent.status== Agent.Status.STUCK){
				//TODO: find reason
				
				//TODO: find some one to solve the problem
				for (Agent OtherAgent : agents) {
					if(!agent.equals(OtherAgent) && OtherAgent.status!=Status.HELPING){
						//TODO: find solution
					}
				}
			}
		}

//		if(conflicts==1){
//			return cut(solutions);
//		}
		
		System.err.println("conflict error");
		return solutions;
	}
	
	private static ArrayList< LinkedList< Node > > cut(ArrayList< LinkedList< Node > >  solutions){
		for (int i = 0; i < solutions.size(); i++) {
			Node  tmp;
			tmp= solutions.get(i).get(0);
			
			solutions.get(i).clear();
			solutions.get(i).add(tmp);
		}
		
		return solutions;
	}
}