package test;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import client.Command;
import client.Command.dir;
import client.Command.type;
import client.Heuristic.AStar;
import client.SearchAgent;
import client.SearchClient;
import client.parser.SettingsContainer;
import client.Strategy;
import client.Strategy.StrategyBestFirst;
import client.node.Color;
import client.node.Node;
import client.node.level.distancemap.FloydWarshallDistanceMap;
import client.node.storage.Box;

//I am a horrible person
@FixMethodOrder(MethodSorters.JVM)
public class test {

	@Before
	public void setup(){
		System.setErr(new PrintStream(new OutputStream() {
		    public void write(int b) {
		    }
		}));
	}
	
	@After
	public void tearDown(){
		SearchClient.state=null;
		SearchClient.agents=new ArrayList<>();
	}
	@Test
	public void addBoxes(){
		Node n = new Node();
		assertEquals(0,n.getBoxes().length);
		n.addBox('k', Color.blue, 2, 4);
		assertEquals(1,n.getBoxes().length);
		assertEquals(Color.blue,n.getBoxes()[0].color);
		assertEquals('k',n.getBoxes()[0].getType());
		
		n.addBox('g', Color.blue, 2, 6);
		assertEquals(2,n.getBoxes().length);
		n.addBox('k', Color.red, 2, 8);
		assertEquals(3,n.getBoxes().length);
		ArrayList<Box>bs=n.getBoxes('k');
		assertEquals(2, bs.size());
		bs=n.getBoxes('g');
		assertEquals(1, bs.size());
	}
	
	@Test
	public void distance() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
	
		SearchClient.init( serverMessages );
		
		assertEquals(1, SearchClient.state.distance(1, 2, 1, 3));
		assertEquals(2, SearchClient.state.distance(1, 1, 1, 3));
		
		assertEquals(1, SearchClient.state.distance(SearchClient.state.agents[0],SearchClient.state.getBoxes()[0]));
	}
	
	@Test
	public void goals() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		
		SearchClient.init( serverMessages );
		
		assertEquals(1, SearchClient.state.getGoals().size());
		assertNull( SearchClient.state.getGoals(Color.cyan));
		assertEquals(1, SearchClient.state.getGoals(Color.blue).size());
	}

	
	
	@Test
 	public void levelSetup()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		
		SearchClient.init( serverMessages );
		
		assertEquals(1, SearchClient.agents.size());
		
		assertEquals("\n+++++\n"
				+ "+0Aa+\n"
				+ "+++++\n"
				+ "   + \n", SearchClient.state.toString());
		
		assertNotNull(SearchClient.state.agentAt(1, 1));
		assertNull(SearchClient.state.agentAt(1, 2));
		assertNull(SearchClient.state.agentAt(1, 3));
		
		assertNotNull(SearchClient.state.boxAt(1, 2));
		assertNull(SearchClient.state.boxAt(1, 1));
		assertNull(SearchClient.state.boxAt(1, 3));

		assertFalse(SearchClient.state.isGoalState());
	}
	@Test
 	public void levelSetup2()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/SACrunch.lvl")) );
		
