package board;

import junit.framework.TestCase;
import image.*;
import helper.*;
import figures.*;

/**
 * @author stephan
 */
public class ChessGameTest extends TestCase
{
  private ChessGame game;

	public ChessGameTest(String arg0)
	{
		super(arg0);
	}

	protected void setUp() throws Exception
	{
		game=new ChessGame( new DummyChessGameSupervisor(),new FigureImageFactoryMock("",true) );
	}
	
	public void testIsFreeArea()
	{
		assertTrue( game.isFreeArea( Position.get("a3") ) );
		assertFalse(game.isFreeArea( Position.get("c1") ) );
		assertFalse(game.isFreeArea( Position.get("d7") ) );
	}
	
	public void testGetFigure()
	{
		assertTrue( game.getFigure( Position.get("e3") )==null );
		Figure king = game.getFigure( Position.get("e1"));
		assertTrue( king instanceof King );
		assertTrue( king.isWhite() );
		Figure bishop = game.getFigure( Position.get("c8"));
		assertTrue( bishop instanceof Bishop );
		assertFalse( bishop.isWhite() );
	}
	
	public void testToString()
	{
		String code = "white 0 "
		             +"Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 "
		             +"Queen-white-d1 King-white-e1-0 "
		             +"Bishop-white-f1 Knight-white-g1 Rock-white-h1-0 "
		             +"Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false "
		             +"Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
		             +"Pawn-white-g2-false Pawn-white-h2-false "
								 +"Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false "
								 +"Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
								 +"Pawn-black-g7-false Pawn-black-h7-false "
								 +"Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 "
								 +"Queen-black-d8 King-black-e8-0 "
								 +"Bishop-black-f8 Knight-black-g8 Rock-black-h8-0";
		assertEquals( code,game.toString() );
	}
	
	public void testEquals()
	{
		ChessGame copy = new ChessGame( game.toString() );
		assertTrue( game.equals(copy) );
	}
	
	public void testCopy() throws Exception
	{
		ChessGame copy = (ChessGame)game.copyGame();
		assertTrue( game.equals(copy) );
	}
	
	public void testGetFigures()
	{
		String des      = "white 0 King-white-e1-0 Bishop-black-g2 Bishop-white-b2 "
										 +"Knight-white-c2 Knight-white-e7 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		assertEquals( 6,game.getFigures().countFigures() );
	}
	
	public void testMove()
	{
		String des     = "white 0 King-white-e1-0 Pawn-white-c2-false King-black-e8-0";
		ChessGame game = new ChessGame( des );
		Move move      = Move.get( Position.get("c2"),Position.get("c4") );
		game.move( move );
		String new_des = "black 1 King-white-e1-0 Pawn-white-c4-true King-black-e8-0";
		assertEquals( new_des,game.toString() );
	}
	
