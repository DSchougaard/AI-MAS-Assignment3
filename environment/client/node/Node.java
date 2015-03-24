package client.node;


import java.awt.Point;
import java.io.*;
import java.util.*;

import client.node.map.*;
import client.node.storage.*;
import client.node.Color;

public class Node implements NodeInterface, LevelInterface{


	private Level level;
	// Box DS
	HashMap<Character, ArrayList<Box>> boxesByType;
	HashMap<Point, Box> boxesByPoint;

	// Agents
	Agent[] agents;


	public Node(Level level){
		this.level = level;

		boxesByType 	= new HashMap<Character, ArrayList<Box>>();
		boxesByPoint 	= new HashMap<Point, Box>();
		agents 			= new Agent[10];
	}

	public Node(Node parent){
		System.err.println("Not yet implemented");
	}

	// Add'ers for the setup
	public void addAgent(char name, Color color, int row, int col){
		int i = (int)name - 48;
		if( agents[i] != null ) return;

		agents[i] = new Agent(name, color, row, col);
	}

	public void addBox(char type, Color color, int row, int col){
		if( !boxesByType.containsKey( type ) ){
			boxesByType.put( new Character(type), new ArrayList<Box>() );
		}

		Box newBox = new Box(type, color, row, col);
		ArrayList<Box> boxSet = boxesByType.get(new Character(type));
		boxSet.add(newBox);

		boxesByPoint.put( new Point(row, col), newBox);
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

	public Agent[] getAgents(){
		return this.agents;
	}

	public Box boxAt(int row, int col){
		return this.boxesByPoint.get(new Point(row, col));
	}

	public Object WTF(int row, int col){
		Point p = new Point(row, col);
		
		// Check for boxes
		if( boxesByPoint.containsKey(p) ){
			return boxesByPoint.get(p);
		}

		// Check for agents
		for( int i = 0; i < 10 ; i++ ){
			if( agents[i].at(row, col) )
				return agents[i];
		}

		// Nothing was found
		return null;
	}

	// Methods from LevelInterface. Parsed directly to LevelInterface.
	public ArrayList<Goal> getGoals(char chr){
		return null;
	}

	public HashMap<Character, ArrayList<Goal>> getAllGoals(){
		return null;
	}

	public boolean isWall(int row, int col){
		return this.level.isWall(row, col);
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return this.level.distance(rowFrom, colFrom, rowTo, colTo);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result ^ this.boxesByType.hashCode();
		result = prime * result ^ this.boxesByPoint.hashCode();
		result = prime * result ^ Arrays.deepHashCode(agents);
		return result;
	}





	//TODO: toString()
	//TODO: hashCode()
	//TODO: equals(Object o)






}