package client.node;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import client.Command;
import client.Command.dir;
import client.Command.type;
import client.node.level.Level;
import client.node.level.LevelInterface;
import client.node.storage.LogicalAgent;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.Goal;


public class Node implements NodeInterface, LevelInterface{

	private final static Random rnd = new Random( System.nanoTime());
	private static Level level;

	// Box DS
	private HashMap<Character, ArrayList<Box>> boxesByType;
	private HashMap<Point, Box> boxesByPoint;
	private HashMap<Integer, Box> boxesByID;

	// Agents
	public LogicalAgent[] agents;
	public static HashMap<Color, ArrayList<Integer>> colorMap = new HashMap<>();
	
	// History
	public Node parent;
	public ArrayList<Command> actions= new ArrayList<>();
	public Command action;
	private int g;
	
	public Node(){
		boxesByType 	= new HashMap<Character, ArrayList<Box>>();
		boxesByPoint 	= new HashMap<Point, Box>();
		boxesByID		= new HashMap<Integer, Box>();
		agents 			= new LogicalAgent[10];
		g=0;

	}
	
	public Node(Level level){
		Node.level = level;
		boxesByType 	= new HashMap<Character, ArrayList<Box>>();
		boxesByPoint 	= new HashMap<Point, Box>();
		boxesByID		= new HashMap<Integer, Box>();
		agents 			= new LogicalAgent[10];
		g=0;
	}


	// Add'ers for the setup
	public void addAgent(char name, Color color, int row, int col) throws IOException{
		int i = (int)name - 48;
		if( agents[i] != null  ) return;
		agents[i] = new LogicalAgent(i, color, row, col);

		if(color==null){
			color=Color.blue;
		}
		
		if( !colorMap.containsKey(color) )
			colorMap.put(color, new ArrayList<Integer>());

		colorMap.get(color).add(i);
	}

	@SuppressWarnings("unused")
	private void removeBox(Box box){
		this.boxesByPoint.remove(new Point(box.row, box.col));
		ArrayList<Box> boxList = boxesByType.get(box.getType());
		boxList.remove(box);
	}
	private void addBox(Box box){
		this.boxesByID.put(box.id, box);
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
		boxesByID.get(box.id);
		box.row=row;
		box.col=col;
		boxesByPoint.put(new Point(box.row, box.col),box);
		boxesByID.put(box.id, box);
	}

	/*
			NodeInterface
	*/
	@Override
	public ArrayList<Box> getBoxes(char type){

		return boxesByType.get(type);
	}

	@Override
	public ArrayList<Box> getBoxes(Color color){
		ArrayList<Box> results = new ArrayList<>();
		for(Box box:boxesByPoint.values()){
			if(box.color==color){
				results.add(box);
			}
		}
		return results;
	}

	@Override
	public Box[] getBoxes(){
		return boxesByPoint.values().toArray(new Box[0]);
	}

	public HashMap<Integer, Box> getBoxesByID(){
		return new HashMap<Integer, Box>(this.boxesByID);
	}



	@Override
	public boolean cellIsFree(int row, int col){
		if( Node.level.isWall(row, col) )
			return false;

		if( this.boxesByPoint.containsKey( new Point(row, col) ) )
			return false;

		if( this.agentAt(row, col) != null )
			return false;

		return true;
	}
	

	public ArrayList<Integer> getAgentIDs(Color color){
		return colorMap.get(color);
	}

	@Override
	public LogicalAgent[] getAgents(){
		return this.agents;
	}

