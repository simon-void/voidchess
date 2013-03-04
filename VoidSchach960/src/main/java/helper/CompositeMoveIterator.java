package helper;

import java.util.*;
/**
 * @author stephan
 */
public class CompositeMoveIterator implements MoveIterator
{
	private List moveIterators;
	private int index;
	private boolean iterationStarted;

	public CompositeMoveIterator()
	{
		moveIterators    = new ArrayList();
		index            = 0;
		iterationStarted = false;
	}

	public boolean hasMoreMoves()
	{
		iterationStarted = true;
		
		if( index>=moveIterators.size() ) return false;
		if( getMoveIterator( index ).hasMoreMoves() ) return true;
		index++;
		while( index<moveIterators.size() ) {
			if( getMoveIterator( index ).hasMoreMoves() ) {
				return true;
			}
			index++;
		}
		return false;
	}

	public Move nextMove()
	{
		iterationStarted = true;
		if( !hasMoreMoves() ) {
			throw new NoSuchElementException();
		}
		return getMoveIterator( index ).nextMove();
	}

	public int totalNumberOfMoves()
	{
		int sum = 0;
		for( int i=0;i < moveIterators.size();i++ ) {
			sum += getMoveIterator( i ).totalNumberOfMoves();
		}
		return sum;
	}

	public boolean isEmpty()
	{
		return totalNumberOfMoves()==0;
	}
	
	public void addMoveIterator( MoveIterator iter )
	{
		if( iterationStarted ) {
			throw new ConcurrentModificationException(
								"MoveIterator was added to CompositMoveIterator after iteration started" );
	} 
	  if( !iter.isEmpty() ) {
	  	moveIterators.add( iter );
	  }
	}
	
	private MoveIterator getMoveIterator( int index )
	{
		return (MoveIterator)moveIterators.get( index );
	}

}
