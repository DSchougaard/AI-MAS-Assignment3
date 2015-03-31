package client.node;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import client.Command;
import client.Command.dir;
import client.Command.type;
import client.node.map.Level;
import client.node.map.LevelInterface;
import client.node.storage.Agent;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.Goal;


public class Node implements NodeInterface, LevelInterface{
	private static Random rnd = new Random( System.currentTimeMillis() );
//	private static Random rnd = new Random( 1 );
	private static Level level;
	// Box DS
	HashMap<Character, ArrayList<Box>> boxesByType;
	HashMap<Point, Box> boxesByPoint;

	// Agents
	public Agent[] agents;
	
	// History
	public Node parent;
	public ArrayList<Command> actions= new ArrayList<>();
	public Command action;
	private int g;
	
	
	public Node(){
		boxesByType 	= new HashMap<Character, ArrayList<Box>>();
		boxesByPoint 	= new HashMap<Point, Box>();
		agents 			= new Agent[10];
		g=0;

	}
	
	public Node(Level level){
		this.level = level;
		boxesByType 	= new HashMap<Character, ArrayList<Box>>();
		boxesByPoint 	= new HashMap<Point, Box>();
		agents 			= new Agent[10];
		g=0;
	}


	// Add'ers for the setup
	public void addAgent(char name, Color color, int row, int col){
		int i = (int)name - 48;
		if( agents[i] != null ) return;
		agents[i] = new Agent(i, color, row, col);
	}





	@SuppressWarnings("unused")
	private void removeBox(Box box){
		this.boxesByPoint.remove(new Point(box.row, box.col));
		ArrayList<Box> boxList = boxesByType.get(box.getType());
		boxList.remove(box);
	}
	private void addBox(Box box){
		this.boxesByPoint.put(new Point(box.row, box.col), box);
		
		ArrayList<Box> boxList= boxesByType.get(box.getType());
		if(boxList==null){
			boxesByType.put(box.getType(), new ArrayList<>());
			boxList= boxesByType.get(box.getType());
		}
		
		boxList.add(box);
		
	}
	
	public void addBox(char type, Color color, int row, int col){
		addBox(new Box(type, color, row, col));
	}
	
	private void moveBox(Box box, int row, int col){
		boxesByPoint.remove(new Point(box.row, box.col));
		box.row=row;
		box.col=col;
		
		boxesByPoint.put(new Point(box.row, box.col),box);
	}


	// Methods from NodeInterface
	@Override
	public ArrayList<Box> getBoxes(char type){

		return boxesByType.get(type);
	}

	@Override
	public ArrayList<Box> getBoxes(Color color){

		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public Box[] getBoxes(){
		return boxesByPoint.values().toArray(new Box[0]);
	}
	
	@Override
	public boolean cellIsFree(int row, int col){
		// Sanity check on coords
//		if( !(row >= 0 && col >= 0 && row <= this.level.getRow() && col <= this.level.getCol()) )
//			return false;

		if( this.level.isWall(row, col) )
			return false;

		if( this.boxesByPoint.containsKey( new Point(row, col) ) )
			return false;

		if( this.agentAt(row, col) != null )
			return false;

		return true;
	}

	@Override
	public Agent[] getAgents(){
		return this.agents;
	}

	@Override
	public Agent agentAt(int row, int col){
		for( int i = 0 ; i < 10 ; i++ ){
			if( this.agents[i]!=null && this.agents[i].isAt(row, col) )
				return this.agents[i];
		}
		return null;
	}

	@Override
	public Box boxAt(int row, int col){
		return this.boxesByPoint.get(new Point(row, col));
	}

	@Override
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

	@Override
	public boolean isGoalState(){
		return isGoalState(this.level.getGoals());
	
	}

	/**
	 * GoalEval on the reduced subset of goals
	 */
	@Override
	public boolean isGoalState(Color color){
		return isGoalState( this.getGoals(color) );
	}
	@Override
	public boolean isGoalState(Goal goal){
		return ( this.boxesByPoint.containsKey( goal.getPoint() ) && this.boxesByPoint.get( goal.getPoint() ).getType() == goal.type );
	}

	@Override
	public boolean isGoalState(ArrayList<Goal> goals){
		for( int i = 0 ; i < goals.size() ; i++ ){
			Point p = goals.get(i).getPoint();
			Box b = this.boxesByPoint.get(p);
			if( b==null ){
				return false;
			}

			if( b.getType() != goals.get(i).type ){
				return false;
			}
		}
		return true;
	}

	@Override
	public Node subdomain(Color color){
		Node subdomainNode = new Node(this.level);
		// Determine which agents falls within the color
		for( int i = 0 ; i < this.agents.length ; i++ ){
			if( this.agents[i].color == color )
				subdomainNode.agents[i] = new Agent(this.agents[i]);
		}
		for( Box b : this.boxesByPoint.values() ){
			if( b.color == color )
				subdomainNode.addBox(b);
		}
		return subdomainNode;
	}

	@Override
	public Node subdomain(ArrayList<Agent> agents){
		Node subdomainNode = new Node(this.level);
		for( Agent a : agents ){
			subdomainNode.agents[a.id] = new Agent(a);
		}
		for( Box b  : this.boxesByPoint.values() ){
			for( Agent a : agents ){
				if( a.color == b.color )
					subdomainNode.addBox(b);
			}
		}
		return subdomainNode;
	}

	@Override
	public Node subdomain(Color color, Agent agent){
		if( agent.color != color )
			return null;

		ArrayList<Agent> a = new ArrayList<Agent>();
		a.add(agent);
		return subdomain(a);
	}




















	// Methods from LevelInterface. Parsed directly to LevelInterface.
	@Override
	public ArrayList<Goal> getGoals(){
		return this.level.getGoals();
	}

	@Override
	public ArrayList<Goal> getGoals(char chr){
		return this.level.getGoals(chr);
	}
	
	@Override
	public ArrayList<Goal> getGoals(Color color){
		return this.level.getGoals(color);
	}

	@Override
	public boolean isWall(int row, int col){
		return this.level.isWall(row, col);
	}

	@Override
	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return this.level.distance(rowFrom, colFrom, rowTo, colTo);
	}
	
