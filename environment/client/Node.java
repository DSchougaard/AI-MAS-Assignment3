package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import client.Command.dir;
import client.Command.type;
import client.SearchClient.Color;
import client.map.Level;
public class Node {

	private static Random rnd = new Random( 1 ); 
	public static int MAX_ROW = 70;
	public static int MAX_COLUMN = 70;

//	public int agentRow;
//	public int agentCol;

	// Arrays are indexed from the top-left of the level, with first index being row and second being column.
	// Row 0: (0,0) (0,1) (0,2) (0,3) ...
	// Row 1: (1,0) (1,1) (1,2) (1,3) ...
	// Row 2: (2,0) (2,1) (2,2) (2,3) ...
	// ...
	// (Start in the top left corner, first go down, then go right)
	// E.g. walls[2] is an array of booleans having size MAX_GRID
	// walls[row][col] is true if there's a wall at (row, col)
	//

	private static Map<Character, Color> colors;
	
	public static boolean[][] walls = new boolean[MAX_ROW][MAX_COLUMN];
	public static char[][] goals = new char[MAX_ROW][MAX_COLUMN];
	
	public Level level;
	
	public char[][] boxes = new char[MAX_ROW][MAX_COLUMN]; 
	
 
	public Node parent;
	public Command action;

	private int g;
	
	Agent agent;
	
	public int[][] agents=new int[10][2];
	{
		for (int i = 0; i < agents.length; i++) {
			agents[i][0]=-1;
			agents[i][1]=-1;
		}
	}
	
	public Node( Node parent, Map<Character, Color> colors, Agent agent ) {
		this.agent=agent;
		Node.colors=colors;
		this.parent = parent;
		if ( parent == null ) {
			g = 0;
		} else {
			g = parent.g() + 1;
			level=parent.level;
		}
		
	}
	
	public Node( Node parent, Map<Character, Color> colors ) {
		Node.colors=colors;
		this.parent = parent;
		if ( parent == null ) {
			g = 0;
		} else {
			g = parent.g() + 1;
			level=parent.level;
		}
		
	}
	
	public Node( Node parent) {
		
		this.parent = parent;
		if ( parent == null ) {
			g = 0;
		} else {
			g = parent.g() + 1;
			level=parent.level;
		}
		
	}
	public void init(int MAX_ROW,int MAX_COLUMN){
		Node.MAX_COLUMN=MAX_COLUMN;
		Node.MAX_ROW=MAX_ROW;
		walls = new boolean[MAX_ROW][MAX_COLUMN];
		goals = new char[MAX_ROW][MAX_COLUMN];
		
	}
	
	public void init(Level level){
		this.level=level;
	}

	public int g() {
		return g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}


	
	public boolean isGoalState() {
//		System.err.println("-------"+colors.toString()+"  ----- "+agent.color);
		for ( int row = 1; row < MAX_ROW - 1; row++ ) {
			for ( int col = 1; col < MAX_COLUMN - 1; col++ ) {
				char g = goals[row][col];
				char b = Character.toLowerCase( boxes[row][col] );

		
				if(g>0 && colors.get(Character.toUpperCase(g)).equals(agent.color) && b != g){

						return false;
					
				}
			}
		}
		System.err.println("goal");
		return true;
	}


