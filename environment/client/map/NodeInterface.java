package client.map;

import java.awt.Point;
import java.util.List;
import java.util.Map;


interface NodeInterface {
	

	// Boxes
	List<Point> getBoxes(char color);
	Map<Character, List<Point>> getAllBoxes();
}