package sc;

import java.util.Comparator;
import java.util.HashMap;

public abstract class Heuristic implements Comparator< Node > {

	public Node initialState;
	
	public HashMap<Character, Point> goals =new HashMap<>();
	public HashMap<Node, Integer> hs =new HashMap<>();
	
	public Heuristic(Node initialState) {
		this.initialState = initialState;

		for (int i = 0; i < Node.MAX_COLUMN; i++) {
			for (int j = 0; j < Node.MAX_ROW; j++) {
				if(Character.isLetter(Node.goals[j][i])){
					
					goals.put(Node.goals[j][i], new Point(j,i));
				}
			
			}
		}
		
		
//		for (int i = 0; i < Node.MAX_COLUMN; i++) {
//			for (int j = 0; j < Node.MAX_ROW; j++) {
//				if(Character.isLetter(Node.goals[j][i])){
//					ArrayList<Point> tmp;
//					if(!goals.containsKey(Node.goals[j][i])){
//						tmp = new ArrayList<>();
//
//					}else{
//						tmp = goals.get(Node.goals[j][i]);
//	
//					}
//					tmp.add(new Point(j,i));
//					goals.put(Node.goals[j][i], tmp);
//				}
//			
//			}
//		}
	}

	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}

	public int h( Node n ) {
		//euclid distance from mover to box and from box to goal
		Integer tmpH=hs.get(n);
		if(tmpH==null){

			int h=0;
			for (int i = 0; i < Node.MAX_COLUMN; i++) {
				for (int j = 0; j < Node.MAX_ROW; j++) {
					if(Character.isLetter(n.boxes[j][i])){
						if(Character.toLowerCase(n.boxes[j][i])!=Node.goals[j][i]){
						

//							Point p=goals.get(Character.toLowerCase(n.boxes[j][i]));
//							
//							h=Math.abs(p.x-j)+Math.abs(p.y-i)+Math.abs(n.agentRow-j)+Math.abs(n.agentCol-i)-1;
//							if(n.agentRow==3 && n.agentCol==14)
//							System.err.println("position: "+n.agentRow+","+n.agentCol+" agent to box: "+(Math.abs(n.agentRow-j)+Math.abs(n.agentCol-i)-1)+"\nbox to goal: "+(Math.abs(p.x-j)+Math.abs(p.y-i))+"\n"+(n.g()));
							h+=goals.get(Character.toLowerCase(n.boxes[j][i])).dist(j, i)+Point.dist(j, i, n.agentRow, n.agentCol)-1;
							
						}
					}
				}
			}
			hs.put(n, h);
			return h;
		}else{
			return tmpH;
		}
		
	}

	public abstract int f( Node n );

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super( initialState );
		}

		public int f( Node n ) {
			return n.g() + h( n );
		}

		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState) {
			super( initialState );
			W = 5; // You're welcome to test this out with different values, but for the reporting part you must at least indicate benchmarks for W = 5
		}

		public int f( Node n ) {
			return n.g() + W * h( n );
		}

		public String toString() {
			return String.format( "WA*(%d) evaluation", W );
		}
	}

	public static class Greedy extends Heuristic {

		public Greedy(Node initialState) {
			super( initialState );
		}

		public int f( Node n ) {
			return h( n );
		}

		public String toString() {
			return "Greedy evaluation";
		}
	}
	

	
	public static class Point{
		
		public int y;
		public int x;

		public Point(int x, int y){
			this.x=x;
			this.y=y;
		}
		public static int dist(int x, int y, int x2, int y2){
		System.err.println(x+ " "+ y +" to "+x2+" " +y2+": "+((int) Math.sqrt(Math.pow(Math.abs(x-x2),2)+Math.pow(Math.abs(y-y2),2)))+" : "+(Math.abs(x-x2)+Math.abs(y-y2)));
			return (int) Math.sqrt(Math.pow(Math.abs(x-x2),2)+Math.pow(Math.abs(y-y2),2));
		}
		public int dist(int x, int y){
			return (int) Math.sqrt(Math.pow(Math.abs(this.x-x),2)+Math.pow(Math.abs(this.y-y),2));
		}
//		
//		public int dist(Point p){
//			return (int) Math.sqrt(Math.pow(Math.abs(this.x-p.x),2)+Math.pow(Math.abs(this.y-p.y),2));
//		}
		
//		public static int dist(int x, int y, int x2, int y2){
//			if(((int) Math.sqrt(Math.pow(Math.abs(x-x2),2)+Math.pow(Math.abs(y-y2),2)))!=(Math.abs(x-x2)+Math.abs(y-y2))){
//				System.err.println(x+ " "+ y +" to "+x2+" " +y2+": "+((int) Math.sqrt(Math.pow(Math.abs(x-x2),2)+Math.pow(Math.abs(y-y2),2)))+" : "+(Math.abs(x-x2)+Math.abs(y-y2)));
//			}
//				return Math.abs(x-x2)+Math.abs(y-y2);
//		}
//		public int dist(int x, int y){
//			System.err.println(x+ "-"+ y +" to "+this.x+" " +this.y+":-"+(Math.abs(this.x-x)+Math.abs(this.y-y)));
//			return Math.abs(this.x-x)+Math.abs(this.y-y);
//		}
//		
//		public int dist(Point p){
//			System.err.println("hej");
//			return Math.abs(this.x-p.x)+Math.abs(this.y-p.y);
//		}
	}
}
