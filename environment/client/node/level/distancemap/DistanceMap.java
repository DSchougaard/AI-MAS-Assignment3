package client.node.level.distancemap;

import java.awt.Point;

import client.node.level.Level;

public abstract class DistanceMap {

	public abstract int distance(Point p1, Point p2);
	public abstract int distance(int rowFrom, int colFrom, int rowTo, int colTo);
	public abstract String name();
	public abstract void initialize(Level level);

}