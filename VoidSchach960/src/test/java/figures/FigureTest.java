package figures;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import image.*;
import helper.*;
import board.*;

/**
 * @author stephan
 */
public class FigureTest extends TestCase
{
	private FigureImageMock figureImage = new FigureImageMock( 10,20,30 );
	
	public FigureTest(String arg0)
	{
		super(arg0);
	}
	
	public void testConstructor()
	{
		new MockFigure( figureImage,true,Position.get(0,0) );
	}
	
	public void testPaint()
	{
		Figure figure=new MockFigure( figureImage,true,Position.get(0,0) );
		figure.paint(null,10,20,30);
		figureImage.verify();
	}
	
	public void testIsDifferentColor()
	{
		Figure figure1=new MockFigure( figureImage,true,Position.get(0,0) );
		Figure figure2=new MockFigure( figureImage,true,Position.get(0,0) );
		Figure figure3=new MockFigure( figureImage,false,Position.get(0,0) );
		assertTrue( figure1.hasDifferentColor(figure3) );
		assertFalse( figure2.hasDifferentColor(figure1) );
		assertFalse( figure3.hasDifferentColor(figure3) );
	}
	
	public void testCanBeHitByEnpasent()
	{
		assertFalse( "should be false for all figures but pawns",
		  new MockFigure(	figureImage,
			  				true,
			  				Position.get(0,0)
			  			).canBeHitByEnpasent()
		);
	}
	
	public void testToString()
	{
		Position pos            = Position.get( "g4" );
		Figure figure1=new MockFigure( figureImage,true,pos );
		assertEquals("MockFigure-white-g4",figure1.toString());
		Figure figure2=new Bishop( figureImage,false,pos );
		assertEquals("Bishop-black-g4",figure2.toString() );
	}
	
	public void testSubtypes()
	{
		Position pos            = Position.get( "g4" );
		new Pawn( figureImage,true,pos );
		new Rock( figureImage,true,pos );
		new Knight( figureImage,true,pos );
		new Bishop( figureImage,true,pos );
		new Queen( figureImage,true,pos );
		new King( figureImage,true,pos );
	}
	
	public void testIsWhite()
	{
		Position pos            = Position.get( "g4" );
		
		Figure figure1 = new Rock( figureImage,true,pos );
		assertTrue( figure1.isWhite() );
		
		Figure figure2 = new Knight( figureImage,false,pos );
		assertFalse( figure2.isWhite() ); 
	}
	
	public void testFigureMoved()
	{
		Position from           = Position.get( "c1" );
		Position to             = Position.get( "g5" );
		Position thirdpos       = Position.get( "f5" );
		
		Figure figure1 = new Bishop( figureImage,true,from );
		figure1.figureMoved( Move.get( from, to ) );
		assertEquals( figure1.toString(),"Bishop-white-g5" );
		figure1.figureMoved( Move.get( thirdpos,from ) );
		assertEquals( figure1.toString(),"Bishop-white-g5" );
	}
	
	public void testRochade()
	{
		Position from           = Position.get( "c1" );
		Position to             = Position.get( "g5" );
		Move move               = Move.get(from,to);
		
		assertFalse( new Pawn(figureImage,true,from).canParticipateInRochade() );

		Figure king = new King(figureImage,true,from);
		assertTrue( "unmoved king",king.canParticipateInRochade() );
		king.figureMoved( move );
		assertFalse( "moved king",king.canParticipateInRochade() );
		
		Figure rock = new Rock(figureImage,false,from);
		assertTrue( "unmoved rock",rock.canParticipateInRochade() );
		rock.figureMoved( move );
		assertFalse( "moved rock",rock.canParticipateInRochade() );
	}
	
	public void testIsPassivetBound()
	{
		String des     = "white 0 King-white-e1-0 Rock-white-e3-4 Queen-black-e5";
		SimpleArrayBoard game = new SimpleArrayBoard( des );
		
		Position from           = Position.get( "e3" );
		Position to1            = Position.get( "e2" );
		Position to2            = Position.get( "e4" );
		Position to3            = Position.get( "e5" );
		Position to4            = Position.get( "e6" );
		Position to5            = Position.get( "d3" );

		Rock rock = new Rock(figureImage,true,from);
		
		assertFalse( rock.isPassiveBound(to1,game) );
		assertFalse( rock.isPassiveBound(to2,game) );
		assertFalse( rock.isPassiveBound(to3,game) );
		assertTrue(  rock.isPassiveBound(to4,game) );
		assertTrue(  rock.isPassiveBound(to5,game) );
	}
	
