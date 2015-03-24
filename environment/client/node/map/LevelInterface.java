package client.node.map;

import java.awt.Point;
import java.util.*;

public interface LevelInterface {

	// Goals
	ArrayList<Point> getGoals(char chr);
	HashMap<Character, ArrayList<Point> > getAllGoals();
	boolean isWall(int row, int col);
}