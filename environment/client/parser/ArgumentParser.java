package client.parser;

import client.node.level.distancemap.DistanceMap;
import client.node.level.distancemap.FloydWarshallDistanceMap;

public class ArgumentParser{

	private final static String DISTANCEMAP_CLASS_PREFIX = "client.node.level.distancemap.";

	public static SettingsContainer parse(String[] args){
		SettingsContainer settings = new SettingsContainer();
		int i = 0;
		while( i < args.length ){
			switch(args[i]){
				case "-dm":
					try{
						Object _dm = Class.forName(DISTANCEMAP_CLASS_PREFIX + args[i+1]).newInstance();
						settings.dm = (DistanceMap) _dm;
						System.err.println("Initialized using " + settings.dm.name() + ".");
				    }catch( Exception e ){
				    	System.err.println("Unrecognized DistanceMap class. Using default map.");
				    	settings.dm = new FloydWarshallDistanceMap();
				    }
					break;
				default:
					if( i > 0 ) i--;
					break;
			}
			i+=2;
		}
		if(settings.dm==null){
			settings.dm = new FloydWarshallDistanceMap();
		}
		return settings;
	}
}
	