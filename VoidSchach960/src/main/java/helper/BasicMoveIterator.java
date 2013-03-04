package helper;

import java.util.*;
/**
 * @author stephan
 */
public class BasicMoveIterator implements MoveIterator
{
	private ArrayQueue moves;
	private boolean nextStarted;

	public BasicMoveIterator()
	{
		moves = new ArrayQueue( 32 );
		nextStarted = false;
	}

	public boolean hasMoreMoves()
	{
		return moves.size()!=0;
	}

	public Move nextMove()
	{
		nextStarted = true;
		return (Move)moves.popFirst();
	}

	public int totalNumberOfMoves()
	{
		return moves.size();
	}

	public boolean isEmpty()
	{
		return moves.size()==0;
	}
	
	public void clear()
	{
		moves.clear();
		nextStarted = false;
	}
	
	public void addMove( Move move )
	{
		if( nextStarted ) {
			throw new ConcurrentModificationException(
                "Move was added to BasicMoveIterator after iteration" );
		} 
		moves.putLast( move );
	}

}