	@Override
	public LogicalAgent agentAt(int row, int col){
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


	public Object objectAt(Base base){
		return objectAt(base.row, base.col);
	}

	@Override
	public Object objectAt(int row, int col){
		Point p = new Point(row, col);
		
		// Check for boxes
		if( boxesByPoint.containsKey(p) ){
			return boxesByPoint.get(p);
		}

		// Check for agents
		for( int i = 0; i < 10 ; i++ ){
			if(agents[i]!=null && agents[i].isAt(row, col) )
				return agents[i];
		}

		// Nothing was found
		return null;
	}

	public char isGoal(int row, int col){
		return level.isGoal(row, col);
	}
	


	/**
	 * GoalEval on the reduced subset of goals
	 */
	@Override
	public boolean isGoalState(){
		return isGoalState(Node.level.getGoals());
	}
	
	@Override
	public boolean isGoalState(Color color){
		return isGoalState( this.getGoals(color) );
	}
	@Override
	public boolean isGoalState(Goal goal){
		return ( this.boxesByPoint.containsKey( goal.getPoint() ) && this.boxesByPoint.get( goal.getPoint() ).getType() == goal.getType() );
	}

	@Override
	public boolean isGoalState(ArrayList<Goal> goals){
		if( goals == null ) return true;
		
		for( int i = 0 ; i < goals.size() ; i++ ){
			Point p = goals.get(i).getPoint();
			Box b = this.boxesByPoint.get(p);
			if( b==null ){
				return false;
			}

			if( b.getType() != goals.get(i).getType() ){
				return false;
			}
		}
		return true;
	}






	@Override
	public Node subdomain(Color color){
		Node subdomainNode = new Node();
		// Determine which agents falls within the color
		for( int i = 0 ; i < this.agents.length ; i++ ){
			if( this.agents[i].color == color )
				subdomainNode.agents[i] = new LogicalAgent(this.agents[i]);
		}
		for( Box b : this.boxesByPoint.values() ){
			if( b.color == color )
				subdomainNode.addBox(b);
		}
		return subdomainNode;
	}

	@Override
	public Node subdomain(ArrayList<Integer> agentIDs){
		Node subdomainNode = new Node();
		for( int agentID : agentIDs ){
			subdomainNode.agents[agentID] = new LogicalAgent(this.agents[agentID]);
		}
		for( Box b  : this.boxesByPoint.values() ){
			for( LogicalAgent a : agents ){
				if( a.color == b.color )
					subdomainNode.addBox(b);
			}
		}
		return subdomainNode;
	}

	@Override
	public Node subdomain(int agentID){
		Node subdomainNode = new Node();

		subdomainNode.agents[agentID] = new LogicalAgent(this.agents[agentID]);
		for(Box box: this.getBoxes(agents[agentID].color)){
			subdomainNode.addBox(box);
		}
		
		return subdomainNode;
	}


	/*
			LevelInterface
	*/
	@Override
	public ArrayList<Goal> getGoals(){
		return Node.level.getGoals();
	}

	@Override
	public ArrayList<Goal> getGoals(char chr){
		return Node.level.getGoals(chr);
	}
	
	@Override
	public ArrayList<Goal> getGoals(Color color){
		return Node.level.getGoals(color);
	}

	@Override
	public boolean isWall(int row, int col){
		return Node.level.isWall(row, col);
	}

	@Override
	public Integer distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return Node.level.distance(rowFrom, colFrom, rowTo, colTo);
	}
	
	@Override
	public Integer distance(Base from, Base to) {
		return Node.level.distance(from, to);
	}

	public void calculateCluster(LogicalAgent[] agents, boolean b){
		Node.level.calculateCluster(agents, b);
	}
	public HashMap<Integer, ArrayList<Goal>> getClusters(){
		return Node.level.getClusters();
	}
	public ArrayList<Goal> getCluster(int agentID){
		ArrayList<Goal> cluster 	= Node.level.getCluster(agentID);
		ArrayList<Goal> filtered 	= new ArrayList<Goal>();

		// Bypass filtering
		// return t;
		// Filter

		if( cluster == null )
			return new ArrayList<Goal>();

		for( Goal goal : cluster ){
			Box box=this.boxesByPoint.get(goal.getPoint());
			if(box == null || (box.getType() != goal.getType())){
				filtered.add(goal);
			}
		}

		return filtered;
	}


	/**
	 * possible states/nodes to reach from current node. when the agent only is allow to do action involving a box
	 * 
	 * @param agentID
	 * @return list of reachable states
	 */
	public ArrayList< Node > getExpandedBoxNodes(int agentID) {
		ArrayList< Node > expandedNodes = new ArrayList< Node >( Command.every.length );
		for ( Command c : Command.every ) {
			// Determine applicability of action
			
			int newAgentRow = agents[agentID].row + dirToRowChange( c.dir1 );
			int newAgentCol = agents[agentID].col + dirToColChange( c.dir1 );
			Box box;
			if ( c.actType == type.Push ) {
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
	

	/**
	 * possible states/nodes to reach from current node.
	 * 
	 * @param agentID
	 * @return list of reachable states
	 */
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

	public void excecuteCommand(Command c, int agentID){
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
	
	
	public Node ChildNode() {
		Node child =CopyNode();
		child.parent=this;
		child.g+=1;
		
		return child;
	}

	
	public Node CopyNode() {
		Node copy= new Node();
		for (int i = 0; i < agents.length; i++) {
			if(this.agents[i]!=null){
				copy.agents[i]=new LogicalAgent(this.agents[i]);
			}
		}

		for (Box box : this.boxesByPoint.values()) {
			copy.addBox(new Box(box));
		}

		copy.g=this.g;
		copy.parent=this.parent;

		return copy;
	}
	
	public static Level getLevel() {
		return level;
	}
	
	@Override
	public String toString(){
		Character[][] map=level.toArray();
		for (int i = 0; i < agents.length; i++) {
			if(agents[i]!=null){
				map[agents[i].row][agents[i].col]= (char) ((int)'0'+agents[i].id);
			}
		}
		
		for (Box box : getBoxes()) {
			map[box.row][box.col]=Character.toUpperCase(box.getType());
		}

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
	
	
	
	public String toStringDistance(int row, int col){
		Character[][] map=level.toArray();


		StringBuilder s = new StringBuilder();
		s.append("\n");
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if(map[i][j] != '+'){
					if(distance(row, col, i, j)==null){
						s.append("NaN");
					}else{
					s.append(distance(row, col, i, j));
					}
				}else{
						s.append(map[i][j]);
				}
			}
			s.append("\n");
		}
		return s.toString();
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.boxesByPoint.hashCode();
//		result = prime * result + Arrays.deepHashCode(getBoxes());
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

		if( !this.boxesByPoint.equals(other.boxesByPoint) )
			return false;

		return true;
	}

}