package board;

import junit.framework.TestCase;
import figures.*;
import helper.CheckStatus;
import helper.FigureIterator;
import helper.Position;
import image.*;

/**
 * @author stephan
 */
public class SimpleBoardTest extends TestCase
{
	private SimpleChessBoardInterface board;
	final private String initial = "Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 "+
	"Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rock-white-h1-0 "+
	"Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false Pawn-white-d2-false "+
	"Pawn-white-e2-false Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false "+
	"Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false Pawn-black-d7-false "+
	"Pawn-black-e7-false Pawn-black-f7-false Pawn-black-g7-false Pawn-black-h7-false "+
	"Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 "+
	"Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rock-black-h8-0";
	
	public SimpleBoardTest(String arg0)
	{
		super(arg0);
	}

	protected void setUp() throws Exception
	{
		board = new SimpleArrayBoard( new FigureImageFactoryMock("",true) );
	}
	
	public void testInit()
	{
		Figure figure = board.getFigure( Position.get("a1") );
		assertTrue( figure.isRock() );
		assertTrue( figure.isWhite() );
		
		figure = board.getFigure( Position.get("e8") );
		assertTrue( figure.isKing() );
		assertFalse( figure.isWhite() );
		
		figure = board.getFigure( Position.get("e3") );
		assertNull( figure );
	}
	
	public void testInit960()
	{
		board.init( 518 );
		assertEquals( initial,board.toString() );
		
		board.init(0);
		board.init(314);
		board.init(959);
		try {
			board.init(960);
			fail( "AssertionError should have been thrown:Argument bigger than 959" );
		}catch( AssertionError e) {}
		try {
			board.init(-1);
			fail( "AssertionError should have been thrown:Argument smaller than 0" );
		}catch( AssertionError e) {}
	}
	
	public void testIsFreeArea()
	{
		assertTrue(  board.isFreeArea( Position.get("a3")) );
		assertFalse( board.isFreeArea( Position.get("a2")) );
	}
	
	public void testSetFigure()
	{
		Figure figure = board.getFigure( Position.get("b1") );
		Position to = Position.get("e4");
		board.setFigure( to,figure );
		assertTrue( board.getFigure( to ).isKnight() );
	}
	
	public void testGetFigures()
	{
		board.setFigure( Position.get("c2"),null );
		FigureIterator iter = board.getFigures();
		assertEquals( 31,iter.countFigures() );
	}
	
	public void testGetCheckStatus()
	{
		CheckStatus status = board.getCheckStatus( true );
		assertFalse( status.isCheck() );
		
		String des = "white 0 King-white-e1-0 Rock-white-h2-1 Queen-black-b4";
		board.init( des );
		status = board.getCheckStatus( true );
		assertTrue( status.isCheck() );
		assertFalse( status.onlyKingCanMove() );
		assertEquals( 3,status.getPossiblePositions().countPositions() );
		
		des = "white 0 King-white-e1-3 Queen-black-f1 "
			 +"Knight-white-g4 Queen-black-e5 King-black-g7-3";
		board.init( des );
		status = board.getCheckStatus( true );
		assertTrue( status.isCheck() );
		assertTrue( status.onlyKingCanMove() );
	}

	public void testToString()
	{
		assertEquals( initial,board.toString() );
	}
}