	public void testUndo()
	{
		String des     = "white 0 King-white-e1-0 Rock-white-h1-0 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		Position pos1  = Position.get("e1");
		Position pos2  = Position.get("h1");
		Move move      = Move.get( pos1,pos2 );
		game.move( move );
		String new_des = "black 1 Rock-white-f1-1 King-white-g1-1-true King-black-e8-0";
		
		assertEquals( new_des,game.toString() );
		game.undo();
		assertEquals( des,game.toString() );
		assertTrue( game.isMoveable(pos1,pos2,true) );
		
		
		des  = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-f8-0";
		game = new ChessGame( des );
		pos1 = Position.get("f8");
		pos2 = Position.get("g8");
		move = Move.get( pos1,pos2 );
		game.move( move );
		game.undo();
		
		assertEquals( des,game.toString() );
		assertTrue( game.isMoveable(pos1,pos2,false) );
		
		
		des     = "white 0 King-white-e1-0 Pawn-white-b5-false Pawn-black-c5-true King-black-e8-0";
		game = new ChessGame( des );
		pos1 = Position.get("b5");
		pos2 = Position.get("c6");
		move = Move.get(pos1,pos2);
		
		assertTrue( game.isMoveable(pos1,pos2,true) );
		game.move( move );
		game.undo();
		assertTrue( game.isMoveable(pos1,pos2,true) );
		
		
		des     = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-e8-0";
		game = new ChessGame( des );
		pos1 = Position.get("e8");
		pos2 = Position.get("a8");
		move = Move.get(pos1,pos2);
		
		assertTrue( game.isMoveable(pos1,pos2,false) );
		assertEquals( 16,game.getPossibleMoves().totalNumberOfMoves() );
		game.move( move );
		game.move( Move.get("e1-f1") );
		game.move( Move.get("d8-f8") );
		game.move( Move.get("f1-g1") );
		game.undo();
		game.undo();
		game.undo();
		game.undo();
		assertEquals( 16,game.getPossibleMoves().totalNumberOfMoves() );
		assertTrue( game.isMoveable(pos1,pos2,false) );
		
		
		des     = "white 4 Rock-white-a1-0 King-white-e1-0 Bishop-white-f1 "+
							"Rock-white-h1-0 Pawn-white-b2-false Queen-white-d2 "+
							"Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false "+
							"Pawn-white-a3-false Knight-white-c3 Knight-white-f3 "+
							"Pawn-white-c4-false Pawn-black-h4-false Pawn-white-e5-false "+
							"Bishop-black-a6 Pawn-black-b6-false Knight-black-c6 "+
							"Pawn-black-e6-false Pawn-black-h6-false Pawn-black-a7-false "+
							"Pawn-black-c7-false Pawn-black-d7-false Pawn-black-f7-false "+
							"Rock-black-a8-0 Queen-black-d8 King-black-e8-0 "+
							"Rock-black-h8-0";
		game = new ChessGame( des );
		pos1 = Position.get("e1");
		pos2 = Position.get("a1");
		move = Move.get(pos1,pos2);
		
		assertTrue( game.isMoveable(pos1,pos2,true) );
		game.move( move );
		game.undo();
		assertTrue( game.isMoveable(pos1,pos2,true) );
		
		
		des  = "black 1 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false";
		game = new ChessGame( des );
		move = Move.get("a6-b6");
		game.move( move );
		game.undo();
		
		assertEquals( des,game.toString() );
		
		
		des  = "white 1 King-white-h1-4 Pawn-white-a2-false King-black-a6-6";
		game = new ChessGame( des );
		move = Move.get("a2-a4");
		game.move( move );
		game.undo();
		
		assertEquals( des,game.toString() );
		
		
		des  = "white 0 King-white-h1-4 Pawn-white-a2-false Pawn-black-b4-false King-black-a6-6";
		game = new ChessGame( des );
		move = Move.get("a2-a4");
		game.move( move );
		String newDes = "black 1 King-white-h1-4 Pawn-white-a4-true Pawn-black-b4-false King-black-a6-6";
		
		move = Move.get("b4-a3");
		game.move( move );
		move = Move.get("h1-h2");
		game.move( move );
		move = Move.get("a3-a2");
		game.move( move );
		game.undo();
		game.undo();
		game.undo();
		
		assertEquals( newDes,game.toString() );
		
		move = Move.get("b4-b3");
		game.move( move );
		move = Move.get("h1-h2");
		game.move( move );
		move = Move.get("b3-b2");
		game.move( move );
		game.undo();
		game.undo();
		game.undo();
		
		assertEquals( newDes,game.toString() );
		
		
		des = "white 0 King-white-h1-3 King-black-b7-3 Pawn-white-c7-false";
		game = new ChessGame( des );
		move = Move.get("c7-c8");
		game.move( move );
		game.undo();
		
		assertEquals( des,game.toString() );
		
		
		game = new ChessGame( 621 );
		move = Move.get("f2-f3");
		game.move( move );
		move = Move.get("b7-b6");
		game.move( move );
		move = Move.get("g1-b6");
		game.move( move );
		move = Move.get("a7-a6");
		game.move( move );
		des = game.toString();
		move = Move.get("e1-f1");
		game.move( move );
		game.undo();
		assertEquals( des,game.toString() );
		move = Move.get("f1-f2");
		game.move( move );
		
		game = new ChessGame( 314 );
		des = game.toString();
		move = Move.get("d1-c1");
		game.move( move );
		game.undo();
		assertEquals( des,game.toString() );
		
		game=new ChessGame( 707 );
		game.move( Move.get("e1-f3") );
		game.move( Move.get("b7-b6") );
		game.move( Move.get("f1-e3") );
		game.move( Move.get("g7-g6") );
		game.move( Move.get("d1-e1") );
		game.move( Move.get("a8-f3") );
		des = game.toString();
		game.move( Move.get("c1-b1") );
		game.undo();
		assertEquals( des,game.toString() );
	}
	
