package client.node.storage; 

import java.awt.Point;

public class Base{
	public int row, col;

	public Base(int row, int col){
		this.row = row;
		this.col = col;
	}

	public boolean isAt(int row, int col){
		return ( this.row == row && this.col == col );
	}

	public Point getPoint(){
		return new Point(this.row, this.col);
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		Base b = (Base)obj;
		return ( this.row == b.row && this.col == b.col );
	}

	@Override
	public int hashCode() {
		final int prime = 41;
		int result = 7;
		result = prime * result + this.row;
		result = prime * result + this.col;
		return result;
	}
	
	@Override
	public String toString(){
		return "("+row+","+col+")";
	}
}