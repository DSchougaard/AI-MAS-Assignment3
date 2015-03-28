package client;

import java.util.LinkedList;

public class Command {
	static {
		LinkedList< Command > cmds = new LinkedList< Command >();
		for ( dir d1 : dir.values() ) {
			for ( dir d2 : dir.values() ) {
				if ( !Command.isOpposite( d1, d2 ) ) {
					cmds.add( new Command( type.Push, d1, d2 ) );
				}
			}
		}
		for ( dir d1 : dir.values() ) {
			for ( dir d2 : dir.values() ) {
				if ( d1 != d2 ) {
					cmds.add( new Command( type.Pull, d1, d2 ) );
				}
			}
		}
		for ( dir d : dir.values() ) {
			cmds.add( new Command( d ) );
		}


		every = cmds.toArray( new Command[0] );
	}

	public final static Command[] every;

	private static boolean isOpposite( dir d1, dir d2 ) {
		return d1.ordinal() + d2.ordinal() == 3;
	}

	// Order of enum important for determining opposites
	public static enum dir {
		N, W, E, S
	};
	
	public static enum type {
		Move, Push, Pull, NoOp
	};

	public final type actType;
	public final dir dir1;
	public final dir dir2;

	public Command( dir d ) {
		actType = type.Move;
		dir1 = d;
		dir2 = null;
	}
	
	public Command(  ) {
		actType = type.NoOp;
		dir1 = null;
		dir2 = null;
	}


	public Command( type t, dir d1, dir d2 ) {
		actType = t;
		dir1 = d1;
		dir2 = d2;
		
	}

	public String toString() {
		switch (actType) {
		case Move:
			return actType.toString() + "(" + dir1 + ")";
		case NoOp:
			return actType.toString();
		default:
			return actType.toString() + "(" + dir1 + "," + dir2 + ")";
		}
	
	}
	

	public String toActionString() {
		return "[" + this.toString() + "]";
	}
	
	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() ){
			return false;
		}
		
		return this.toString().equals(obj.toString());
		
	}

}
