package client.node.storage;
import java.util.ArrayList;
import java.util.LinkedList;

// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Agent extends Base{
	public int id;
	public Color color;
	public enum Status{STUCK, PLAN, DONE, IDLE, HELPING}
	public Status status = Status.IDLE;
	public ArrayList<Goal> subgoals = new ArrayList<>();
	public LinkedList<Goal> subgoalsList = new LinkedList<>(); 

	
	public Agent(int name, Color color, int row, int col){
		super(row, col);
		this.id 	= name;
		if(color==null){
			this.color = Color.noColor;
		}else{
			this.color = color;
		}
	}

	public Agent(char name, int row, int col){
		super(row, col);
		this.id 	= (int)name;
		this.color 	= Color.noColor;
	}

	
	public Agent(Agent agent) {
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

		Agent b = (Agent)obj;
		return ( this.id == b.id && this.color == b.color );
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + super.hashCode();
		result = prime * result + this.id;
		return result;
	}
}
