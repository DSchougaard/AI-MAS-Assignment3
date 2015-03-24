package client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



public class Conflict{
	
	
	
	
	public static ArrayList< LinkedList< Node > > solve(ArrayList< LinkedList< Node > > solutions, List< Agent > agents){
		
		int conflicts=0;
		for (Agent agent : agents) {
			if(agent.conflict){
				conflicts++;
				agent.conflict=false;
			}
		}

		if(conflicts==1){
			return cut(solutions);
		}
		
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