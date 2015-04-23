package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Box extends Base{
	private static int globalID = 0;

	private char type;
	public Color color;
	public final int id;

	public Box(char t, Color color, int row, int col){ 
		super(row, col);
		this.setType(t);
		this.id = globalID++;
		if(color==null){
			this.color = Color.blue;
		}else{
			this.color = color;
		}
	}

	public Box(Box box) {
		super(box.row, box.col);
		this.id = box.id;
		this.color=box.color;
		this.setType(box.getType());
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		
		if(!super.equals( obj )){
			return false;
		}

		Box b = (Box)obj;
		return ( this.id == b.id && this.getType() == b.getType() && this.color ==b.color );
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result + super.hashCode();
		result = prime * result + this.type;
		return result;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = Character.toLowerCase(type);
	}
}