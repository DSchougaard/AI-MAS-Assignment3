package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Box extends Base{
	public char type;
	public Color color;

	public Box(char t, Color color, int row, int col){ 
		super(row, col);
		this.type = t;
		this.color = color;
	}
}