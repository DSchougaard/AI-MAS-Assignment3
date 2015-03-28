package Test;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

import client.Command;
import client.Command.dir;
import client.Command.type;
import client.Heuristic.AStar;
import client.Strategy;
import client.Strategy.StrategyBestFirst;
import client.SearchClient;
import client.node.Color;
import client.node.Node;
import client.node.storage.Box;

public class test {

	@Test
	public void addBoxes(){
		Node n = new Node();
		assertEquals(0,n.getAllBoxes().size());
		assertEquals(0,n.getBoxes().length);
		n.addBox('k', Color.blue, 2, 4);
		assertEquals(1,n.getAllBoxes().size());
		assertEquals(1,n.getBoxes().length);
		assertEquals(Color.blue,n.getBoxes()[0].color);
		assertEquals('k',n.getBoxes()[0].getType());
		
		n.addBox('g', Color.blue, 2, 6);
		assertEquals(2,n.getAllBoxes().size());
		assertEquals(2,n.getBoxes().length);
		n.addBox('k', Color.red, 2, 8);
		assertEquals(3,n.getBoxes().length);
		assertEquals(2,n.getAllBoxes().size());
		ArrayList<Box>bs=n.getAllBoxes().get('k');
		assertEquals(2, bs.size());
		bs=n.getAllBoxes().get('g');
		assertEquals(1, bs.size());
	}
	
	@Test
	public void distance() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
	
		SearchClient client = new SearchClient( serverMessages );
		
		assertEquals(1, client.state.distance(1, 2, 1, 3));
		assertEquals(2, client.state.distance(1, 1, 1, 3));
		
