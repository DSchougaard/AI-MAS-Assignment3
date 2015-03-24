package client.node;


import java.awt.Point;
import java.io.*;
import java.util.*;

import client.Command;
import client.Command.dir;
import client.Command.type;
import client.node.map.*;
import client.node.storage.*;
import client.node.Color;

public class Node implements NodeInterface, LevelInterface{

	private static Random rnd = new Random( System.currentTimeMillis() );
	private Level level;
	// Box DS
	HashMap<Character, ArrayList<Box>> boxesByType;
	HashMap<Point, Box> boxesByPoint;

	private void boxRemove(Box box){
		this.boxesByPoint.remove(new Point(box.row, box.col));
		ArrayList<Box> boxList = boxesByType.get(box.type);
		boxList.remove(box);
	}
	private void boxAdd(Box box){
		this.boxesByPoint.put(new Point(box.row, box.col), box);
		ArrayList<Box> boxList = boxesByType.get(box.type);
		boxList.add(box);
	}


	// Agents
	Agent[] agents;

	
	// history
	public Node parent;
	public ArrayList<Command> actions= new ArrayList<>();
	public Command action;
	
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
		// Sanity check on coords
		if( !(row >= 0 && col >= 0 && row <= this.level.getRow() && col <= this.level.getCol()) )
			return false;

		if( this.level.isWall(row, col) )
			return false;

		if( this.boxesByPoint.containsKey( new Point(row, col) ) )
			return false;

		if( this.agentAt(row, col) != null )
			return false;

		return true;
	}

	public Agent[] getAgents(){
		return this.agents;
	}

	public Agent agentAt(int row, int col){
		for( int i = 0 ; i < 10 ; i++ ){
			if( this.agents[i].isAt(row, col) )
				return this.agents[i];
		}
		return null;
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
			if( agents[i].isAt(row, col) )
				return agents[i];
		}

		// Nothing was found
		return null;
	}

	public boolean isGoalState(){
		ArrayList<Goal> goals = this.level.getAllGoals();
		for( int i = 0 ; i < goals.size() ; i++ ){
			Point p = goals.get(i).getPoint();
			if( !this.boxesByPoint.containsKey(p) )
				return false;

			Box b = this.boxesByPoint.get(p);
			if( b.type != goals.get(i).type )
				return false;
		}
		return true;
	}

























	// Methods from LevelInterface. Parsed directly to LevelInterface.
	public ArrayList<Goal> getGoals(char chr){
		return this.level.getGoals(chr);
	}

	public HashMap<Character, ArrayList<Goal>> getGoalMap(){
		return this.level.getGoalMap();
	}

	public ArrayList<Goal> getAllGoals(){
		return this.level.getAllGoals();
	}

	public boolean isWall(int row, int col){
		return this.level.isWall(row, col);
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return this.level.distance(rowFrom, colFrom, rowTo, colTo);
	}


//	public ArrayList< Node > getExpandedNodes(Agent agent) {
//		ArrayList< Node > expandedNodes = new ArrayList< Node >( Command.every.length );
//		for ( Command c : Command.every ) {
//			// Determine applicability of action
//			
//			int newAgentRow = agents[agent.id][0] + dirToRowChange( c.dir1 );
//			int newAgentCol = agents[agent.id][1] + dirToColChange( c.dir1 );
//
//			if ( c.actType == type.Move ) {
//				// Check if there's a wall or box on the cell to which the agent is moving
//				if ( cellIsFree( newAgentRow, newAgentCol ) ) {
//					
//					Node n = this.ChildNode();
//					n.action = c;
//					n.agents[agent.id][0] = newAgentRow;
//					n.agents[agent.id][1] = newAgentCol;
//					expandedNodes.add( n );
//					
//				}
//			} else if ( c.actType == type.Push ) {
//				// Make sure that there's actually a box to move
//				if ( boxAt( newAgentRow, newAgentCol ) && agent.color.equals(colors.get(boxes[newAgentRow][newAgentCol]))) {
////				if ( boxAt( newAgentRow, newAgentCol ) ) {
//					int newBoxRow = newAgentRow + dirToRowChange( c.dir2 );
//					int newBoxCol = newAgentCol + dirToColChange( c.dir2 );
//					// .. and that new cell of box is free
//					if ( cellIsFree( newBoxRow, newBoxCol ) ) {
//
//						Node n = this.ChildNode();
//						n.action = c;
//						n.agents[agent.id][0] = newAgentRow;
//						n.agents[agent.id][1] = newAgentCol;
//						
//						n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
//						n.boxes[newAgentRow][newAgentCol] = 0;
//						expandedNodes.add( n );
//					}
//				}
//			} else if ( c.actType == type.Pull ) {
//				// Cell is free where agent is going
//				if ( cellIsFree( newAgentRow, newAgentCol ) ) {
//					int boxRow = agents[agent.id][0] + dirToRowChange( c.dir2 );
//					int boxCol = agents[agent.id][1] + dirToColChange( c.dir2 );
//					// .. and there's a box in "dir2" of the agent					
//					if ( boxAt( boxRow, boxCol )  && agent.color == colors.get(boxes[boxRow][boxCol])) {
//						Node n = this.ChildNode();
//						n.action = c;
//						n.agents[agent.id][0] = newAgentRow;
//						n.agents[agent.id][1] = newAgentCol;
//						n.boxes[agents[agent.id][0]][agents[agent.id][1]] = this.boxes[boxRow][boxCol];
//						n.boxes[boxRow][boxCol] = 0;
//						expandedNodes.add( n );
//					}
//				}
//			}
//		}
//		Collections.shuffle( expandedNodes, rnd );
//		
//		return expandedNodes;
//	}

	private static int dirToRowChange( dir d ) { 
		return ( d == dir.S ? 1 : ( d == dir.N ? -1 : 0 ) ); // South is down one row (1), north is up one row (-1)
	}

	private static int dirToColChange( dir d ) {
		return ( d == dir.E ? 1 : ( d == dir.W ? -1 : 0 ) ); // East is left one column (1), west is right one column (-1)
	}

	private Node ChildNode() {
		//TODO:
		
		return null;
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

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Node other = (Node) obj;
	
		if( !Arrays.equals(this.agents, other.agents) )
			return false;

		if( !this.boxesByPoint.keySet().equals( other.boxesByPoint.keySet() ) )
			return false;

		for( Point p : boxesByPoint.keySet() ){
			if( !boxesByPoint.get(p).equals( other.boxesByPoint.get(p) ) )
				return false;
		}

		if( !this.boxesByType.keySet().equals( other.boxesByType.keySet() ) )
			return false;

		for( Character c : boxesByType.keySet() ){
			if( !this.boxesByType.get(c).equals( other.boxesByType ) )
				return false;
		}


		return true;
	}



	//TODO: toString()
	//TODO: hashCode()
	//TODO: equals(Object o)






}