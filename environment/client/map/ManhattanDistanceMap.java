package client.map;

import client.map.DistanceMap;
import client.map.Level.Cell;

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

	public ManhattanDistanceMap(Level map){
		for( int row = 0 ; row < map.getRow() ; row++ ){
			for( int col = 0 ; col < map.getCol() ; col++ ){
				
			}
		}
	}

}