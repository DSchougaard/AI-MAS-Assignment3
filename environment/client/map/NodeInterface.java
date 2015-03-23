import java.awt.Point;
import java.util.List;
import java.util.Map;


interface NodeInterface {
	
	// Goals
	List<Point> getGoals(char chr);
	Map<Character, List<Point> > getAllGoals();

	// Boxes
	List<Point> getBoxes(char color);
	Map<Character, List<Point>> getAllBoxes();
}