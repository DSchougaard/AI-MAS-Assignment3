package client.node;


import java.awt.Point;
import java.io.*;
import java.util.*;

import client.node.map.*;

import client.node.storage.*;

public class Node implements NodeInterface, LevelInterface{


	private Level level;
	// Box DS
	HashMap<Character, ArrayList<Box>> boxesByType;
	HashMap<Point, Box> boxesByPoint;

	// Agents
	HashMap<Character, Agent> agents;


	public Node(Level level){
		this.level = level;

		boxesByType 	= new HashMap<Character, ArrayList<Box>>();
		boxesByPoint 	= new HashMap<Point, Box>();
		agents 			= new HashMap<Character, Agent>();
	}

	public Node(Node parent){
		System.err.println("Not yet implemented");
	}

	// Add'ers for the setup
	public void addAgent(char name, String color, int col, int row){
		this.agents.put( name, new Agent(name, color, col, row) );
	}

	public void addBox(char type, String color, int col, int row){
		if( !boxesByType.containsKey( type ) ){
			boxesByType.put( new Character(type), new ArrayList<Box>() );
		}

		Box newBox = new Box(type, color, col, row);
		ArrayList<Box> boxSet = boxesByType.get(new Character(type));
		boxSet.add(newBox);

		boxesByPoint.put( new Point(col, row), newBox);
	}






	// Methods from NodeInterface
	public ArrayList<Box> getBoxes(char color){
		return boxesByType.get(new Character(color));
	}

	public HashMap<Character, ArrayList<Box>> getAllBoxes(){
		return this.boxesByType;
	}

	public boolean cellIsFree(int row, int col){
		return false;
	}

	public ArrayList<Agent> getAgents(){
		return new ArrayList<Agent>(this.agents.values());
	}

	public Box boxAt(int row, int col){
		return this.boxesByPoint.get(new Point(row, col));
	}

	// Methods from LevelInterface. Parsed directly to LevelInterface.
	public ArrayList<Goal> getGoals(char chr){
		return null;
	}

	public HashMap<Character, ArrayList<Goal>> getAllGoals(){
		return null;
	}

	public boolean isWall(int col, int row){
		return this.level.isWall(row, col);
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return this.level.distance(rowFrom, colFrom, rowTo, colTo);
	}





	//TODO: toString()
	//TODO: hashCode()
	//TODO: equals(Object o)






}