package helper;

import java.util.ConcurrentModificationException;

/**
 * @author stephan
 */
public class BasicPositionIterator implements PositionIterator
{
	private ArrayQueue positions;
	private boolean nextStarted;
	
	public BasicPositionIterator()
	{
		this( 16 );
	}
	
	public BasicPositionIterator( int size )
	{
		positions = new ArrayQueue( size );
		nextStarted = false;
	}
	
	public void addPosition( Position pos )
	{
		if( nextStarted ) {
			throw new ConcurrentModificationException(
                "Position was added to BasicPositionIterator after iteration" );
		} 
		positions.putLast( pos ); 
	}
	
	public void clear()
	{
		positions.clear();
		nextStarted = false;
	}
	
	public Position next()
	{
		nextStarted = true;
		return (Position)positions.popFirst();
	}
	
	public boolean hasNext()
	{
		return positions.size()!=0;
	}
	
	public int countPositions()
	{
		return positions.size();
	}
	
	public boolean isEmpty()
	{
		return positions.size()==0;
	}
	
	public boolean contains( Position other )
	{
		return positions.contains( other );
	}
}
