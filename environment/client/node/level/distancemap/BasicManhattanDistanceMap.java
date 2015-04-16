package client.node.level.distancemap;

import client.node.level.Level;
import client.node.storage.Base;

public class BasicManhattanDistanceMap extends DistanceMap{
	public BasicManhattanDistanceMap(){}

	public Integer distance(Base a, Base b){
		return this.distance(a.row, a.col, b.row, b.col);
	}

	public Integer distance(int rowFrom, int colFrom, int rowTo, int colTo){		
		return Math.abs(rowFrom-rowTo)+Math.abs(colFrom-colTo);
	}
	
	public String name(){
		return "BasicManhattanDistanceMap";
	}

	public void initialize(Level level){}
}