package client;

public class Settings{

	public static class Global{
		public static final boolean PRINT = false;
		public static boolean EXPANDED_DEBUG = false;
	}


	public static class SearchClient{
		public static final int HISTORY_LENGTH = 10;
		public static final int CYCLE_THRESHOLD = 4;
	}

	public static class Conflict{
		public static final int REMAINING_AGENT_PLAN_THRESHOLD = 5;
	
		public static final int NO_OP_RESOLVE_AGENT_CONFLICT = 1;
		public static final int NO_OP_CLEAR_ROUTE = 2;
	}


	public static class ArgumentParser{
		public static final String DISTANCEMAP_CLASS_PREFIX = "client.node.level.distancemap.";
	}
	
	public static class SearchAgent{
		public static int searchMaxOffset = 2;
		public static int searchStartOffset = 20;
	}

}