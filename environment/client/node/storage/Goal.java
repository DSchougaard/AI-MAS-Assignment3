package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type

public class Goal extends Base{
	private final char type;
	public int importance =0;
	public static int maxImportance=0; 
	
	
	public Goal(char type, int row, int col){
		super(row, col);
		this.type = type;
	}

	public char getType(){
		return type;
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		
		if(!super.equals( obj )){
			return false;
		}

		Goal b = (Goal)obj;
		return ( this.type == b.type );
	}
	
	@Override
	public String toString(){
		return type+" "+super.toString();
	}
	
	
}
