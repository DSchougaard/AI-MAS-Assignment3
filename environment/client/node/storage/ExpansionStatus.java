package client.node.storage;

import client.SearchClient.Memory;
import client.Strategy;

public class ExpansionStatus {
	int explored;
	int frontiers;
	float timeSpentOnSearch;
	float maximunMemoryUse;
	
	public ExpansionStatus(){
		explored = 0;
		frontiers = 0;
		timeSpentOnSearch = 0;
		maximunMemoryUse = Memory.used();
	}
	
	public ExpansionStatus(Strategy strategy){
		explored = strategy.countExplored();
		frontiers = strategy.countFrontier();
		timeSpentOnSearch = strategy.timeSpent();
		maximunMemoryUse = Memory.used();
		
	}
	
	public void add(ExpansionStatus status){
		this.explored += status.explored;
		this.frontiers += status.frontiers;
		this.timeSpentOnSearch += status.timeSpentOnSearch;
		this.maximunMemoryUse = Math.max(this.maximunMemoryUse, status.maximunMemoryUse);
	}
	
	public String toString(){
		return String.format( "#Total Explored: %4d, #Frontier: %3d, Time: %3.2f s \t[Used: %.2f MB, Free: %.2f MB, Alloc: %.2f MB, MaxAlloc: %.2f MB]", explored, frontiers, timeSpentOnSearch, maximunMemoryUse, (Memory.total()-maximunMemoryUse), Memory.total(), Memory.max() );
	}
}