	@Override
	public int distance(Base from, Base to) {
		return this.level.distance(from, to);

	}

	public void calculateCluster(Agent[] agents){
		this.level.calculateCluster(agents);
	}
	public HashMap<Integer, ArrayList<Goal>> getClusters(){
		return this.level.getClusters();
	}
	public ArrayList<Goal> getCluster(Agent agent){
		ArrayList<Goal> t 			= this.level.getCluster(agent);
		ArrayList<Goal> filtered 	= new ArrayList<Goal>();

		// Bypass filtering
		// return t;

		// Filter
		for( Goal g : t ){
			if( !this.boxesByPoint.containsKey(g.getPoint())
			  ||(this.boxesByPoint.containsKey(g.getPoint()) && this.boxesByPoint.get(g.getPoint()).getType() != g.getType() )){
				filtered.add(g);
			}
		}

		return this.level.getCluster(agent);
	}

	@Override
	public ArrayList< Node > getExpandedNodes(int agentID) {
		ArrayList< Node > expandedNodes = new ArrayList< Node >( Command.every.length );
		for ( Command c : Command.every ) {
			// Determine applicability of action
			
			int newAgentRow = agents[agentID].row + dirToRowChange( c.dir1 );
			int newAgentCol = agents[agentID].col + dirToColChange( c.dir1 );
			Box box;
			if ( c.actType == type.Move ) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if ( cellIsFree( newAgentRow, newAgentCol ) ) {
					
					Node child = ChildNode();
					child.action = c;
					child.agents[agentID].row = newAgentRow;
					child.agents[agentID].col = newAgentCol;
					expandedNodes.add( child );

				}
			} else if ( c.actType == type.Push ) {
				
				// Make sure that there's actually a box to move
				box = boxAt(newAgentRow, newAgentCol);
				if ( box!=null && agents[agentID].color.equals(box.color)) {
					
					int newBoxRow = newAgentRow + dirToRowChange( c.dir2 );
					int newBoxCol = newAgentCol + dirToColChange( c.dir2 );
					// .. and that new cell of box is free
					if ( cellIsFree( newBoxRow, newBoxCol ) ) {
						
						Node n = this.ChildNode();
						n.action = c;
						n.agents[agentID].row = newAgentRow;
						n.agents[agentID].col = newAgentCol;

						n.moveBox(n.boxAt(newAgentRow, newAgentCol), newBoxRow, newBoxCol);

						expandedNodes.add( n );
						
					}

				}
				

			} else if ( c.actType == type.Pull ) {
				// Cell is free where agent is going
				if ( cellIsFree( newAgentRow, newAgentCol ) ) {
					
					int boxRow = agents[agentID].row + dirToRowChange( c.dir2 );
					int boxCol = agents[agentID].col + dirToColChange( c.dir2 );
					// .. and there's a box in "dir2" of the agent	
					box = boxAt( boxRow, boxCol );
					if ( box!= null  && agents[agentID].color == box.color) {

						Node n = this.ChildNode();
						n.action = c;
						n.agents[agentID].row = newAgentRow;
						n.agents[agentID].col = newAgentCol;
	
						n.moveBox(n.boxAt(boxRow, boxCol), agents[agentID].row, agents[agentID].col);

						expandedNodes.add( n );
					}
				}
				
				

			}
		}
		Collections.shuffle( expandedNodes, rnd );
		
