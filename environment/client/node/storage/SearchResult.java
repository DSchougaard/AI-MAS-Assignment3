package client.node.storage;

import java.util.LinkedList;

import client.node.Node;

public class SearchResult{

	public enum Result{PLAN, STUCK, DONE, MEMMORY, TIME, IMPOSIBLE}
	public Result reason;
	public LinkedList<Node> solution;
	public ExpansionStatus expStatus;
	
	public SearchResult(){
		solution= new LinkedList<>();
		reason=Result.STUCK;
	}
	
	public SearchResult(Result result,LinkedList<Node> solution){
		this.solution= solution;
		this.reason=result;
		this.expStatus= new ExpansionStatus();
	}
	
	public SearchResult(Result result,LinkedList<Node> solution, ExpansionStatus expStatus){
		this.solution= solution;
		this.reason=result;
		this.expStatus=expStatus;
	}
}