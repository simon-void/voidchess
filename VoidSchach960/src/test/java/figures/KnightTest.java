package figures;

import junit.framework.TestCase;
import image.*;
import board.*;
import helper.*;

/**
 * @author stephan
 */
public class KnightTest extends TestCase
{
	public KnightTest(String arg0)
	{
		super(arg0);
	}
	
	public void testIsReachable()
	{
		String des     = "black 0 Pawn-white-c2-false Knight-black-d4 "
										+"Knight-black-f5";
		ChessGame game = new ChessGame( des );
		
		FigureImage figureImage = new FigureImageMock(10,20,30);
		Position from           = Position.get( "d4" );
		Position to1            = Position.get( "b3" );
		Position to2            = Position.get( "b5" );
		Position to3            = Position.get( "c6" );
		Position to4            = Position.get( "e6" );
		Position to5            = Position.get( "c2" );
		Position to6            = Position.get( "f3" );
		Position to7            = Position.get( "d1" );
		Position to8            = Position.get( "f5" );
    
		Knight knight = new Knight(figureImage,false,from);
		assertTrue( knight.isReachable(to1,game) );
		assertTrue( knight.isReachable(to2,game) );
		assertTrue( knight.isReachable(to3,game) );
		assertTrue( knight.isReachable(to4,game) );
		assertTrue( knight.isReachable(to5,game) );
		assertTrue( knight.isReachable(to6,game) );
		assertFalse( knight.isReachable(to7,game) );
		assertFalse( knight.isReachable(to8,game) );
		assertFalse( knight.isReachable(from,game) );
	}
	
	public void testGetPossibleMoves()
	{
		String des     = "white 0 Knight-white-a1 King-white-e1-0 "
										+"Knight-white-g6";
		SimpleArrayBoard game = new SimpleArrayBoard( des );
    
		Figure knight1 = game.getFigure( Position.get( "a1" ) );
		BasicMoveIterator moveIter1 = new BasicMoveIterator();
		knight1.getPossibleMoves(game,moveIter1);
		assertEquals( moveIter1.totalNumberOfMoves(),2 );
		Figure knight2 = game.getFigure( Position.get( "g6" ) );
		BasicMoveIterator moveIter2 = new BasicMoveIterator();
		knight2.getPossibleMoves(game,moveIter2);
		assertEquals( moveIter2.totalNumberOfMoves(),6 );
	}
}
