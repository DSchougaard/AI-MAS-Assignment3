package client;
import java.util.ArrayList;


// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;
import client.node.storage.LogicalAgent;
import client.node.storage.Goal;

public class SearchAgent{
	public int id;
	public Color color;
	public enum Status{STUCK, PLAN, DONE, IDLE, HELPING}
	public Status status = Status.IDLE;
	public ArrayList<Goal> subgoals = new ArrayList<>();
	
	public SearchAgent(int name, Color color, int row, int col){
		this.id 	= name;
		if(color==null){
			this.color = Color.noColor;
		}else{
			this.color = color;
		}
	}

	public SearchAgent(char name, int row, int col){
		this.id 	= (int)name;
		this.color 	= Color.noColor;
	}

	
	public SearchAgent(SearchAgent agent) {
		this.id=agent.id;
		this.color=agent.color;
	}

	public SearchAgent(LogicalAgent agent) {
		this.id=agent.id;
		this.color=agent.color;
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() ){
			return false;
		}


		SearchAgent b = (SearchAgent)obj;
		return ( this.id == b.id && this.color == b.color );
	}
	
	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 5;
		result = prime * result + this.id;
		return result;
	}
}
