package client.node;


import java.awt.Point;
import java.io.*;
import java.util.*;

import client.node.map.*;

public class Node implements NodeInterface, LevelInterface{



	public class Box{
		public char type;
		public String color;
		public int col, row;
		public Box(char t, String color, int col, int row){ 
			this.type = t;
			this.color = color;
			this.col = col;
			this.row = row;
		}
	}

	public class Agent{
		public char name;
		public String color;
		public int col, row;
		public Agent(char name, String color, int col, int row){
			this.name 	= name;
			this.color 	= color;
			this.col 	= col;
			this.row 	= row;
		}
	}

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


	// Methods from LevelInterface. Parsed directly to LevelInterface.
	public ArrayList<Point> getGoals(char chr){
		return null;
	}

	public HashMap<Character, ArrayList<Point>> getAllGoals(){
		return null;
	}

	public boolean isWall(int col, int row){
		return this.level.isWall(row, col);
	}










	//TODO: toString()
	//TODO: hashCode()
	//TODO: equals(Object o)






}