	public void testIsBound() throws Exception
	{
		String des = "white 0 King-white-e1-0 Rock-white-e3-4 Queen-black-e5";
		SimpleArrayBoard game = new SimpleArrayBoard( des );

		Position from1 = Position.get( "e3" );
		Position from2 = Position.get( "e1" );
		Position to1 = Position.get( "e2" );
		Position to2 = Position.get( "e4" );
		Position to3 = Position.get( "e5" );
		Position to4 = Position.get( "e6" );
		Position to5 = Position.get( "d3" );
		Position to6 = Position.get( "d2" );
		Position to7 = Position.get( "a1" );

		Rock rock = new Rock( figureImage,true,from1 );
		King king = new King( figureImage,true,from2 );

		assertFalse( rock.isBound( to1,game ) );
		assertFalse( rock.isBound( to2,game ) );
		assertFalse( rock.isBound( to3,game ) );
		assertTrue(  rock.isBound( to4,game ) );
		assertTrue(  rock.isBound( to5,game ) );
		assertFalse( king.isBound( to1,game ) );
		assertFalse( king.isBound( to6,game ) );
		
		des = "white 0 Rock-white-a1-0 King-white-e1-0 Rock-white-e3-4 Bishop-black-c3 Queen-black-e5";
		game.init( des );
		
		assertTrue(  rock.isBound( to1,game ) );
		assertTrue(  rock.isBound( to2,game ) );
		assertTrue(  rock.isBound( to3,game ) );
		assertTrue(  rock.isBound( to4,game ) );
		assertTrue(  rock.isBound( to5,game ) );
		assertFalse( king.isBound( to1,game ) );
		assertTrue(  king.isBound( to6,game ) );
		assertTrue(  king.isBound( to7,game ) );
		
		des = "white 0 Rock-white-a1-0 King-white-e1-0 Bishop-black-e2";
		game.init( des );
		
		assertFalse( king.isBound( to1,game ) );
		assertFalse(  king.isBound( to6,game ) );
		assertTrue(  king.isBound( to7,game ) );
	}
	
	public void testIsMoveable()
	{
		String des     = "white 0 King-white-e1-0 Rock-white-h2-1 Queen-black-h4";
		SimpleArrayBoard game = new SimpleArrayBoard( des );
		
		Position from           = Position.get( "h2" );
		Position to1            = Position.get( "f2" );
		Position to2            = Position.get( "h4" );
		Position to3            = Position.get( "g3" );
		Position to4            = Position.get( "e2" );

		Rock rock = new Rock(figureImage,true,from);
		
		assertTrue( rock.isMoveable(to1,game) );
		assertTrue( rock.isMoveable(to2,game) );
		assertFalse( rock.isMoveable(to3,game) );
		assertFalse( rock.isMoveable(to4,game) );
	}
	
	public void testGetTypeInfo()
	{
		Position from1 = Position.get( "e3" );
		Position from2 = Position.get( "e1" );

		Pawn pawn1 = new Pawn( figureImage,true,from1 );
		Pawn pawn2 = new Pawn( figureImage,true,from1 );
		Pawn pawn3 = new Pawn( figureImage,false,from1 );
		Pawn pawn4 = new Pawn( figureImage,true,from2 );
		
		assertTrue( pawn1.getTypeInfo()==pawn2.getTypeInfo() );	//gleiche Objekte sollten die gleiche TypeInfo haben
		assertTrue( pawn1.getTypeInfo()!=pawn3.getTypeInfo() ); //unterschiedliche TypeInfo bei unterschiedliche Farbe
		assertTrue( pawn1.getTypeInfo()==pawn4.getTypeInfo() ); //Position geht nicht mit ein
		
		Rock   rock   = new Rock( figureImage,true,from1 );
		Knight knight = new Knight( figureImage,true,from1 );
		Bishop bishop = new Bishop( figureImage,true,from1 );
		Queen  queen  = new Queen( figureImage,true,from1 );
		King   king   = new King( figureImage,true,from2 );
		
		Byte pawnByte   = new Byte(  pawn1.getTypeInfo() );
		Byte rockByte   = new Byte(   rock.getTypeInfo() );
		Byte knightByte = new Byte( knight.getTypeInfo() );
		Byte bishopByte = new Byte( bishop.getTypeInfo() );
		Byte queenByte  = new Byte(  queen.getTypeInfo() );
		Byte kingByte   = new Byte(   king.getTypeInfo() );
		
		List figureByteList = new LinkedList();
		//Die byte-Werte müssen paarweise disjunkt sein
		figureByteList.add( pawnByte );
		assertFalse(	"Bytewert sollte noch nicht in der Liste sein",
						figureByteList.contains( rockByte )
		);
		figureByteList.add( rockByte );
		assertFalse(	"Bytewert sollte noch nicht in der Liste sein",
						figureByteList.contains( knightByte )
		);
		figureByteList.add( knightByte );
		assertFalse(	"Bytewert sollte noch nicht in der Liste sein",
						figureByteList.contains( bishopByte )
		);
		figureByteList.add( bishopByte );
		assertFalse(	"Bytewert sollte noch nicht in der Liste sein",
						figureByteList.contains( queenByte )
		);
		figureByteList.add( queenByte );
		assertFalse(	"Bytewert sollte noch nicht in der Liste sein",
						figureByteList.contains( kingByte )
		);
	}

	public static MoveIterator getPossibleMovesFrom( ChessGameInterface game,Position from )
	{
		BasicMoveIterator moveIter;
		moveIter = new BasicMoveIterator();
		MoveIterator allMoves = game.getPossibleMoves();
		while( allMoves.hasMoreMoves() ) {
			Move move = allMoves.nextMove();
			if( move.from.equalsPosition( from ) ) {
				moveIter.addMove( move );
			}
		}
		return moveIter;
	}
}
