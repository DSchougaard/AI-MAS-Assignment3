package client.map;

import client.map.Node;

class Parser{

	private BufferedReader in;


	public Parser(BufferedReader in){
		this.in = in;
	}

	public parse(){
		Map< Character, String > colors = new HashMap< Character, String >();
		String line, color;
		ArrayList<String> tempMapContainer;
		/*
		Skeleton code borrowed from example
		*/

		// Read lines specifying colors
		while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
			line = line.replaceAll( "\\s", "" );
			color = line.split( ":" )[0];

			for ( String id : line.split( ":" )[1].split( "," ) )
				colors.put( id.charAt( 0 ), color );
		}

		// Read lines specifying level layout
		int maxCol = 0, maxRow = 0;
		while ( !line.equals( "" ) ) {
			if( line.length() > maxCol ){
				maxCol = line.length();
			}
			tempMapContainer.add(line);
		}
		maxRow = tempMapContainer.size();

		// Create the data structures for the bookkeeping
		Map m = new Map(maxCol, maxRow);


		// Parse the preloaded array
		for( int row = 0 ; row < maxRow ; row++ ){

			int n 		= maxCol - tempMapContainer.get(row).length;
			// Space padd the strings, if they're not of max length
			//String line = String.format(tempMapContainer.get(row) + "$-" + n + "s");
			String line = String.format("%-" + n + "s", tempMapContainer.get(row)); 

			for( int col = 0 ; col < line.length() ; col++ ){

				if( line.charAt( col ) == ' ' ){
					// Space
				}else if(line.charAt( col ) == '+' ){
					// Wall
				}else if( line.charAt( col ) >= 'a' && line.charAt( col ) <= 'z' ){
					// Goal
				}else if( line.charAt( col ) >= 'A' && line.charAt( col ) <= 'Z' ){
					// Box
				}else if( line.charAt( col ) >= '0' && line.charAt( col ) <= '9' ){
					// Agent
				}


			}
		}




		// Read lines specifying level layout
		while ( !line.equals( "" ) ) {
			for ( int i = 0; i < line.length(); i++ ) {
				char id = line.charAt( i );
				if ( '0' <= id && id <= '9' )
					agents.add( new Agent( id, colors.get( id ) ) );
			}

			line = in.readLine();

		}


	}
}