	public void testHandleEnpasent()
	{
		String des     = "black 0 Pawn-white-c4-true Pawn-black-b4-false "
										+"King-white-e1-0 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		Move move      = Move.get( Position.get("b4"),Position.get("c3") );
		game.move( move );
		String new_des = "white 0 King-white-e1-0 Pawn-black-c3-false "
										+"King-black-e8-0";
		assertEquals( new_des,game.toString() );
	}
	
	public void testHandleRochade()
	{
		String des     = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-e8-0 ";
		ChessGame game = new ChessGame( des );
		Move move      = Move.get( Position.get("e8"),Position.get("a8") );
		game.move( move );
		String new_des = "white 1 King-white-e1-0 King-black-c8-1-true Rock-black-d8-1";
		assertEquals( new_des,game.toString() );
		
		des     = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-f8-0 ";
		game = new ChessGame( des );
		move      = Move.get( Position.get("f8"),Position.get("a8") );
		game.move( move );
		new_des = "white 1 King-white-e1-0 King-black-c8-1-true Rock-black-d8-1";
		assertEquals( new_des,game.toString() );
		
		des     = "white 0 King-white-e1-0 Rock-white-h1-0 King-black-e8-0 ";
		game = new ChessGame( des );
		move      = Move.get( Position.get("e1"),Position.get("h1") );
		game.move( move );
		new_des = "black 1 Rock-white-f1-1 King-white-g1-1-true King-black-e8-0";
		assertEquals( new_des,game.toString() );
		
		des     = "white 0 King-white-g1-0 Rock-white-h1-0 King-black-e8-0 ";
		game = new ChessGame( des );
		move      = Move.get( Position.get("g1"),Position.get("h1") );
		game.move( move );
		new_des = "black 1 Rock-white-f1-1 King-white-g1-1-true King-black-e8-0";
		assertEquals( new_des,game.toString() );
		
		des     = "white 0 King-white-f1-0 Rock-white-g1-0 King-black-e8-0 ";
		game = new ChessGame( des );
		move      = Move.get( Position.get("f1"),Position.get("g1") );
		game.move( move );
		new_des = "black 1 Rock-white-f1-1 King-white-g1-1-true King-black-e8-0";
		assertEquals( new_des,game.toString() );
	}
	
	public void testIsMatt()
	{
		String des     = "black 0 King-white-e1-0 Queen-black-h2 "
										+"Pawn-black-f3-false King-black-e8-0";
		ChessGame game = new ChessGame( des );
		int endoption = game.move( Move.get( "h2-e2" ) );
		assertTrue( endoption==ChessGameInterface.MATT );
	}
	
	public void testIsDrawBecauseOfNoMoves()
	{
		String des     = "black 0 King-white-e1-0 Queen-black-h2 "
										+"Pawn-black-c2-false Pawn-white-e7-false King-black-e8-0";
		ChessGame game = new ChessGame( des );
		int endoption = game.move( Move.get( "h2-g2" ) );
		assertTrue( endoption==ChessGameInterface.PATT );
	}

	public void testIsDrawBecauseOfLowMaterial()
	{
		String des     = "white 0 King-white-e1-0 Bishop-black-g2 "
										+"Knight-white-c2 Knight-white-e7 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		int endoption = game.move( Move.get( "e1-f2" ) );
		assertTrue( endoption==ChessGameInterface.DRAW );
	}
	
	public void testIsDrawBecauseOfThreeTimesSamePosition()
	{
		//Ich gehe davon aus, daß es ein Unterschied in der Stellung ist,
		//ob der König noch rockadefähig ist, oder nicht
		String des      = "white 0 King-white-e1-0 Bishop-black-g2 Bishop-white-b2 "
										 +"Knight-white-c2 Knight-white-e7 King-black-e8-0";
		ChessGame game  = new ChessGame( des );
		Move whiteMove  = Move.get( "c2-a1" );
		Move whiteReturn= Move.get( "a1-c2" );
		Move blackMove  = Move.get( "g2-h3" );
		Move blackReturn= Move.get( "h3-g2" );
		
		game.move( whiteMove );
		game.move( blackMove );
		game.move( whiteReturn );
		game.move( blackReturn );
		
		game.move( whiteMove );
		game.move( blackMove );
		int endoption;
		endoption = game.move( whiteReturn );
		assertFalse( endoption==ChessGameInterface.THREE_TIMES_SAME_POSITION );
		endoption = game.move( blackReturn );
		assertTrue( endoption==ChessGameInterface.THREE_TIMES_SAME_POSITION );
	}
	
