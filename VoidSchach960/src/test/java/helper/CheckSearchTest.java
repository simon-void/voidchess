package helper;

import java.util.List;

import board.SimpleArrayBoard;
import junit.framework.TestCase;

/**
 * @author stephan
 */
public class CheckSearchTest extends TestCase
{
	public CheckSearchTest(String arg0)
	{
		super(arg0);
	}
	
	public void testSignum()
	{
		assertEquals(  1,CheckSearch.signum(  4 ) );
		assertEquals(  0,CheckSearch.signum(  0 ) );
		assertEquals( -1,CheckSearch.signum(-34 ) );
	}
	
	public void testAreDiagonalPositions()
	{
		Position pos1 = Position.get( "a1" );
		Position pos2 = Position.get( "a8" );
		Position pos3 = Position.get( "h1" );
		
		assertTrue( CheckSearch.areDiagonalPositions( pos2,pos3 ) );
		assertTrue( CheckSearch.areDiagonalPositions( pos3,pos2 ) );
		assertFalse(CheckSearch.areDiagonalPositions( pos1,pos2 ) );
		assertFalse(CheckSearch.areDiagonalPositions( pos1,pos3 ) );
	}
	
	public void testAreStraightPositions()
	{
		Position pos1 = Position.get( "a1" );
		Position pos2 = Position.get( "a8" );
		Position pos3 = Position.get( "h1" );
		
		assertFalse(CheckSearch.areStraightPositions( pos2,pos3 ) );
		assertFalse(CheckSearch.areStraightPositions( pos3,pos2 ) );
		assertTrue( CheckSearch.areStraightPositions( pos1,pos2 ) );
		assertTrue( CheckSearch.areStraightPositions( pos1,pos3 ) );
	}
	
	public void testAnalyseCheck()
	{
		String des = "white 0 King-white-e4-4 King-black-e5-4";
		SimpleArrayBoard game = new SimpleArrayBoard( des );
		CheckStatus status = CheckSearch.analyseCheck( game,true );
		
		assertTrue( status.isCheck() );
		assertFalse( status.onlyKingCanMove() );
		List<Position> possiblePositions = status.getPossiblePositions();
		assertEquals(1, possiblePositions.size() );
		assertTrue( possiblePositions.get(0).equalsPosition( Position.get( "e5" ) ) );
		
		des = "white 0 King-white-e4-4 King-black-e6-4";
		game.init( des );
		status = CheckSearch.analyseCheck( game,true );
		
		assertFalse( status.isCheck() );
		assertFalse( status.onlyKingCanMove() );
		
		des = "black 0 King-white-e1-0 Queen-white-e2 Knight-white-d6 King-black-e8-0";
		game.init( des );
		status = CheckSearch.analyseCheck( game,false );
		
		assertTrue( status.isCheck() );
		assertTrue( status.onlyKingCanMove() );
		
		des = "black 0 King-white-e1-0 Queen-white-e6 Knight-white-c6 King-black-e8-0";
		game.init( des );
		status = CheckSearch.analyseCheck( game,false );
		
		assertTrue( status.isCheck() );
		assertFalse( status.onlyKingCanMove() );
		possiblePositions = status.getPossiblePositions();
		assertEquals( 2, possiblePositions.size() );
		assertTrue( possiblePositions.contains( Position.get("e6") ) );
		assertTrue( possiblePositions.contains( Position.get("e7") ) );
		
		des = "white 0 King-white-e1-0 Rock-white-h2-1 Queen-black-h4";
		game.init( des );
		status = CheckSearch.analyseCheck( game,true );
		
		assertTrue( status.isCheck() );
		assertFalse( status.onlyKingCanMove() );
		possiblePositions = status.getPossiblePositions();
		assertEquals( 3, possiblePositions.size() );
		assertTrue( possiblePositions.contains( Position.get("h4") ) );
		assertTrue( possiblePositions.contains( Position.get("g3") ) );
		assertTrue( possiblePositions.contains( Position.get("f2") ) );
	}
}
