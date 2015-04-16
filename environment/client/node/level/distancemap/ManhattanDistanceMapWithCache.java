package client.node.level.distancemap;

import java.util.HashMap;

import client.node.level.Level;
import client.node.storage.Base;

public class ManhattanDistanceMapWithCache extends DistanceMap{

	private HashMap<Base, HashMap<Base, Integer> > cache;
	private BasicManhattanDistanceMap dm;

	public ManhattanDistanceMapWithCache(){
		this.cache 	= new HashMap<Base, HashMap<Base, Integer> >();
		this.dm 	= new BasicManhattanDistanceMap();
	}

	public int distance(Base p1, Base p2){
		if( !cache.containsKey( p1 ) )
			cache.put(p1, new HashMap<Base, Integer>());

		HashMap<Base, Integer> l2Cache = cache.get(p1);

		if( !l2Cache.containsKey(p2) )
			l2Cache.put(p2, this.dm.distance(p1.row, p1.col, p2.row, p2.col));

		return l2Cache.get(p2);
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return this.distance( new Base(rowFrom, colFrom), new Base(rowTo, colTo));
	}	

	public String name(){
		return "ManhattanDistanceMapWithCache";
	}

	public void initialize(Level level){}
}