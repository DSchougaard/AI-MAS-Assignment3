package client;

import java.util.Random;

public class Agent{
	
	
	private static Random rand = new Random();

		// We don't actually use these for Randomly Walking Around
		public int id;
		public String color;

		Agent( int id, String color ) {
			this.id = id;
			this.color = color;
		}

		public String act() {
			return Command.every[rand.nextInt( Command.every.length )].toString();
		}
	
}