	public void testIsDrawBecauseOf50HitlessMoves()
	{
		String des     = "white 98 King-white-e1-0 Pawn-white-a2-false "
										+"Pawn-black-b4-false King-black-e8-0";
		ChessGame game = new ChessGame( des );
		int endoption;
		endoption = game.move( Move.get("a2-a4") );
		assertFalse( endoption==ChessGameInterface.FIFTY_MOVES_NO_HIT );
		endoption = game.move( Move.get("b4-a3") );
		assertFalse( endoption==ChessGameInterface.FIFTY_MOVES_NO_HIT );
		
		des     = "white 98 King-white-e1-0 Pawn-white-a2-false "
							+"Pawn-black-b4-false King-black-e8-0";
		game = new ChessGame( des );
		endoption = game.move( Move.get("a2-a4") );
		assertFalse( endoption==ChessGameInterface.FIFTY_MOVES_NO_HIT );
		endoption = game.move( Move.get("b4-b3") );
		assertTrue( endoption==ChessGameInterface.FIFTY_MOVES_NO_HIT );
	}
	
	public void testHandleTrasformPawn()
	{
		MockChessGameSupervisor mock = new MockChessGameSupervisor( "Knight" );
		String des     = "black 0 King-white-e1-0 Pawn-black-g2-false "
										+"King-black-e8-0";
		ChessGame game = new ChessGame( des );
		game.setSupervisor( mock );
		game.move( Move.get( "g2-g1" ) );
		String new_des = "white 1 King-white-e1-0 Knight-black-g1 "
										+"King-black-e8-0";
		assertEquals( new_des,game.toString() );
	}
	
	public void testIsSelectable()
	{
		String des     = "white 0 King-white-e1-0 Queen-black-g2 "
										+"Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0";
		ChessGame game = new ChessGame( des );
		assertFalse( game.isSelectable( Position.get("e1"),true ) );
		assertTrue(  game.isSelectable( Position.get("e6"),true ) );
		assertFalse( game.isSelectable( Position.get("e6"),false ) );
		
		des =  "black 0 King-white-e1-0 Queen-white-e2 "
					+"Bishop-black-c8 King-black-e8-0";
		game= new ChessGame( des );
		assertTrue( game.isSelectable( Position.get("c8"),false ) );
		
		des =  "black 0 King-white-e1-0 Queen-white-e2 "
					+"Rock-black-a6-1 King-black-e8-0";
		game= new ChessGame( des );
		assertTrue( game.isSelectable( Position.get("a6"),false ) );
	}
	
	public void testIsMoveable()
	{
		String des     = "white 0 King-white-e1-0 Queen-black-g2 "
										+"Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0";
		ChessGame game = new ChessGame( des );
		

		assertTrue( game.isMoveable( Position.get("e6"),Position.get("e7"),true ) );
		
		des     = "black 0 King-white-e1-0 Pawn-black-a5-false "
						 +"King-black-g6-2 Rock-white-h6-1";
		game = new ChessGame( des );
		assertFalse( game.isMoveable( Position.get("a5"),Position.get("a4"),false ) );
		
		des     = "black 0 King-white-g7-6 King-black-e8-0 Rock-black-h8-0";
		game = new ChessGame( des );
		assertFalse( game.isMoveable( Position.get("e8"),Position.get("g8"),false ) );
		
		game = new ChessGame( 621 );
		game.move( Move.get("f2-f3") );
		game.move( Move.get("a7-a6") );
		Position from = Position.get( "f1" );
		Position to   = Position.get( "f2" );
		assertTrue( game.isMoveable( from,to,true ) );
		assertFalse( game.isFreeArea( from ) );
	}
	
	public void testColorChangedBetweenMoves()
	{
		String des     = "white 0 King-white-e1-0 Pawn-black-g3-false "
										+"King-black-e8-0";
		ChessGame game = new ChessGame( des );
		game.move( Move.get( "e1-d1" ) );
		try{
			game.move( Move.get( "d1-c1" ) );
			fail();
		}catch( AssertionError e ) {}
	}
	
	public void testChecksForMoveMovesFigureNotNull()
	{
		String des     = "white 0 King-white-e1-0 Pawn-black-g3-false "
										+"King-black-e8-0";
		ChessGame game = new ChessGame( des );
		try{
			game.move( Move.get( "a1-b1" ) );
			fail();
		}catch( AssertionError e ) {}
	}
	
