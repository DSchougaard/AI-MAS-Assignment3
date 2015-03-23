package client.map;

import java.awt.Point;
import java.util.*;

interface LevelInterface {

	// Goals
	ArrayList<Point> getGoals(char chr);
	HashMap<Character, ArrayList<Point> > getAllGoals();
	boolean isWall(int row, int col);
}