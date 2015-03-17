package client.map;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;



/*
	@author: Daniel Schougaard
*/

class Level{


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

	private ArrayList<String> tempMapContainer;
	private int maxRow;
	private int maxCol;

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
	public Level(DistanceMap dmap){
		this.tempMapContainer = new ArrayList<String>();
		this.goals = new HashMap<Character, ArrayList<Cell> >();
	}









	/*
		Setup Methods for level
	*/
	public void feedLine(String line){
		tempMapContainer.add(line);
	}

	public void finish(){
		maxRow 	= tempMapContainer.size();
		maxCol 	= 0;
		//loop through rows to find max length of level
		for( int i = 0 ; i < maxRow ; i++ ){
			if( tempMapContainer.get(i).length() > maxCol )
				maxCol = tempMapContainer.get(i).length();
		}

		map = new Cell[maxCol][maxRow];

		// loop through all rows
		String line;
		for( int row = 0; row < maxRow ; row++ ){
			// loop through the row
			line = tempMapContainer.get(row);

			for( int col = 0 ; col < maxCol ; col++ ){

				if( line.charAt( col ) == ' ' ){
					map[col][row] = new Cell(Type.SPACE);
				}else if(line.charAt( col ) == '+' ){
					map[col][row] = new Cell(Type.WALL);
				}else if( line.charAt( col ) >= 'a' && line.charAt( col ) <= 'z' ){
					char chr = line.charAt( col );
					map[col][row] = new Cell(Type.GOAL, chr );

					// check if the letter exists
					if( !goals.containsKey(new Character(chr)) ){
						goals.put(new Character(chr), new ArrayList<Cell>());
					}

					ArrayList<Cell> c = goals.get(new Character(chr));
					c.add(map[col][row]);
				}
			}
		}

		// Destroy reference to temp container. Let JAVA GC handle it.
		this.tempMapContainer = null;
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

	public List<Cell> getGoals(char chr){
		return this.goals.get(new Character(chr));
	}
	public Map<Character, List<Cell>> getAllGoals(){
		return this.goals;
	}
}


