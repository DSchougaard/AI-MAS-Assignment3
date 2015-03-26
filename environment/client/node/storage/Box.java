package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Box extends Base{
	public char type;
	public Color color;

	public Box(char t, Color color, int row, int col){ 
		super(row, col);
		this.type = Character.toLowerCase(t);
		if(color==null){
			this.color = Color.noColor;
		}else{
			this.color = color;
		}
		
	}

	public Box(Box box) {
		super(box.row, box.col);
		this.color=box.color;
		this.type=box.type;
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		super.equals( obj );

		Box b = (Box)obj;
		return ( this.type == b.type && this.color == b.color );
	}
}