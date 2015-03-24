package client.node.map;


import java.io.*;
import java.util.*;

import java.awt.Point;




/*
	@author: Daniel Schougaard
*/

public class Level implements LevelInterface{


	/*
			-----------> col
			|
			|
			|
			|
			|
		    \/
		    row


	*/

	/*
		Private variables for Level
	*/

	private int maxRow;
	private int maxCol;
	private DistanceMap dm;


	public class Cell{

		private Type type;
		private char letter;

		public Cell(Type type){
			this.type = type;
		}

		public Cell(Type type, char letter){
			this.letter = letter;
			this.type = type;
		}
	}

	private static Cell[][] map;

	public enum Type { SPACE, WALL, GOAL, BOX, AGENT }

	private HashMap<Character, ArrayList<Point> > goals;






	// Constructor
	public Level(int maxCol, int maxRow){
		this.maxCol 	= maxCol;
		this.maxRow 	= maxRow;

		map 			= new Cell[maxCol][maxRow];
		this.goals 		= new HashMap<Character, ArrayList<Point>>();

	}	

	public Level(DistanceMap dmap){
		this.goals 				= new HashMap<Character, ArrayList<Point> >();
		this.dm 				= dmap;
	}


	// Setup methods for the Level
	public void addWall(int col, int row){
		this.map[col][row] = new Cell(Type.WALL);
	}

	public void addGoal(int col, int row, char letter){
		this.map[col][row] = new Cell(Type.GOAL, letter);

		if( !goals.containsKey(new Character(letter)) ){
			goals.put( new Character(letter), new ArrayList<Point>() );
		}

		ArrayList<Point> tempGoals = goals.get( new Character(letter) );
		tempGoals.add(new Point(col, row));
	}

	public void addSpace(int col, int row){
		this.map[col][row] = new Cell(Type.SPACE);
	}



	/*
		Interface for the Heuristic to use
	*/

	public int getCol(){
		return this.maxCol;
	}

	public int getRow(){
		return this.maxRow;
	}


	public char isGoal(int col, int row){
		if( this.map[col][row].type == Type.GOAL )
			return this.map[col][row].letter;

		return '\0';
	}

	public boolean isWall(int col, int row){
		return ( this.map[col][row].type == Type.WALL );
	}


	public ArrayList<Point> getGoals(char chr){
		return this.goals.get(new Character(chr));
	}


	public HashMap<Character, ArrayList<Point>> getAllGoals(){
		return this.goals;
	}
}