		assertEquals(1, client.state.distance(client.state.agents[0],client.state.getBoxes()[0]));
	}
	
	@Test
	public void goals() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		assertEquals(1, client.state.getAllGoals().size());
		assertNull( client.state.getGoalsByColor(Color.cyan));
		assertEquals(1, client.state.getGoalsByColor(Color.noColor).size());
	}

	
	
	@Test
 	public void levelSetup()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		assertEquals(1, client.agents.size());
		
		assertEquals("\n+++++\n"
				+ "+0Aa+\n"
				+ "+++++\n"
				+ "   + \n", client.state.toString());
		
		assertNotNull(client.state.agentAt(1, 1));
		assertNull(client.state.agentAt(1, 2));
		assertNull(client.state.agentAt(1, 3));
		
		assertNotNull(client.state.boxAt(1, 2));
		assertNull(client.state.boxAt(1, 1));
		assertNull(client.state.boxAt(1, 3));

		assertFalse(client.state.isGoalState());
	}
	@Test
 	public void levelSetup2()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/SACrunch.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		assertEquals(1, client.agents.size());
		

		
		assertNotNull(client.state.agentAt(1, 1));
		assertNull(client.state.agentAt(1, 2));
		assertNull(client.state.agentAt(1, 3));
		

		assertNull(client.state.boxAt(1, 1));
		assertNull(client.state.boxAt(1, 3));

		assertFalse(client.state.isGoalState());
		assertFalse(client.state.isGoalState(Color.noColor));
		
		assertEquals(4, client.state.getGoalsByColor(Color.noColor).size());

	}
	@Test
	public void singleExpand()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		
		
		SearchClient agentClient = new SearchClient( client.state, client.agents.get(0) );

		assertEquals(client.state, agentClient.state);
		

		ArrayList<Node>expanded =agentClient.state.getExpandedNodes(0);
	
		assertEquals(1, expanded.size());
		assertEquals(new Command(type.Push, dir.E, dir.E), expanded.get(0).action);
		
		
		
		
		
		assertEquals("\n+++++\n"
				+ "+0Aa+\n"
				+ "+++++\n"
				+ "   + \n", client.state.toString());
		
		assertNotNull(expanded.get(0).agentAt(1, 2));
		assertNull(expanded.get(0).agentAt(1, 1));
		assertNull(expanded.get(0).agentAt(1, 3));
		
		assertNotNull(expanded.get(0).boxAt(1, 3));
		assertNull(expanded.get(0).boxAt(1, 1));
		assertNull(expanded.get(0).boxAt(1, 2));

		assertTrue(expanded.get(0).isGoalState());
		
		
		
		assertNotNull(client.state.agentAt(1, 1));
		assertNull(client.state.agentAt(1, 2));
		assertNull(client.state.agentAt(1, 3));
		
		assertNotNull(client.state.boxAt(1, 2));
		assertNull(client.state.boxAt(1, 1));
		assertNull(client.state.boxAt(1, 3));

		assertFalse(client.state.isGoalState());

		
		
		ArrayList<Command>cmds=new ArrayList<>();
		cmds.add(new Command(type.Push, dir.E, dir.E));
		Node excuted=client.state.excecuteCommands(cmds);
		
		
		assertNotNull(excuted.agentAt(1, 2));
		assertNull(excuted.agentAt(1, 1));
		assertNull(excuted.agentAt(1, 3));
		
		assertNotNull(excuted.boxAt(1, 3));
		assertNull(excuted.boxAt(1, 1));
		assertNull(excuted.boxAt(1, 2));

		assertTrue(excuted.isGoalState());
		
		
		
		
		
		assertNotNull(client.state.agentAt(1, 1));
		assertNull(client.state.agentAt(1, 2));
		assertNull(client.state.agentAt(1, 3));
		
		assertNotNull(client.state.boxAt(1, 2));
		assertNull(client.state.boxAt(1, 1));
		assertNull(client.state.boxAt(1, 3));

		assertFalse(client.state.isGoalState());

		
	}
	
	@Test
	public void doubleExpand() throws Exception {

		
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );

		SearchClient client = new SearchClient( serverMessages );


		SearchClient agentClient = new SearchClient( client.state, client.state.agents[0] );

		

		ArrayList<Node>expanded =agentClient.state.getExpandedNodes(0);
	
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
				assertNotEquals(client.state.hashCode(), node.hashCode());
				
			}else if(node.action.actType==type.Pull){

				assertEquals(client.state, node);
				assertNotNull(node.parent.boxAt(1, 3));
				assertNotNull(node.boxAt(1, 2));
				assertNotNull(node.agentAt(1, 1));
				assertNotNull(node.cellIsFree(1, 3));
				assertEquals("Pull(W,E)", node.action.toString());
				assertEquals(client.state.hashCode(), node.hashCode());
			}else{
				fail();
			}
		}

		
		assertEquals(client.state.hashCode(), agentClient.state.hashCode());
	
	}

	@Test
 	public void multiLevelSetup()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		assertEquals(2, client.agents.size());
		
		assertEquals("\n++++++++++++++++++++++++++++\n"
				+ "+             0        Aa  +\n"
				+ "+     1                Bb  +\n"
				+ "+ +++++++++++++++++        +\n"
				+ "+                          +\n"
				+ "++++++++++++++++++++++++++++\n", client.state.toString());
		
		assertNotNull(client.state.agentAt(2, 6));
		assertNotNull(client.state.agentAt(1, 14));
		assertNull(client.state.agentAt(1, 23));
		assertNull(client.state.agentAt(2, 23));
		
		assertNotNull(client.state.boxAt(1, 23));
		assertNotNull(client.state.boxAt(2, 23));
		assertNull(client.state.boxAt(1, 1));
		assertNull(client.state.boxAt(1, 3));

		assertFalse(client.state.isGoalState());
		
		assertEquals(1, client.state.getGoalsByColor(Color.red).size());
		assertEquals(Color.red, client.state.boxAt(1, 23).color);
		
		assertEquals(1, client.state.getGoalsByColor(Color.green).size());
		assertEquals(Color.green, client.state.boxAt(2, 23).color);
	}
	
	@Test
	public void MAsingleExpand()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		
		
		SearchClient agentClient = new SearchClient( client.state, client.agents.get(0) );

		assertEquals(client.state, agentClient.state);
		assertEquals(Color.red, agentClient.state.boxAt(1, 2).color);

		ArrayList<Node>expanded =agentClient.state.getExpandedNodes(0);
	
		assertEquals(1, expanded.size());
		assertEquals(new Command(type.Push, dir.E, dir.E), expanded.get(0).action);
		
		
		
		
		
		assertEquals("\n+++++\n"
				+ "+0Aa+\n"
				+ "+1Bb+\n"
				+ "+++++\n"
				, client.state.toString());
		
		assertNotNull(expanded.get(0).agentAt(1, 2));
		assertNull(expanded.get(0).agentAt(1, 1));
		assertNull(expanded.get(0).agentAt(1, 3));
		
		assertNotNull(expanded.get(0).boxAt(1, 3));
		assertNull(expanded.get(0).boxAt(1, 1));
		assertNull(expanded.get(0).boxAt(1, 2));

		assertTrue(expanded.get(0).isGoalState(Color.red));
		
		
		
		assertNotNull(client.state.agentAt(1, 1));
		assertNull(client.state.agentAt(1, 2));
		assertNull(client.state.agentAt(1, 3));
		
		assertNotNull(client.state.boxAt(1, 2));
		assertNull(client.state.boxAt(1, 1));
		assertNull(client.state.boxAt(1, 3));

		assertFalse(client.state.isGoalState(Color.red));

		
		
		ArrayList<Command>cmds=new ArrayList<>();
		cmds.add(new Command(type.Push, dir.E, dir.E));
		Node excuted=client.state.excecuteCommands(cmds);
		
		
		assertNotNull(excuted.agentAt(1, 2));
		assertNull(excuted.agentAt(1, 1));
		assertNull(excuted.agentAt(1, 3));
		
		assertNotNull(excuted.boxAt(1, 3));
		assertNull(excuted.boxAt(1, 1));
		assertNull(excuted.boxAt(1, 2));

		assertTrue(excuted.isGoalState(Color.red));
		
		
		
		
		
		assertNotNull(client.state.agentAt(1, 1));
		assertNull(client.state.agentAt(1, 2));
		assertNull(client.state.agentAt(1, 3));
		
		assertNotNull(client.state.boxAt(1, 2));
		assertNull(client.state.boxAt(1, 1));
		assertNull(client.state.boxAt(1, 3));

		assertFalse(client.state.isGoalState());

		
	}
	

	@Test
	public void MAsingleExpand2()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		
		
		SearchClient agentClient = new SearchClient( client.state, client.agents.get(0) );

		Strategy strategy1 = new StrategyBestFirst( new AStar( agentClient.state, client.agents.get(0).id ) );
		assertTrue( strategy1.frontierIsEmpty());
		strategy1.addToFrontier(agentClient.state);
		assertEquals(client.state, agentClient.state);

		ArrayList<Node>expanded =agentClient.state.getExpandedNodes(0);
		assertEquals(3, expanded.size());
		for (Node node : expanded) {
//			System.err.println(node);
//			System.err.println(node.hashCode());
			if(!strategy1.inFrontier(node)){
//				System.err.println("damm");
				strategy1.addToFrontier(node);
			}
		}
		assertNotEquals(agentClient.state.toString(), expanded.get(0).toString());
		assertNotEquals(agentClient.state, expanded.get(0));
		assertNotEquals(expanded.get(0), expanded.get(1));
		assertEquals(4, strategy1.countFrontier());

		ArrayList<Command>cmds=new ArrayList<>();
		expanded.forEach(n->cmds.add(n.action));
		assertTrue(cmds.contains(new Command(dir.E)));
		assertTrue(cmds.contains(new Command(dir.W)));
		assertTrue(cmds.contains(new Command(dir.S)));
		
		
		
		expanded =expanded.get(0).getExpandedNodes(0);
		
		assertEquals(3, expanded.size());
		cmds.clear();
		expanded.forEach(n->cmds.add(n.action));
		assertTrue(cmds.contains(new Command(dir.E)));
		assertTrue(cmds.contains(new Command(dir.W)));
		assertTrue(cmds.contains(new Command(dir.S)));
		
		
		
		

		
		
	}
	@Test
	public void multiAgent() throws Exception {
		
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );

		SearchClient client = new SearchClient( serverMessages );


		
		
		
		SearchClient agentClient = new SearchClient( client.state, client.state.agents[0] );
		Strategy strategy1 = new StrategyBestFirst( new AStar( agentClient.state, client.state.agents[0].id ) );
		LinkedList<Node> sol1=agentClient.Search(strategy1);
		assertEquals(9, sol1.size());