	public void testHasHitBiggerFigure()
	{
		String des     = "white 0 King-white-h2-3 Queen-black-h3 "
										+"Pawn-white-g2-false Bishop-white-e7 King-black-e8-0 "
										+"Knight-black-g5 Pawn-white-a6-false";
		ChessGame game = new ChessGame( des );
		assertTrue( game.hasHitFigure() );			//da numberWithoutHit=0 ist in 'des'
		game.move( Move.get( "g2-h3" ) );
		assertTrue( game.hasHitFigure() );
		game.move( Move.get( "g5-h3" ) );
		assertTrue( game.hasHitFigure() );
		game.move( Move.get( "h2-h3" ) );
		assertTrue( game.hasHitFigure() );
		game.move( Move.get( "e8-e7" ) );
		assertTrue( game.hasHitFigure() );
		game.move( Move.get( "a6-a7" ) );
		assertFalse( game.hasHitFigure() );
		game.move( Move.get( "e7-d7" ) );
		assertFalse( game.hasHitFigure() );
	}
	
	public void testCountFigures()
	{
		String des     = "white 0 King-white-e1-0 Pawn-black-a5-true "
										+"Pawn-white-b5-false Pawn-white-e7-false King-black-e8-0";
		ChessGame game = new ChessGame( des );
		assertTrue( game.countFigures()==5 );
		game.move( Move.get( "b5-a6" ) );
		assertTrue( game.countFigures()==4 );
		game.move( Move.get( "e8-e7" ) );
		assertTrue( game.countFigures()==3 );
	}
	
