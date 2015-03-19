package client;

import java.util.Random;

import client.SearchClient.Color;

public class Agent{
	
	
	private static Random rand = new Random();

		// We don't actually use these for Randomly Walking Around
		public int id;
		public Color color;

		Agent(int id){
			this.id=id;
			this.color=Color.noColor;
		}
		
		Agent( int id, Color color ) {
			this.id = id;
			this.color = color;
		}

		public String act() {
			return Command.every[rand.nextInt( Command.every.length )].toString();
		}
	
}