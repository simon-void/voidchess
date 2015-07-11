package figures;

import java.util.LinkedList;
import java.util.List;

import image.*;
import board.*;
import helper.*;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;
import org.testng.annotations.*;
/**
 * @author stephan
 */
public class BishopTest
{	
  @Test
	public void testIsReachable()
	{
		String des     = "white 0 Pawn-white-c2-false Bishop-white-d3- "
										+"Knight-black-b5";
		ChessGame game = new ChessGame( des );

		Position from           = Position.get( "d3" );
		Position to1            = Position.get( "b5" );
		Position to2            = Position.get( "h7" );
		Position to3            = Position.get( "f1" );
		Position to4            = Position.get( "c2" );
		Position to5            = Position.get( "a6" );
		Position to6            = Position.get( "b1" );
		Position to7            = Position.get( "d4" );
    
		Bishop bishop = new Bishop(true,from);
		assertTrue( bishop.isReachable(to1,game) );
		assertTrue( bishop.isReachable(to2,game) );
		assertTrue( bishop.isReachable(to3,game) );
		assertFalse( bishop.isReachable(to4,game) );
		assertFalse( bishop.isReachable(to5,game) );
		assertFalse( bishop.isReachable(to6,game) );
		assertFalse( bishop.isReachable(to7,game) );
		assertFalse( bishop.isReachable(from,game) );
	}

  @Test
	public void testGetPossibleMoves()
	{
		String des     = "white 0 Bishop-white-b1 King-white-e1-0 "
										+"Knight-white-f7";
		SimpleArrayBoard game = new SimpleArrayBoard( des, mock(LastMoveProvider.class) );
    
		Figure bishop = game.getFigure( Position.get( "b1" ) );
		List<Move> moveIter = new LinkedList<>();
		bishop.getPossibleMoves(game,moveIter);
		assertEquals( moveIter.size(),7 );
	}
}
