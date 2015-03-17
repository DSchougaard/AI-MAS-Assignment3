package client.map;

import client.map.DistanceMap;
import client.map.Level.Cell;



public class ManhattanDistanceMap extends DistanceMap{

	private class KeyWrapper{
		private cell a, b;
		public KeyWrapper(Cell a, Cell b){
			this.a = a;
			this.b = b;
		}
	}

	public ManhattanDistanceMap(Level map){
		for( int row = 0 ; row < map.getRow() ; row++ ){
			for( int col = 0 ; col < map.getCol() ; col++ ){
				
			}
		}
	}

}