		return expandedNodes;
	}

	private static int dirToRowChange( dir d ) { 
		return ( d == dir.S ? 1 : ( d == dir.N ? -1 : 0 ) ); // South is down one row (1), north is up one row (-1)
	}

	private static int dirToColChange( dir d ) {
		return ( d == dir.E ? 1 : ( d == dir.W ? -1 : 0 ) ); // East is left one column (1), west is right one column (-1)
	}

	@Override	
	public int g() {
		return g;
	}
	
	@Override
	public LinkedList<Node> extractPlan() {
		LinkedList< Node > plan = new LinkedList< Node >();
		Node n = this;
		while( !n.isInitialState() && n.action!=null) { //remove null
			plan.addFirst( n );
			n = n.parent;
		}
		return plan;
	}
	public boolean isInitialState() {
		return this.parent == null;
	}

	public Node excecuteCommands(ArrayList<Command> cs){
		Node child = ChildNode();
		for (int i = 0; i < cs.size(); i++) {
			if(cs.get(i)!=null){
				child.excecuteCommand( cs.get(i), i);
			}
		}
	
		return child;
		
	}

	public Node excecuteCommands(ArrayList<Command> cs, int agent){
		Node child = ChildNode();
		for (int i = 0; i < cs.size(); i++) {
			if(cs.get(i)!=null){
				child.excecuteCommand(cs.get(i), agent);
			}
		}
		
		return child;
		
	}

	private void excecuteCommand(Command c, int agentID){
		actions.add(c);
		int newAgentRow = agents[agentID].row + dirToRowChange( c.dir1 );
		int newAgentCol = agents[agentID].col + dirToColChange( c.dir1 );
		switch (c.actType) {
		case Move:
			agents[agentID].row = newAgentRow;
			agents[agentID].col = newAgentCol;
			break;
		case Push:
			int newBoxRow = newAgentRow + dirToRowChange( c.dir2 );
			int newBoxCol = newAgentCol + dirToColChange( c.dir2 );
			agents[agentID].row = newAgentRow;
			agents[agentID].col = newAgentCol;
			moveBox(boxAt(newAgentRow, newAgentCol), newBoxRow, newBoxCol);
			
			break;
		case Pull:
			int boxRow = agents[agentID].row + dirToRowChange( c.dir2 );
			int boxCol = agents[agentID].col + dirToColChange( c.dir2 );
			
			int tmpAgentRow = agents[agentID].row;
			int tmpAgentCol = agents[agentID].col;
			
			agents[agentID].row = newAgentRow;
			agents[agentID].col = newAgentCol;
			moveBox(boxAt(boxRow, boxCol), tmpAgentRow, tmpAgentCol);
			break;
		case NoOp:
			break;
		default:
			throw new UnsupportedOperationException(c.toString());

		}

		
	}
	
	
	private Node ChildNode() {
		Node child =CopyNode();
		child.parent=this;
		child.g+=1;
		
		return child;
	}

	
	public Node CopyNode() {
		Node copy= new Node();
		copy.level=this.level;
		for (int i = 0; i < agents.length; i++) {
			if(this.agents[i]!=null){
				copy.agents[i]=new Agent(this.agents[i]);
			}
		}

		for (Box box : this.boxesByPoint.values()) {
			copy.addBox(new Box(box));
		}

		copy.g=this.g;
		copy.parent=this.parent;

		return copy;
	}
	
	@Override
	public String toString(){
		Character[][] map=level.toArray();
		for (int i = 0; i < agents.length; i++) {
			if(agents[i]!=null){
				map[agents[i].row][agents[i].col]= (char) ((int)'0'+agents[i].id);
			}
		}
		
//		getBoxes().forEach(box-> map[box.row][box.col]=Character.toUpperCase(box.getType()));
		
		for (Box box : getBoxes()) {
			map[box.row][box.col]=Character.toUpperCase(box.getType());
		}
//		getBoxes().forEach(box-> System.err.println("box "+box.row+" "+box.col));
		StringBuilder s = new StringBuilder();
		s.append("\n");
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				s.append(map[i][j]);
			}
			s.append("\n");
		}
		
		return s.toString();
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(this.getBoxes());
//		result = prime * result + this.boxesByType.hashCode();
//		result = prime * result + this.boxesByPoint.hashCode();
		result = prime * result + Arrays.deepHashCode(agents);
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

		if( !Arrays.equals(this.agents, other.agents) ){
			return false;
		}

		if( !this.boxesByPoint.keySet().equals( other.boxesByPoint.keySet() ) ){
			return false;
		}
		
		for( Point p : boxesByPoint.keySet() ){
			if( !boxesByPoint.get(p).equals( other.boxesByPoint.get(p) ) ){
				return false;
			}
		}
		if( boxesByPoint.size() != other.boxesByPoint.keySet().size()){
			return false;
		}
//		if( !this.boxesByType.keySet().equals( other.boxesByType.keySet() ) ){
//			return false;
//		}
//
//		for( Character c : boxesByType.keySet() ){
//			if( !this.boxesByType.get(c).equals( other.boxesByType.get(c) ) ){	
//				return false;
//			}
//		}


		return true;
	}



}