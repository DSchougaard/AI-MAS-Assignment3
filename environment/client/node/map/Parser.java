package client.node.map;

import java.io.*;
import java.util.*;


import java.awt.Point;



import client.node.Node;
import client.node.Color;



public class Parser{


	public static Node parse(BufferedReader in) throws IOException{
		
		Map< Character, Color > colors = new HashMap< Character, Color >();
		String line, color;
		ArrayList<String> tempMapContainer = new ArrayList<String>();
		/*
		Skeleton code borrowed from example
		*/
		System.err.println("parsing");
		// Read lines specifying colors
		while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
			System.err.println(line);
			line = line.replaceAll( "\\s", "" );
			color = line.split( ":" )[0];

			for ( String id : line.split( ":" )[1].split( "," ) )
				colors.put( id.charAt( 0 ), Color.valueOf(color) );
		}

		// Read lines specifying level layout
		int maxCol = 0, maxRow = 0;
		while ( !line.equals( "" ) ) {
			if( line.length() > maxCol ){
				maxCol = line.length();
			}
			System.err.println(line);
			tempMapContainer.add(line);
			line=in.readLine();
		}
		maxRow = tempMapContainer.size();

		// Create the data structures for the bookkeeping
		Level level = new Level(maxRow, maxCol);
		Node node = new Node(level);


		// Parse the preloaded array
		for( int row = 0 ; row < maxRow ; row++ ){
//			int n 		= maxCol - tempMapContainer.get(row).length();
//			// Space padd the strings, if they're not of max length
//			//String line = String.format(tempMapContainer.get(row) + "$-" + n + "s");
//			line = String.format("%-" + n + "s", tempMapContainer.get(row)); 
			line= tempMapContainer.get(row);
			for( int col = 0 ; col < line.length() ; col++ ){
				if( line.charAt( col ) == ' ' ){
					// Space
					level.addSpace(row, col);
				}else if(line.charAt( col ) == '+' ){
					// Wall
					level.addWall(row, col);
				}else if( line.charAt( col ) >= 'a' && line.charAt( col ) <= 'z' ){
					// Goal
					level.addGoal(row, col, line.charAt(col) );
					level.addSpace(row, col);
				}else if( line.charAt( col ) >= 'A' && line.charAt( col ) <= 'Z' ){
					// Box
					node.addBox(line.charAt(col), colors.get(line.charAt(col)), row, col);
					level.addSpace(row, col);
				}else if( line.charAt( col ) >= '0' && line.charAt( col ) <= '9' ){
					// Agent
					node.addAgent(line.charAt(col), colors.get(line.charAt(col)), row, col);
					level.addSpace(row, col);
				}
			}
		}

		return node;
	}
}