//		System.err.println(client.state.agents[0].color);
		SearchClient agentClient2 = new SearchClient( client.state, client.state.agents[1] );
		Strategy strategy2 = new StrategyBestFirst( new AStar( agentClient.state, client.state.agents[1].id ) );
		LinkedList<Node> sol2=agentClient2.Search(strategy2);
		assertEquals(17, sol2.size());


	}
	
	
	@Test
	public void multiGoals() throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/MAsimple1.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		assertEquals(2, client.state.getAllGoals().size());
		assertNull( client.state.getGoalsByColor(Color.cyan));
		assertEquals(1, client.state.getGoalsByColor(Color.green).size());
		assertEquals(1, client.state.getGoalsByColor(Color.red).size());
		ArrayList<Command> cms= new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			cms.add(new Command(dir.E));
			
		}
		cms.add(new Command(type.Push,dir.E,dir.E));
		Node agent0=client.state.excecuteCommands(cms, 1);
		assertTrue(agent0.isGoalState(Color.green));
		assertTrue(!agent0.isGoalState(Color.red));
		
		ArrayList<Command> cms2= new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			cms2.add(new Command(dir.E));
			
		}
		cms2.add(new Command(type.Push,dir.E,dir.E));
		Node agent1=client.state.excecuteCommands(cms2, 0);
		assertTrue(!agent1.isGoalState(Color.green));
		assertTrue(agent1.isGoalState(Color.red));

		
		Node total=agent0.excecuteCommands(cms2,0);
		
		assertTrue(total.isGoalState(Color.green));
		assertTrue(total.isGoalState(Color.red));
		assertTrue(total.isGoalState());
	}
}
