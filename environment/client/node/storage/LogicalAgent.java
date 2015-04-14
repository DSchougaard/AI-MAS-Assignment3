package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class LogicalAgent extends Base{
	public int id;
	public Color color;

	
	public LogicalAgent(int name, Color color, int row, int col){
		super(row, col);
		this.id 	= name;
		if(color==null){
			this.color = Color.blue;
		}else{
			this.color = color;
		}
	}

	public LogicalAgent(char name, int row, int col){
		super(row, col);
		this.id 	= (int)name;
		this.color 	= Color.noColor;
	}

	
	public LogicalAgent(LogicalAgent agent) {
		super(agent.row, agent.col);
		this.id=agent.id;
		this.color=agent.color;
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() ){
			return false;
		}
			
		if(!super.equals( obj )){
			return false;
		}

		LogicalAgent b = (LogicalAgent)obj;
		return ( this.id == b.id && this.color == b.color );
	}
	
	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 5;
		result = prime * result + super.hashCode();
		result = prime * result + this.id;
		return result;
	}
}
