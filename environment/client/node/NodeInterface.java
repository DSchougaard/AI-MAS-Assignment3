package client.node;

import java.awt.Point;
import java.util.*;

import client.node.Node.Box;

public interface NodeInterface {
	

	// Boxes
	List<Box> getBoxes(char color);
	HashMap<Character, ArrayList<Box>> getAllBoxes();
}