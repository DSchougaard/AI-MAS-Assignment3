package client.map;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;



/*
	@author: Daniel Schougaard
*/

class Map{


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
		Private variables for Map
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

	private HashMap<Character, ArrayList<Cell> > goals;






	// Constructor
	public Map(int maxCol, int maxRow){
		this.maxCol 	= maxCol;
		this.maxRow 	= maxRow;

		map 			= new Cell[maxCol][maxRow];
		this.goals 		= new HashMap<Character, ArrayList<Cell>>();

	}	

	public Map(DistanceMap dmap){
		this.goals 				= new HashMap<Character, ArrayList<Cell> >();
		this.dm 				= dmap;
	}


	// Setup methods for the Map
	public void addWall(int col, int row){
		cell[col][row] = new Cell(Type.WALL);
	}

	public void addGoal(int col, int row, char letter){
		cell[col][row] = new Cell(Type.GOAL, letter);
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
		if( cell[col][row].type == Type.GOAL )
			return cell[col][row].letter;

		return '\0';
	}


	public List<Cell> getGoals(char chr){
		return this.goals.get(new Character(chr));
	}
	public HashMap<Character, ArrayList<Cell>> getAllGoals(){
		return this.goals;
	}
}


