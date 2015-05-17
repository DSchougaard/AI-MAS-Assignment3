package client;

public class Settings{

	public static class Global{
		public static final boolean PRINT = false;
	}


	public static class client{
		public static boolean EXPANDED_DEBUG = false;
		public static final int HISTORY_LENGTH = 10;
		public static final int CYCLE_THRESHOLD = 4;
	}

	public static class Conflict{
		public static final int REMAINING_AGENT_PLAN_THRESHOLD = 5;
	}


}