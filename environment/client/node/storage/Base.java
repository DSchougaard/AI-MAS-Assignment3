package client.node.storage; 

public class Base{
	public int row, col;

	public Base(int row, int col){
		this.row = row;
		this.col = col;
	}

	public boolean at(int row, int col){
		return ( this.row == row && this.col == col );
	}
}