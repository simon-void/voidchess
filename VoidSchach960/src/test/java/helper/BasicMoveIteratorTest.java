package helper;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import junit.framework.TestCase;
/**
 * @author stephan
 */
public class BasicMoveIteratorTest extends TestCase
{
	private BasicMoveIterator iter;

	public BasicMoveIteratorTest(String arg0)
	{
		super(arg0);
	}

	protected void setUp() throws Exception
	{
		iter = new BasicMoveIterator();
	}
	
	public void testIsEmpty()
	{
		assertTrue( iter.isEmpty() );
	}
	
	public void testAddMove()
	{
		iter.addMove( Move.get("a1-a2") );
		assertFalse( iter.isEmpty() );
		
		iter.nextMove();
		try{
			iter.addMove( Move.get("d1-d8") );
			fail( "ConcurrentModificationException should have been thrown" );
		}catch( ConcurrentModificationException e ) {}
	}
	
	public void testTotalNumberOfMoves()
	{
		assertTrue( 0==iter.totalNumberOfMoves() );
		iter.addMove( Move.get("a1-a2") );
		assertTrue( 1==iter.totalNumberOfMoves() );
		iter.addMove( Move.get("c1-b2") );
		assertTrue( 2==iter.totalNumberOfMoves() );
	}
	
	public void testHasMoreMoves()
	{
		assertFalse( iter.hasMoreMoves() );
		iter.addMove( Move.get("a1-a2") );
		assertTrue( iter.hasMoreMoves() );
	}
	
	public void testNextMove()
	{
		iter.addMove( Move.get( "e8-d8") );
		assertTrue( iter.nextMove().equalsMove( Move.get( "e8-d8") ) );
		
		try{
  		iter.nextMove();
  		fail( "NoSuchElementException should have been thrown" );
		}catch( NoSuchElementException e ) {}
	}

}
