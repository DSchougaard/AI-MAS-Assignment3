package client.utils;

import java.util.LinkedList;
import java.util.Iterator;

public class FiniteQueue<E> extends LinkedList<E>{
	private int size;

	public FiniteQueue(int limit){
		this.size = size;
	}

	@Override
	public boolean add(E o){
		super.add(o);
		while(size() > size)
			super.remove();
		return true;
	}

	public int occurances(E o){
		int occurances = 0;
		for( Iterator<E> itr = super.iterator() ; itr.hasNext(); )  {
			E element = itr.next();

			if( o.equals(element) )
				occurances++;
		}
		System.err.println("FiniteQueue:: Occurances = " + occurances + ".");
		return occurances;
	}
}