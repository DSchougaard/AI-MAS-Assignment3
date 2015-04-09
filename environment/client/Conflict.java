package client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.SearchAgent.Status;
import client.node.Node;



public class Conflict{
	
	
	
	
	public static ArrayList< LinkedList< Node > > solve(ArrayList< LinkedList< Node > > solutions, List< SearchAgent > agents){
		
		for (SearchAgent agent : agents) {
			if(agent.status== SearchAgent.Status.STUCK){
				
				//TODO: find reason
				
				//TODO: find some one to solve the problem
				for (SearchAgent OtherAgent : agents) {
					if(!agent.equals(OtherAgent) && OtherAgent.status!=Status.HELPING){
						//TODO: find solution
					}
				}
			}
		}


		System.err.println("conflict error");
		return solutions;
	}
	
}