package client.node;

import java.awt.Point;
import java.util.HashMap;
import java.util.ArrayList;

import client.node.storage.*;
import client.node.Color;


public class BoxStorage{
	// #1
	//private HashMap<Integer, Box> boxID;
	// #2
	private HashMap<Character, ArrayList<Box>> boxesByType;
	// #3
	private HashMap<Point, Box> boxesByPoint;
	
	public BoxStorage(){
		this.boxesByType = new HashMap<Character, ArrayList<Box>>();
		this.boxesByPoint = new HashMap<Point, Box>();
	}

	public void insertBox(Box box){
		// Add to #2
		this.boxesByPoint.put(new Point(box.row, box.col), box);
		// Add to #3
		if( !this.boxesByType.containsKey(box.getType()) )
			this.boxesByType.put(box.getType(), new ArrayList<Box>());

		ArrayList<Box> boxes = this.boxesByType.get(box.getType());
		boxes.add(box);
	}

	public void removeBox(Box box){
		Point p = new Point(box.row, box.col);
		char type = box.getType();
		this.boxesByPoint.remove(p);
		if( this.boxesByType.containsKey(type) )
			this.boxesByType.get(type).remove(box);
	}

	public void moveBox(Box box, int row, int col){
		if( box == null ){
			System.err.println("BoxStorage :: Somehow a NULL box was moved!");
			return;
		}
		Point p = new Point(box.row, box.col);
		this.boxesByPoint.remove(p);
		box.row = row;
		box.col = col;		
		this.boxesByPoint.put(p, box);
	}


	// Get'ers
	public Box getBox(int row, int col){
		Point p = new Point(row, col);
		Box r = this.boxesByPoint.get(p);
		if( r == null )
			System.err.println("BoxStorage :: Bookkeeping gone wrong. No box here!");
		return r;
	}

	public boolean isBoxAt(int row, int col){
		return this.boxesByPoint.containsKey(new Point(row, col));
	}

	public Box boxAt(int row, int col){
		return this.boxesByPoint.get(new Point(row, col));
	}

	public Box boxAt(Point p){
		return this.boxesByPoint.get(p);
	}

	public ArrayList<Box> getBoxes(char type){
		ArrayList<Box> list = this.boxesByType.get(type);
		if( list == null )
			list = new ArrayList<Box>();
		return list;
	}

	public ArrayList<Box> getBoxes(Color color){
		ArrayList<Box> results = new ArrayList<>();
		for( Box box : boxesByPoint.values() ){
			if( box.color == color )
				results.add(box);
		}
		return results;
	}

	public ArrayList<Box> getBoxes(){
		return new ArrayList<Box>(this.boxesByPoint.values());
	}


	@Override
	public boolean equals( Object obj ){
		BoxStorage other = (BoxStorage)obj;
		return this.getBoxes().equals(other.getBoxes());
	}

	@Override
	public int hashCode(){
		return this.boxesByPoint.hashCode();
	}
}