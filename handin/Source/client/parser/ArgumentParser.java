package client.parser;

import client.node.level.distancemap.DistanceMap;
import client.node.level.distancemap.FloydWarshallDistanceMap;

import client.Settings;

public class ArgumentParser{

	public static SettingsContainer parse(String[] args){
		SettingsContainer settings = new SettingsContainer();
		int i = 0;
		while( i < args.length ){
			switch(args[i]){
			case "-dm":
				try{
					Object _dm = Class.forName( Settings.ArgumentParser.DISTANCEMAP_CLASS_PREFIX + args[i+1]).newInstance();
					settings.dm = (DistanceMap) _dm;
					if( Settings.Global.PRINT){
						System.err.println("Initialized using " + settings.dm.name() + ".");
					}
				}catch( Exception e ){
					if( Settings.Global.PRINT){
						System.err.println("Unrecognized DistanceMap class. Using default map.");
					}
					settings.dm = new FloydWarshallDistanceMap();
				}
				break;
			case "-kc":
				switch (args[i+1]) {
				case "true":
					settings.kcluster=true;
					break;
				case "false":
					settings.kcluster=false;
					break;
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
