package client;

import java.util.Random;

public class Agent{
	
	
	private static Random rand = new Random();

		// We don't actually use these for Randomly Walking Around
		private char id;
		private String color;

		Agent( char id, String color ) {
			this.id = id;
			this.color = color;
		}

		public String act() {
			return Command.every[rand.nextInt( Command.every.length )].toString();
		}
	
}