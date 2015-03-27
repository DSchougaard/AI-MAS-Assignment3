package Test;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.junit.Test;

import client.Command;
import client.Command.dir;
import client.Command.type;
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
		BufferedReader serverMessages = null;
		try {
			serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SearchClient client = new SearchClient( serverMessages );
		
		assertEquals(1, client.state.distance(1, 2, 1, 3));
		assertEquals(2, client.state.distance(1, 1, 1, 3));
		
		assertEquals(1, client.state.distance(client.state.agents[0],client.state.getBoxes()[0]));
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
	public void singleExpand()throws Exception{
		BufferedReader serverMessages = new BufferedReader( new FileReader(new File("E:/GitHub/AI-MAS-Assignment3/environment/levels/simple.lvl")) );
		
		SearchClient client = new SearchClient( serverMessages );
		
		
		
		SearchClient agentClient = new SearchClient( client.state, client.agents.get(0) );

		assertEquals(client.state, agentClient.state);
		

		ArrayList<Node>expanded =agentClient.state.getExpandedNodes();
	
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

		

		ArrayList<Node>expanded =agentClient.state.getExpandedNodes();
	
		Node expanded1=expanded.get(0);
		
		expanded =expanded1.getExpandedNodes();
		
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

}
