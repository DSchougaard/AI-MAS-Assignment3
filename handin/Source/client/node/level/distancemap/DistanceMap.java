package client.node.level.distancemap;

import client.node.level.Level;
import client.node.storage.Base;

public abstract class DistanceMap {

	public abstract Integer distance(Base p1, Base p2);
	public abstract Integer distance(int rowFrom, int colFrom, int rowTo, int colTo);
	public abstract String name();
	public abstract void initialize(Level level);

}