SearchClient.init( serverMessages );
		
		assertEquals(1, SearchClient.agents.size());
		

		
		assertNotNull(SearchClient.state.agentAt(1, 1));
		assertNull(SearchClient.state.agentAt(1, 2));
		assertNull(SearchClient.state.agentAt(1, 3));
		

		assertNull(SearchClient.state.boxAt(1, 1));
		assertNull(SearchClient.state.boxAt(1, 3));

		assertFalse(SearchClient.state.isGoalState());
		assertFalse(SearchClient.state.isGoalState(Color.blue));
		
		assertEquals(4, SearchClient.state.getGoals(Color.blue).size());

	}
	@Test
	public void singleExpand()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		
		SearchClient.init( serverMessages );
		
		SearchAgent agent =SearchClient.agents.get(0);
		
		agent.setState( SearchClient.state);

		assertEquals(SearchClient.state, agent.state);
		

		ArrayList<Node>expanded =agent.state.getExpandedNodes(0);
	
		assertEquals(1, expanded.size());
		assertEquals(new Command(type.Push, dir.E, dir.E), expanded.get(0).action);
		
		
		
		
		
		assertEquals("\n+++++\n"
				+ "+0Aa+\n"
				+ "+++++\n"
				+ "   + \n", SearchClient.state.toString());
		
		assertNotNull(expanded.get(0).agentAt(1, 2));
		assertNull(expanded.get(0).agentAt(1, 1));
		assertNull(expanded.get(0).agentAt(1, 3));
		
		assertNotNull(expanded.get(0).boxAt(1, 3));
		assertNull(expanded.get(0).boxAt(1, 1));
		assertNull(expanded.get(0).boxAt(1, 2));

		assertTrue(expanded.get(0).isGoalState());
		
		
		
		assertNotNull(SearchClient.state.agentAt(1, 1));
		assertNull(SearchClient.state.agentAt(1, 2));
		assertNull(SearchClient.state.agentAt(1, 3));
		
		assertNotNull(SearchClient.state.boxAt(1, 2));
		assertNull(SearchClient.state.boxAt(1, 1));
		assertNull(SearchClient.state.boxAt(1, 3));

		assertFalse(SearchClient.state.isGoalState());

		
		
		ArrayList<Command>cmds=new ArrayList<>();
		cmds.add(new Command(type.Push, dir.E, dir.E));
		Node excuted=SearchClient.state.excecuteCommands(cmds);
		
		
		assertNotNull(excuted.agentAt(1, 2));
		assertNull(excuted.agentAt(1, 1));
		assertNull(excuted.agentAt(1, 3));
		
		assertNotNull(excuted.boxAt(1, 3));
		assertNull(excuted.boxAt(1, 1));
		assertNull(excuted.boxAt(1, 2));

		assertTrue(excuted.isGoalState());
		
		
		
		
		
		assertNotNull(SearchClient.state.agentAt(1, 1));
		assertNull(SearchClient.state.agentAt(1, 2));
		assertNull(SearchClient.state.agentAt(1, 3));
		
		assertNotNull(SearchClient.state.boxAt(1, 2));
		assertNull(SearchClient.state.boxAt(1, 1));
		assertNull(SearchClient.state.boxAt(1, 3));

		assertFalse(SearchClient.state.isGoalState());

		
	}
	
	@Test
	public void doubleExpand() throws Exception {

		
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );

		SearchClient.init( serverMessages );
		

		ArrayList<Node>expanded =SearchClient.state.getExpandedNodes(0);
	
		Node expanded1=expanded.get(0);
		
		expanded =expanded1.getExpandedNodes(0);
		
		assertNotNull(expanded1.agentAt(1, 2));
		assertNull(expanded1.agentAt(1, 1));
		assertNull(expanded1.agentAt(1, 3));
		
		assertNotNull(expanded1.boxAt(1, 3));
		assertNull(expanded1.boxAt(1, 1));
		assertNull(expanded1.boxAt(1, 2));
		
		assertEquals(2, expanded.size());
		assertNotEquals(expanded.get(0).action, expanded.get(1).action);
		
		
		for (Node node : expanded) {
			assertNotNull( node.agentAt(1, 1));
			assertNull( node.agentAt(1, 2));
			assertNull( node.agentAt(1, 3));

			if(node.action.actType==type.Move){

				assertEquals("Move(W)", node.action.toString());
				assertNotNull(node.parent.parent.boxAt(1, 2));
				assertNotNull(node.parent.boxAt(1, 3));
				assertNotNull(node.boxAt(1, 3));
				assertNotEquals(SearchClient.state.hashCode(), node.hashCode());
				
			}else if(node.action.actType==type.Pull){

				assertEquals(SearchClient.state, node);
				assertNotNull(node.parent.boxAt(1, 3));
				assertNotNull(node.boxAt(1, 2));
				assertNotNull(node.agentAt(1, 1));
				assertNotNull(node.cellIsFree(1, 3));
				assertEquals("Pull(W,E)", node.action.toString());
				assertEquals(SearchClient.state.hashCode(), node.hashCode());
			}else{
				fail();
			}
		}

		
		assertEquals(SearchClient.state.hashCode(), SearchClient.state.hashCode());
	
	}

	@Test
 	public void multiLevelSetup()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );
		
		SearchClient.init( serverMessages );
		
		assertEquals(2, SearchClient.agents.size());
		
		assertEquals("\n++++++++++++++++++++++++++++\n"
				+ "+             0        Aa  +\n"
				+ "+     1                Bb  +\n"
				+ "+ +++++++++++++++++        +\n"
				+ "+                          +\n"
				+ "++++++++++++++++++++++++++++\n", SearchClient.state.toString());
		
		assertNotNull(SearchClient.state.agentAt(2, 6));
		assertNotNull(SearchClient.state.agentAt(1, 14));
		assertNull(SearchClient.state.agentAt(1, 23));
		assertNull(SearchClient.state.agentAt(2, 23));
		
		assertNotNull(SearchClient.state.boxAt(1, 23));
		assertNotNull(SearchClient.state.boxAt(2, 23));
		assertNull(SearchClient.state.boxAt(1, 1));
		assertNull(SearchClient.state.boxAt(1, 3));

		assertFalse(SearchClient.state.isGoalState());
		
		assertEquals(1, SearchClient.state.getGoals(Color.red).size());
		assertEquals(Color.red, SearchClient.state.boxAt(1, 23).color);
		
		assertEquals(1, SearchClient.state.getGoals(Color.green).size());
		assertEquals(Color.green, SearchClient.state.boxAt(2, 23).color);
	}
	
	@Test
	public void MAsingleExpand()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple.lvl")) );
		
		SearchClient.init( serverMessages );
		
		SearchAgent agent1 = SearchClient.agents.get(0);
		
		agent1.setState(SearchClient.state);

		assertEquals(SearchClient.state, agent1.state);
		assertEquals(Color.red, agent1.state.boxAt(1, 2).color);

		ArrayList<Node>expanded =agent1.state.getExpandedNodes(0);
	
		assertEquals(1, expanded.size());
		assertEquals(new Command(type.Push, dir.E, dir.E), expanded.get(0).action);
		
		
		
		
		
		assertEquals("\n+++++\n"
				+ "+0Aa+\n"
				+ "+1Bb+\n"
				+ "+++++\n"
				, SearchClient.state.toString());
		
		assertNotNull(expanded.get(0).agentAt(1, 2));
		assertNull(expanded.get(0).agentAt(1, 1));
		assertNull(expanded.get(0).agentAt(1, 3));
		
		assertNotNull(expanded.get(0).boxAt(1, 3));
		assertNull(expanded.get(0).boxAt(1, 1));
		assertNull(expanded.get(0).boxAt(1, 2));

		assertTrue(expanded.get(0).isGoalState(Color.red));
		
		
		
		assertNotNull(SearchClient.state.agentAt(1, 1));
		assertNull(SearchClient.state.agentAt(1, 2));
		assertNull(SearchClient.state.agentAt(1, 3));
		
		assertNotNull(SearchClient.state.boxAt(1, 2));
		assertNull(SearchClient.state.boxAt(1, 1));
		assertNull(SearchClient.state.boxAt(1, 3));

		assertFalse(SearchClient.state.isGoalState(Color.red));

		
		
		ArrayList<Command>cmds=new ArrayList<>();
		cmds.add(new Command(type.Push, dir.E, dir.E));
		Node excuted=SearchClient.state.excecuteCommands(cmds);
		
		
		assertNotNull(excuted.agentAt(1, 2));
		assertNull(excuted.agentAt(1, 1));
		assertNull(excuted.agentAt(1, 3));
		
		assertNotNull(excuted.boxAt(1, 3));
		assertNull(excuted.boxAt(1, 1));
		assertNull(excuted.boxAt(1, 2));

		assertTrue(excuted.isGoalState(Color.red));
		
		
		
		
		
		assertNotNull(SearchClient.state.agentAt(1, 1));
		assertNull(SearchClient.state.agentAt(1, 2));
		assertNull(SearchClient.state.agentAt(1, 3));
		
		assertNotNull(SearchClient.state.boxAt(1, 2));
		assertNull(SearchClient.state.boxAt(1, 1));
		assertNull(SearchClient.state.boxAt(1, 3));

		assertFalse(SearchClient.state.isGoalState());

		
	}
	

	@Test
	public void MAsingleExpand2()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );
		
		SearchClient.init( serverMessages );
		
		
		SearchAgent agent= SearchClient.agents.get(0);
		agent.setState( SearchClient.state );

		Strategy strategy1 = new StrategyBestFirst( new AStar(agent) );
		assertTrue( strategy1.frontierIsEmpty());
		strategy1.addToFrontier(agent.state);
		assertEquals(SearchClient.state, agent.state);

		ArrayList<Node>expanded =agent.state.getExpandedNodes(0);
		assertEquals(3, expanded.size());
		for (Node node : expanded) {
//			System.err.println(node);
//			System.err.println(node.hashCode());
			if(!strategy1.inFrontier(node)){
//				System.err.println("damm");
				strategy1.addToFrontier(node);
			}
		}
		assertNotEquals(SearchClient.state.toString(), expanded.get(0).toString());
		assertNotEquals(SearchClient.state, expanded.get(0));
		assertNotEquals(expanded.get(0), expanded.get(1));
		assertEquals(4, strategy1.countFrontier());

		ArrayList<Command>cmds=new ArrayList<>();
		expanded.forEach(n->cmds.add(n.action));
		assertTrue(cmds.contains(new Command(dir.E)));
		assertTrue(cmds.contains(new Command(dir.W)));
		assertTrue(cmds.contains(new Command(dir.S)));
		

		Node ex=Arrays.stream(expanded.toArray(new Node[0])).filter(n-> n.agents[0].row == 1).findAny().get();
		expanded =ex.getExpandedNodes(0);
		
		assertEquals(3, expanded.size());
		cmds.clear();
		expanded.forEach(n->cmds.add(n.action));
		assertTrue(cmds.contains(new Command(dir.E)));
		assertTrue(cmds.contains(new Command(dir.W)));
		assertTrue(cmds.toString(),cmds.contains(new Command(dir.S)));
		
		
		

		
		
	}
	@Test
	public void multiAgent() throws Exception {
		
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );

		SearchClient.init( serverMessages );


		
		
		SearchAgent agent1 = SearchClient.agents.get(0);
		agent1.setState( SearchClient.state );
		Strategy strategy1 = new StrategyBestFirst( new AStar(agent1 ) );
		LinkedList<Node> sol1=agent1.Search(strategy1).solution;
		assertEquals(9, sol1.size());