	public ArrayList< Node > getExpandedNodes() {
		ArrayList< Node > expandedNodes = new ArrayList< Node >( Command.every.length );
		for ( Command c : Command.every ) {
			// Determine applicability of action
			int newAgentRow = agents[agent.id][0] + dirToRowChange( c.dir1 );
			int newAgentCol = agents[agent.id][1] + dirToColChange( c.dir1 );

			if ( c.actType == type.Move ) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if ( cellIsFree( newAgentRow, newAgentCol ) ) {
					
					Node n = this.ChildNode();
					n.action = c;
					n.agents[agent.id][0] = newAgentRow;
					n.agents[agent.id][1] = newAgentCol;
					expandedNodes.add( n );
					
				}
			} else if ( c.actType == type.Push ) {
				// Make sure that there's actually a box to move
				if ( boxAt( newAgentRow, newAgentCol ) && agent.color.equals(colors.get(boxes[newAgentRow][newAgentCol]))) {
//				if ( boxAt( newAgentRow, newAgentCol ) ) {
					int newBoxRow = newAgentRow + dirToRowChange( c.dir2 );
					int newBoxCol = newAgentCol + dirToColChange( c.dir2 );
					// .. and that new cell of box is free
					if ( cellIsFree( newBoxRow, newBoxCol ) ) {

						Node n = this.ChildNode();
						n.action = c;
						n.agents[agent.id][0] = newAgentRow;
						n.agents[agent.id][1] = newAgentCol;
						n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
						n.boxes[newAgentRow][newAgentCol] = 0;
						expandedNodes.add( n );
					}
				}
			} else if ( c.actType == type.Pull ) {
				// Cell is free where agent is going
				if ( cellIsFree( newAgentRow, newAgentCol ) ) {
					int boxRow = agents[agent.id][0] + dirToRowChange( c.dir2 );
					int boxCol = agents[agent.id][1] + dirToColChange( c.dir2 );
					// .. and there's a box in "dir2" of the agent					
					if ( boxAt( boxRow, boxCol )  && agent.color == colors.get(boxes[boxRow][boxCol])) {
						Node n = this.ChildNode();
						n.action = c;
						n.agents[agent.id][0] = newAgentRow;
						n.agents[agent.id][1] = newAgentCol;
						n.boxes[agents[agent.id][0]][agents[agent.id][1]] = this.boxes[boxRow][boxCol];
						n.boxes[boxRow][boxCol] = 0;
						expandedNodes.add( n );
					}
				}
			}
		}
		Collections.shuffle( expandedNodes, rnd );
		
		return expandedNodes;
	}


	private boolean cellIsFree( int row, int col ) {
		return ( !Node.walls[row][col] && this.boxes[row][col] == 0 );
	}

	private boolean boxAt( int row, int col ) {
		return this.boxes[row][col] > 0;
	}

	private int dirToRowChange( dir d ) { 
		return ( d == dir.S ? 1 : ( d == dir.N ? -1 : 0 ) ); // South is down one row (1), north is up one row (-1)
	}

	private int dirToColChange( dir d ) {
		return ( d == dir.E ? 1 : ( d == dir.W ? -1 : 0 ) ); // East is left one column (1), west is right one column (-1)
	}

	private Node ChildNode() {
		Node copy = new Node( this );
		for ( int row = 0; row < MAX_ROW; row++ ) {
//			System.arraycopy( this.walls[row], 0, copy.walls[row], 0, MAX_COLUMN );
			System.arraycopy( this.boxes[row], 0, copy.boxes[row], 0, MAX_COLUMN );
//			System.arraycopy( this.goals[row], 0, copy.goals[row], 0, MAX_COLUMN );
		}
		copy.agent=this.agent;
		return copy;
	}

	public LinkedList< Node > extractPlan() {
		LinkedList< Node > plan = new LinkedList< Node >();
		Node n = this;
		while( !n.isInitialState() ) {
			plan.addFirst( n );
			n = n.parent;
		}
		return plan;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode( boxes );
		result = prime * result + Arrays.deepHashCode( agents );

		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Node other = (Node) obj;
		if ( !Arrays.deepEquals( boxes, other.boxes ) ) {
			return false;
		}

		for (int i = 0; i < agents.length; i++) {
			for (int j = 0; j < agents[0].length; j++) {
				if(agents[i][j]!=other.agents[i][j]){
					return false;
				}
			}
		}

		return true;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for ( int row = 0; row < MAX_ROW; row++ ) {
			if ( !Node.walls[row][0] ) {
				break;
			}
			for ( int col = 0; col < MAX_COLUMN; col++ ) {
				if ( this.boxes[row][col] > 0 ) {
					s.append( this.boxes[row][col] );
				} else if ( Node.goals[row][col] > 0 ) {
					s.append( Node.goals[row][col] );
				} else if ( Node.walls[row][col] ) {
					s.append( "+" );
				} else {
					s.append( " " );
				}
				for (int i = 0; i < agents.length; i++) {
					if(row==agents[i][0] && col ==agents[i][1]){
						s.replace(s.length()-1, s.length(), i+"");
//						s.append( i );
					}
				}
			}

			s.append( "\n" );
		}
		return s.toString();
	}

	public Color getColor(Character c){
		return colors.get(c);
	}
}