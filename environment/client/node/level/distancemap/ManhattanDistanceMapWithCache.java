package client.node.level.distancemap;

import java.util.*;
import java.awt.Point;

import client.node.level.Level;

public class ManhattanDistanceMapWithCache extends DistanceMap{

	private HashMap<Point, HashMap<Point, Integer> > cache;
	private BasicManhattanDistanceMap dm;

	public ManhattanDistanceMapWithCache(){
		this.cache 	= new HashMap<Point, HashMap<Point, Integer> >();
		this.dm 	= new BasicManhattanDistanceMap();
	}

	public int distance(Point p1, Point p2){
		if( !cache.containsKey( p1 ) )
			cache.put(p1, new HashMap<Point, Integer>());

		HashMap<Point, Integer> l2Cache = cache.get(p1);

		if( !l2Cache.containsKey(p2) )
			l2Cache.put(p2, this.dm.distance(p1.x, p1.y, p2.x, p2.y));

		return l2Cache.get(p2);
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return this.distance( new Point(rowFrom, colFrom), new Point(rowTo, colTo));
	}	

	public String name(){
		return "ManhattanDistanceMapWithCache";
	}

	public void initialize(Level level){}
}