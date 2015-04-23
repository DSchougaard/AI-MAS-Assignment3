package client.node.level;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import client.node.Color;
import client.node.level.clustering.KClusteringGoals;
import client.node.level.distancemap.DistanceMap;
import client.node.storage.Base;
import client.node.storage.Goal;
import client.node.storage.LogicalAgent;

/*
	@author: Daniel Schougaard
*/

public class Level implements LevelInterface{
	/*
		Private variables for Level
	*/
	private int maxRow;
	private int maxCol;
	private static DistanceMap dm;

	
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


	// Acessing Goals.
	private HashMap<Character, ArrayList<Goal> > goalsByType;
	private HashMap<Color, ArrayList<Goal>> goalsByColor;
	
	private ArrayList<Goal> goals;

	// Clusters
	private HashMap<Integer, ArrayList<Goal>> clusters;

	// Constructor
	public Level(int maxRow, int maxCol, DistanceMap dm){
		this.maxCol 	= maxCol;
		this.maxRow 	= maxRow;
		map 			= new Cell[maxRow][maxCol];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				map[i][j] = new Cell(Type.SPACE);
			}
		}
		this.goalsByType 		= new HashMap<Character, ArrayList<Goal>>();
		this.goalsByColor 		= new HashMap<Color, ArrayList<Goal>>();
		Level.dm 				= dm;
		this.clusters 			= new HashMap<Integer, ArrayList<Goal>>();
		this.goals= new ArrayList<>();
	}	


	// Setup methods for the Level
	public void addWall(int row, int col){
		Level.map[row][col] = new Cell(Type.WALL);
	}

	public void addGoal(int row, int col, char letter, Color color){
		letter=Character.toLowerCase(letter);
		Level.map[row][col] = new Cell(Type.GOAL, letter);

		if( !goalsByType.containsKey(new Character(letter)) ){
			goalsByType.put( letter, new ArrayList<Goal>() );
		}
		if(color==null){
			color=Color.blue;
		}
		Goal goal=new Goal(letter, row, col);
		ArrayList<Goal> tempGoals = goalsByType.get( new Character(letter) );
		
		tempGoals.add(goal);
		
		addColor(goal, color);
		goals.add(goal);
	}

	public void addSpace(int row, int col){
		Level.map[row][col]  = new Cell(Type.SPACE);
	}

	public void addColor(Goal goal, Color color){
		ArrayList<Goal> chrs= goalsByColor.get(color);
		if(chrs==null){
			chrs=new ArrayList<>();
			goalsByColor.put(color, chrs);
		}
		chrs.add(goal);
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


	public char isGoal(int row, int col){
		if( Level.map[row][col] .type == Type.GOAL )
			return Level.map[row][col].letter;

		return '\0';
	}

	public boolean isWall(int row, int col){
		return ( Level.map[row][col].type == Type.WALL );
	}


	public ArrayList<Goal> getGoals(char chr){
		return this.goalsByType.get(new Character(chr));
	}


	public HashMap<Character, ArrayList<Goal>> getGoalMap(){
		return this.goalsByType;
	}

	public ArrayList<Goal> getGoals(){
		
		return goals;
	}

	public ArrayList<Goal> getGoals(Color color){
		return this.goalsByColor.get(color);
	}

	public Integer distance(int rowFrom, int colFrom, int rowTo, int colTo){
		if(dm==null){
			System.err.println("DistanceMap: "+dm);
			return 0;
		}
		
		return Level.dm.distance(rowFrom, colFrom, rowTo, colTo);
	}

	@Override
	public Integer distance(Base from, Base to) {
		return distance(from.row, from.col, to.row, to.col);
	}

	public void calculateCluster(LogicalAgent[] agents, boolean kcluster){

		if(kcluster){
			KClusteringGoals kcg = new KClusteringGoals(agents, this);
			this.clusters=kcg.getClusters();
		}else{
			for( int i = 0 ; i < agents.length ; i++ ){
				if( agents[i] != null )
					this.clusters.put( agents[i].id, this.goalsByColor.get(agents[i].color) );
			}
		}

	}

	public HashMap<Integer, ArrayList<Goal>> getClusters(){
		return this.clusters;

	}

	public ArrayList<Goal> getCluster(int agentID){
		return clusters.get( agentID );
	}

	public Character[][] toArray(){
		Character[][] result= new Character[maxRow][maxCol];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				switch (map[i][j].type) {
				case WALL:
					result[i][j]='+';
					break;
				case SPACE:
					result[i][j]=' ';
					break;
				case GOAL:
					result[i][j]=map[i][j].letter;
				break;
				default:
					result[i][j]='x';
					break;
				}
			}
		}
		return result;
	}
	
	public int[][] analyse(){
		int importance[][] = new int[maxRow][maxCol];
		
		HashSet<Base> explored= new HashSet<>();
		
		ArrayList<Base> deadends= new ArrayList<>();
		ArrayList<Base> cornores= new ArrayList<>();
		ArrayList<Base> onewall= new ArrayList<>();
		ArrayList<Base> nowalls= new ArrayList<>();
		ArrayList<Base> twowalls= new ArrayList<>();
		for (int i = 1; i < map.length-1; i++) {
			for (int j = 1; j < map[0].length-1; j++) {
				if(map[i][j].type==Level.Type.WALL){
					continue;
				}
				int wallsCount=0;
				if(map[i][j-1].type == Type.WALL){
					wallsCount++;
				}
				if(map[i][j+1].type == Type.WALL){
					wallsCount++;
				}
				
				if(map[i-1][j].type == Type.WALL){
					wallsCount++;
				}
				
				if(map[i+1][j].type == Type.WALL){
					wallsCount++;
				}
				switch (wallsCount) {
				case 0:
					nowalls.add(new Base(i,j));
					break;
				case 1:
					onewall.add(new Base(i,j));
					break;
				case 2:
					if((map[i][j-1].type == Type.WALL && map[i][j+1].type == Type.WALL) || (map[i-1][j].type == Type.WALL && map[i+1][j].type == Type.WALL) ){
						twowalls.add(new Base(i,j));
					}else{
						if((map[i-1][j-1].type == Type.WALL && map[i][j+1].type == Type.WALL && map[i+1][j].type == Type.WALL) 
								|| (map[i+1][j-1].type == Type.WALL && map[i][j+1].type == Type.WALL && map[i-1][j].type == Type.WALL) 
								|| (map[i-1][j+1].type == Type.WALL && map[i][j-1].type == Type.WALL && map[i+1][j].type == Type.WALL) 
								|| (map[i+1][j+1].type == Type.WALL && map[i][j-1].type == Type.WALL && map[i-1][j].type == Type.WALL)){
							twowalls.add(new Base(i,j));
						}else{
							cornores.add(new Base(i,j));
						}
					}
					break;
				case 3:
					deadends.add(new Base(i,j));
					break;
				default:
					while(true){
						System.err.println("unknown field type");
						System.out.println("unknown field type");
					}
				}

			}
		}
		int max=0;
		for (int i = 0; i < deadends.size(); i++) {
			Base base=deadends.get(i);
			int wallCount;
			int imp=1;
			do{
				wallCount=0;
				importance[base.row][base.col]=imp;
				explored.add(base);
				imp++;
				max=Math.max(imp, max);
				Base tmpBase = null;
				if(map[base.row][base.col-1].type == Type.WALL){
					wallCount++;
				}else{
					Base b = new Base(base.row, base.col-1);
					if(!explored.contains(b)){
						tmpBase=b;
					}
				}
				if(map[base.row][base.col+1].type == Type.WALL){
					wallCount++;
				}else{
					Base b = new Base(base.row, base.col+1);
					if(!explored.contains(b)){
						tmpBase=b;
					}
				}
				
				if(map[base.row-1][base.col].type == Type.WALL){
					wallCount++;
				}else{
					Base b = new Base(base.row-1, base.col);
					if(!explored.contains(b)){
						tmpBase=b;
					}
				}
				
				if(map[base.row+1][base.col].type == Type.WALL){
					wallCount++;
				}else{
					Base b = new Base(base.row+1, base.col);
					if(!explored.contains(b)){
						tmpBase=b;
					}
				}
				if(base.equals(new Base(2,4))){
					System.err.println("hej");
				}
				base=tmpBase;
			}while(base != null && wallCount>=2 && !explored.contains(base) );
		}
		

		
		for (Base base :cornores) {
			
			if(!explored.contains(base)){
				importance[base.row][base.col]=max;
			}
		}
		max++;
		for (Base base :onewall) {
			
			if(!explored.contains(base)){
				importance[base.row][base.col]=max;
			}
		}
		max++;
		
		for (Base base :nowalls) {
			
			if(!explored.contains(base)){
				importance[base.row][base.col]=max;
			}
		}
		max++;
		
		for (Base base :twowalls) {
			
			if(!explored.contains(base)){
				importance[base.row][base.col]=max;
			}
		}

		Goal.maxImportance=max;
		for(Goal goal: goals){
			goal.importance=importance[goal.row][goal.col];
		}

		
		
		return importance;
	}
	

}