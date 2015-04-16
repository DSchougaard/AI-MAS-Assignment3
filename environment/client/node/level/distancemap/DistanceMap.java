package client.node.level.distancemap;

import client.node.level.Level;
import client.node.storage.Base;

public abstract class DistanceMap {

	public abstract int distance(Base p1, Base p2);
	public abstract int distance(int rowFrom, int colFrom, int rowTo, int colTo);
	public abstract String name();
	public abstract void initialize(Level level);

}