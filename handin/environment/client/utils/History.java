package client.utils;

import client.node.storage.Base;

public class History{
	private int limit;

			// Base[Agents][History]
	private Base[][] histories;
	private int[] pointers;

	public History(int numAgents, int limit){
		this.limit = limit;
		this.histories = new Base[numAgents][limit];
		this.pointers = new int[numAgents];
		for( int i = 0 ; i < numAgents ; i ++ )
			this.pointers[i] = 0;

		for( int i = 0 ; i < numAgents ; i++ ){
			for( int j = 0 ; j < limit ; j++ ){
				this.histories[i][j] = new Base(-1, -1);
			}
		}

	}


	public void add(int agentNo, Base b){
		pointers[agentNo] = (this.pointers[agentNo] + 1) % this.limit;
		this.histories[agentNo][pointers[agentNo]] = b;
	}

	public int occurances(int agentNo, Base b){
		int occurances = 0;
		for( int i = 0 ; i < this.limit ; i++ ){
			if( b.equals(this.histories[agentNo][i]) )
				occurances++;
		}
		return occurances;
	}
}