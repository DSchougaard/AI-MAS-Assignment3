package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Agent extends Base{
	public int id;
	public Color color;
	public Boolean conflict =false;
	
	public Agent(char name, Color color, int row, int col){
		super(row, col);
		this.id 	= (int)name;
		this.color 	= color;
	}

	public Agent(char name, int row, int col){
		super(row, col);
		this.id 	= (int)name;
		this.color 	= Color.noColor;
	}

	
	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		super.equals( obj );

		Agent b = (Agent)obj;
		return ( this.id == b.id && this.color == b.color );
	}
}