//		System.err.println(SearchClient.state.agents[0].color);
		SearchAgent agent2 = SearchClient.agents.get(1);
		agent2.setState(SearchClient.state);
		Strategy strategy2 = new StrategyBestFirst( new AStar(SearchClient.agents.get(1)) );
		LinkedList<Node> sol2=agent2.Search(strategy2).solution;
		assertEquals(17, sol2.size());


	}
	
	@Test
	public void multiAgent2() throws Exception {
		
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );

SearchClient.init( serverMessages );


		
		
		SearchAgent agent1 = SearchClient.agents.get(0);
		agent1.setState( SearchClient.state );
		Strategy strategy1 = new StrategyBestFirst( new AStar(agent1 ) );
		LinkedList<Node> sol1=agent1.Search(strategy1).solution;
		assertEquals(9, sol1.size());
//		System.err.println(SearchClient.state.agents[0].color);
		SearchAgent agent2 = SearchClient.agents.get(1);
		agent2.setState(SearchClient.state);
		Strategy strategy2 = new StrategyBestFirst( new AStar(agent2) );
		LinkedList<Node> sol2=agent2.Search(strategy2).solution;
		assertEquals(17, sol2.size());


	} 
	@Test
	public void multiGoals() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );
		
		SearchClient.init( serverMessages );
		
		assertEquals(2, SearchClient.state.getGoals().size());
		assertNull( SearchClient.state.getGoals(Color.cyan));
		assertEquals(1, SearchClient.state.getGoals(Color.green).size());
		assertEquals(1, SearchClient.state.getGoals(Color.red).size());
		ArrayList<Command> cms= new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			cms.add(new Command(dir.E));
			
		}
		cms.add(new Command(type.Push,dir.E,dir.E));
		Node agent0=SearchClient.state.excecuteCommands(cms, 1);
		assertTrue(agent0.isGoalState(Color.green));
		assertTrue(!agent0.isGoalState(Color.red));
		
		ArrayList<Command> cms2= new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			cms2.add(new Command(dir.E));
			
		}
		cms2.add(new Command(type.Push,dir.E,dir.E));
		Node agent1=SearchClient.state.excecuteCommands(cms2, 0);
		assertTrue(!agent1.isGoalState(Color.green));
		assertTrue(agent1.isGoalState(Color.red));

		
		Node total=agent0.excecuteCommands(cms2,0);
		
		assertTrue(total.isGoalState(Color.green));
		assertTrue(total.isGoalState(Color.red));
		assertTrue(total.isGoalState());
	}

	@Test
	public void DistanceMap() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/SACrunch.lvl")) );
		
		SettingsContainer con = new SettingsContainer();
		con.dm=new FloydWarshallDistanceMap();
		SearchClient.init( serverMessages, con );
		
		Node n=SearchClient.state;
		StringBuilder builder= new StringBuilder();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				
				if(n.cellIsFree(i, j) || n.objectAt(i, j) != null){

					builder.append(n.distance(2, 1, i, j));
//					System.out.print(n.distance(2, 1, i, j));
				}else{
					builder.append("x");
//					System.out.print("x");
				}
			}
//			System.out.println();
			builder.append("\n");
		}
		assertEquals("xxxxxxxx\n"+
				"x1x17161514x\n"+
				"x01x151413x\n"+
				"x1x5x1312x\n"+
				"x2345x11x\n"+
				"x3xxxx10x\n"+
				"x456789x\n"+
				"xxxxxxxx\n", builder.toString());
	}
	
	@Test
	public void hash() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );
		
		SearchClient.init( serverMessages );
		
		assertEquals(764559546, SearchClient.state.hashCode());
	}
	
	@Test
	public void Floyd() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/Test1.lvl")) );
		
		SearchClient.init( serverMessages );
		
		System.out.println(SearchClient.state);
		System.out.println(SearchClient.state.toStringDistance(1, 1));
		
	}
}