package client.parser;

import java.lang.reflect.Constructor;

import client.Strategy;
import client.heuristic.*;

import client.Settings;

public class StrategyParser{
	private final static String HEURISTIC_CLASS_PREFIX = "client.heuristic.";
	
	public static Strategy parse(Heuristic heuristic, String strategy){
		Strategy s;
		try{

			Class<?> clazz = Class.forName(HEURISTIC_CLASS_PREFIX + strategy);
			Constructor<?> constructor = clazz.getConstructor(Heuristic.class);
			Object instance = constructor.newInstance(heuristic);
			s = (Strategy) instance;
			if( Settings.Global.PRINT ){
				System.err.println("Initialized using " + s.toString() + ".");
			}
		}catch( Exception e ){
			if( Settings.Global.PRINT ){
				System.err.println("Unrecognized Strategy class. Using default strategy.");
			}
			s = new Greedy(heuristic);
		}
		return s;
	}
	
}