	public void testGetPossibleMoves()
	{
		game.move(Move.get("g1-f3"));
		game.move(Move.get("b8-c6"));
		game.move(Move.get("f3-g1"));
		game.move(Move.get("c6-b4"));
		game.move(Move.get("g1-f3"));
		game.move(Move.get("b4-c2"));
		MoveIterator iter = game.getPossibleMoves( );
		assertEquals( 1,iter.totalNumberOfMoves() );
		
		String des     = "black 0 King-white-g1-2 Bishop-black-b6 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		game.move( Move.get("b6-c5") );
		iter = game.getPossibleMoves( );
		assertEquals( 4,iter.totalNumberOfMoves() );
		
		des = "black 0 Rock-white-a1-0 Rock-white-f1-1 King-white-g1-1-true "
		      +"Pawn-white-a2-false Pawn-white-b2-false Bishop-white-d2 Bishop-white-e2 "
		      +"Pawn-white-f2-false Pawn-white-h2-false Queen-white-b3 Pawn-white-g3-false "
		      +"Pawn-white-e4-false Pawn-black-b5-false Pawn-black-a6-false Bishop-black-b6 "
		      +"Pawn-black-h6-false Bishop-black-b7 Pawn-black-f7-false Pawn-black-g7-false "
		      +"Rock-black-c8-1 Queen-black-d8 Rock-black-f8-1 King-black-g8-1";
		 game = new ChessGame( des );
		 game.move( Move.get( "b6-f2" ) );
		 iter = game.getPossibleMoves( );
		 assertEquals( 4,iter.totalNumberOfMoves() );
		 
		des  = "black 0 Pawn-white-b2-false King-white-d3-2 Rock-black-h4-1 "+
					 "Rock-black-a8-0 King-black-e8-0";
		game = new ChessGame( des );
		game.move( Move.get( "a8-a3" ) );
		iter = game.getPossibleMoves( );
		
		assertEquals( 5,iter.totalNumberOfMoves() );
		
		des  = "black 0 King-white-d3-2 Knight-black-e5 Bishop-black-g8 King-black-e8-0";
		game = new ChessGame( des );
		game.move( Move.get( "g8-h7" ) );
		iter = game.getPossibleMoves( );
		
		assertEquals( 5,iter.totalNumberOfMoves() );
		
		des  = "white 2 Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 King-white-e1-0 "+
					 "Queen-white-d1 Bishop-white-f1 Knight-white-g1 Rock-white-h1-0 Pawn-white-a2-false "+
					 "Pawn-white-b2-false Pawn-white-d2-false Pawn-white-e2-false "+
					 "Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false "+
					 "Pawn-white-c3-false Pawn-black-d6-false Pawn-black-a7-false "+
					 "Pawn-black-b7-false Pawn-black-c7-false Pawn-black-e7-false Pawn-black-f7-false "+
					 "Pawn-black-g7-false Pawn-black-h7-false Rock-black-a8-0 Knight-black-b8 "+
					 "Bishop-black-c8 Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 "+
					 "Rock-black-h8-0";
		game = new ChessGame( des );
		game.move( Move.get( "d1-a4" ) );
		iter = game.getPossibleMoves( );
		
		assertEquals( 6,iter.totalNumberOfMoves() );
		
		des = "black 0 King-white-e1-0 Rock-white-d2-2 Queen-black-e2 "+
					"Bishop-black-b4 King-black-e8-0";
		game = new ChessGame( des );
		game.move( Move.get( "b4-c3" ) );
		iter = game.getPossibleMoves( );
		
		assertEquals( 1,iter.totalNumberOfMoves() );
		
		des     = "black 0 King-white-g1-2 Pawn-black-c4-false Pawn-white-d4-true "+
							"Bishop-black-b6 King-black-e8-0";
		game = new ChessGame( des );
		game.move( Move.get("c4-d3") );
		iter = game.getPossibleMoves();
		assertEquals( 4,iter.totalNumberOfMoves() );
		
		des     = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-e8-0";
		game = new ChessGame( des );
		game.move( Move.get("e8-a8") );
		iter = game.getPossibleMoves();
		assertEquals( 3,iter.totalNumberOfMoves() );
		
		des = "black 0 King-white-h1-3 Pawn-white-c7-false "
			 +"Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false "
			 +"Knight-black-a7 King-black-b7-3-false";
		game = new ChessGame( des );
		game.move( Move.get("b7-c6") );
		game.move( Move.get("c7-c8") );
		iter = game.getPossibleMoves();
		assertEquals( 1,iter.totalNumberOfMoves() );
		
		des     = "black 0 King-white-g7-6 King-black-e8-0 Rock-black-h8-0";
		game = new ChessGame( des );
		iter = game.getPossibleMoves();
		assertEquals( 12,iter.totalNumberOfMoves() );
		
		des     = "white 0 King-white-g6-6 Pawn-white-g7-false King-black-e8-0 Knight-black-h8";
		game = new ChessGame( des );
		iter = game.getPossibleMoves();
		assertEquals( 7,iter.totalNumberOfMoves() );
		
		des     = "white 0 Rock-white-b1-0 King-white-d1-0 Rock-white-e1-0 Bishop-black-d3 King-black-d8-0";
		game = new ChessGame( des );
		iter = game.getPossibleMoves();
		assertEquals( 22,iter.totalNumberOfMoves() );
		
		game=new ChessGame( 518 );
		game.move( Move.get("e2-e4") );
		game.move( Move.get("d7-d5") );
		game.move( Move.get("f1-b5") );
		game.move( Move.get("c7-c6") );
		game.move( Move.get("b5-c6") );
		game.move( Move.get("b8-d7") );
		game.move( Move.get("c6-b5") );
		iter = game.getPossibleMoves();
		assertEquals( 19,iter.totalNumberOfMoves() );
		
		game=new ChessGame( 621 );
		game.move( Move.get("g2-g3") );
		game.move( Move.get("f7-f6") );
		game.move( Move.get("c2-c3") );
		game.move( Move.get("g8-f7") );
		game.move( Move.get("d1-c2") );
		game.move( Move.get("e8-f8") );
		game.move( Move.get("c2-h7") );
		iter = game.getPossibleMoves();
		assertEquals( 1,iter.totalNumberOfMoves() );
		
		des = "white 0 Rock-black-e1-8 "
			 +"Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 "
			 +"Knight-white-g4 Queen-black-e5 King-black-g7-3";
		game = new ChessGame( des );
		game.move( Move.get( "f2-e1" ) );
		game.move( Move.get( "e2-f1" ) );
		iter = game.getPossibleMoves();
		assertEquals( 2,iter.totalNumberOfMoves() );
	}
	
	public void testIsCheck()
	{
		String des     = "white 0 King-white-g1-2 Bishop-black-f2 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		assertTrue( game.isCheck( true ) );
	}
	
	public void testGetHistory()
	{
		ChessGame game = new ChessGame( 621 );
		game.move( Move.get("g2-g3") );
		game.move( Move.get("f7-f6") );
		assertEquals( "f7-f6,g2-g3",game.getHistory() );
		game.move( Move.get("c2-c3") );
		game.move( Move.get("g8-f7") );
		assertEquals( "g8-f7,c2-c3,f7-f6,g2-g3",game.getHistory() );
		game.move( Move.get("d1-c2") );
		game.move( Move.get("a7-a6") );
		game.move( Move.get("c2-h7") );
		assertEquals( "c2-h7,a7-a6,d1-c2,g8-f7",game.getHistory() );
	}
}
