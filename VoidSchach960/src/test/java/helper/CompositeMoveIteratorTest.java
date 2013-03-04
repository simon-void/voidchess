package helper;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import junit.framework.TestCase;

/**
 * @author stephan
 */
public class CompositeMoveIteratorTest extends TestCase
{

	private CompositeMoveIterator moveIterator;
	private BasicMoveIterator     emptyIterator;
	private BasicMoveIterator     oneMoveIterator;

	public CompositeMoveIteratorTest(String arg0)
	{
		super(arg0);
	}

	protected void setUp() throws Exception
	{
		moveIterator = new CompositeMoveIterator();
		emptyIterator = new BasicMoveIterator();
		oneMoveIterator = new BasicMoveIterator();
		oneMoveIterator.addMove( Move.get( "e1-g1" ) );
	}
	
	public void testIsEmpty()
	{
		assertTrue( moveIterator.isEmpty() );
		moveIterator.addMoveIterator( emptyIterator );
		assertTrue( moveIterator.isEmpty() );
	}
	
	public void testAddMoveIterator()
	{
		moveIterator.addMoveIterator( oneMoveIterator );
		assertFalse( moveIterator.isEmpty() );
		
		moveIterator.nextMove();
		try{
			moveIterator.addMoveIterator( emptyIterator );
			fail( "ConcurrentModificationException should have been thrown" );
		}catch( ConcurrentModificationException e ) {}
	}
	
	public void testTotalNumberOfMoves()
	{
		assertTrue( 0==moveIterator.totalNumberOfMoves() );
		moveIterator.addMoveIterator( emptyIterator );
		assertTrue( 0==moveIterator.totalNumberOfMoves() );
		moveIterator.addMoveIterator( oneMoveIterator );
		assertTrue( 1==moveIterator.totalNumberOfMoves() );
	}
	
	public void testHasMoreMoves()
	{
		CompositeMoveIterator emptyCompositeMoveIterator = new CompositeMoveIterator();
		assertFalse( emptyCompositeMoveIterator.hasMoreMoves() );
		
		moveIterator.addMoveIterator( new BasicMoveIterator() );
		moveIterator.addMoveIterator( new BasicMoveIterator() );
		moveIterator.addMoveIterator( new BasicMoveIterator() );
		moveIterator.addMoveIterator( oneMoveIterator );
		assertTrue( moveIterator.hasMoreMoves() );
		moveIterator.nextMove();
		assertFalse( moveIterator.hasMoreMoves() );
		
	}
	
	public void testNextMove()
	{
		moveIterator.addMoveIterator( new BasicMoveIterator() );
		moveIterator.addMoveIterator( new BasicMoveIterator() );
		moveIterator.addMoveIterator( new BasicMoveIterator() );
		BasicMoveIterator iter = new BasicMoveIterator();
		iter.addMove( Move.get( "e8-d8") );
		moveIterator.addMoveIterator( iter );
		assertTrue( moveIterator.nextMove().equalsMove( Move.get( "e8-d8") ) );
		
		try{
			moveIterator.nextMove();
			fail( "NoSuchElementException should have been thrown" );
		}catch( NoSuchElementException e ) {}
	}


}
