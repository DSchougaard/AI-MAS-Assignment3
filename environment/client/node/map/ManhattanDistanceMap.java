package client.node.map;

import client.node.map.DistanceMap;
import client.node.map.Level.Cell;

import java.awt.Point;


public class ManhattanDistanceMap extends DistanceMap{

	private class KeyWrapper{
		private Point a, b;
		public KeyWrapper(Point a, Point b){
			this.a = a;
			this.b = b;
		}
	}

	public double distance(Point a, Point b){
		return a.distance(b);
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return (int)(new Point(rowFrom, colFrom)).distance( new Point(rowTo, colTo));
	}
	
	public ManhattanDistanceMap(Level map){
		for( int row = 0 ; row < map.getRow() ; row++ ){
			for( int col = 0 ; col < map.getCol() ; col++ ){
				
			}
		}
	}

}