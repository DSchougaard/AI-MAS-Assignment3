package client.node.map;

import java.awt.Point;


public class BasicManhattanDistanceMap extends DistanceMap{

	public BasicManhattanDistanceMap(){

	}


	private class KeyWrapper{
		private Point a, b;
		public KeyWrapper(Point a, Point b){
			this.a = a;
			this.b = b;
		}
	}

	public int distance(Point a, Point b){
		return this.distance(a.x, a.y, b.x, b.y);
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return (int)(new Point(rowFrom, colFrom)).distance( new Point(rowTo, colTo));
	}
	

}