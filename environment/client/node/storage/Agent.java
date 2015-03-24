package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Agent extends Base{
	public int name;
	public Color color;

	public Agent(char name, Color color, int row, int col){
		super(row, col);
		this.name 	= (int)name;
		this.color 	= color;
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		super.equals( obj );

		Agent b = (Agent)obj;
		return ( this.name == b.name && this.color == b.color );
	}
}
