package figures;

import junit.framework.TestCase;
import image.*;
import board.*;
import helper.*;
/**
 * @author stephan
 */
public class QueenTest extends TestCase
{
	public QueenTest(String arg0)
	{
		super(arg0);
	}
	
	public void testIsReachable()
	{
		String des     = "white 0 Queen-white-e2";
		ChessGame game = new ChessGame( des );
		
		FigureImage figureImage = new FigureImageMock(10,20,30);
		Position from           = Position.get( "e2" );
		Position to1            = Position.get( "e5" );
		Position to2            = Position.get( "c4" );
		Position to3            = Position.get( "d4" );
    
		Queen queen = new Queen(figureImage,true,from);
		assertTrue( queen.isReachable(to1,game) );
		assertTrue( queen.isReachable(to2,game) );
		assertFalse( queen.isReachable(to3,game) );
		assertFalse( queen.isReachable(from,game) );
	}
	
	public void testGetPossibleMoves()
	{
		String des     = "white 0 Queen-white-d1 King-white-e1-0 ";
		SimpleArrayBoard game = new SimpleArrayBoard( des );
    
		Figure queen = game.getFigure( Position.get( "d1" ) );
		BasicMoveIterator moveIter = new BasicMoveIterator();
		queen.getPossibleMoves(game,moveIter);
		assertEquals( moveIter.totalNumberOfMoves(),17 );
	}

}
