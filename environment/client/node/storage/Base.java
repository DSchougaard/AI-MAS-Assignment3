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

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		Base b = (Base)obj;
		return ( this.row == b.row && this.col == b.col );
	}


}