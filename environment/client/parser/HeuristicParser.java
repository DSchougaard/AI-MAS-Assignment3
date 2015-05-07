package client.parser;

import java.lang.reflect.Constructor;

import client.SearchAgent;
import client.heuristic.AStar;
import client.heuristic.Greedy;
import client.heuristic.WeightedAStar;
import client.heuristic.Heuristic;

import client.heuristic.*;

public class HeuristicParser{
	private final static String HEURISTIC_CLASS_PREFIX = "client.heuristic.";
	
	public static Heuristic parse(SearchAgent agent, String heuristic){
		Heuristic h;
		try{
			Class<?> clazz = Class.forName(HEURISTIC_CLASS_PREFIX + heuristic);
			Constructor<?> constructor = clazz.getConstructor(SearchAgent.class);
			Object instance = constructor.newInstance(agent);
			h = (Heuristic) instance;
			System.err.println("Initialized using " + h.toString() + ".");
		}catch( Exception e ){
			System.err.println("Unrecognized Heuristic class. Using default heuristic.");
			h = new Greedy(agent);
		}
		return h;
	}
	
}