package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Box extends Base{
	private char type;
	public Color color;

	public Box(char t, Color color, int row, int col){ 
		super(row, col);
		this.setType(t);
		if(color==null){
			this.color = Color.noColor;
		}else{
			this.color = color;
		}
		
	}

	public Box(Box box) {
		super(box.row, box.col);
		this.color=box.color;
		this.setType(box.getType());
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		super.equals( obj );

		Box b = (Box)obj;
		return ( this.getType() == b.getType() && this.color == b.color );
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = Character.toLowerCase(type);
	}
}