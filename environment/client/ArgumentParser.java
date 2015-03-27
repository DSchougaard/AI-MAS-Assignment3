package client;

import java.lang.reflect.*;

import client.SettingsContainer;

import client.node.map.DistanceMap;
import client.node.map.BasicManhattanDistanceMap;

public class ArgumentParser{

	private final static String DISTANCEMAP_CLASS_PREFIX = "client.node.map.";

	public static SettingsContainer parse(String[] args){
		SettingsContainer settings = new SettingsContainer();
		int i = 0;
		while( i < args.length ){
			switch(args[i]){
				case "-dm":
					try{
						Object _dm = Class.forName(DISTANCEMAP_CLASS_PREFIX + args[i+1]).newInstance();
						settings.dm = (DistanceMap) _dm;
				    }catch( Exception e ){
				    	System.err.println("Unrecognized DistanceMap class. Using default map.");
				    	settings.dm = new BasicManhattanDistanceMap();
				    }
					break;
				default:
					if( i > 0 ) i--;
					break;
			}
			i+=2;
		}
		return settings;
	}
}
	