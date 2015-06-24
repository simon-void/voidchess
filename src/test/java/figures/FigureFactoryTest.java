package figures;

import junit.framework.TestCase;
import image.*;
import helper.*;

/**
 * @author stephan
 */
public class FigureFactoryTest extends TestCase
{

	public FigureFactoryTest(String arg0)
	{
		super(arg0);
	}

  public void testConstructor()
	{
		new FigureFactory( new FigureImageFactoryMock("notest",true) );
	}
	
	public void testGet()
	{
		Position pos = Position.get( "b3" );
		
		FigureImageFactoryMock figureImageFactory = new FigureImageFactoryMock( "King",true );
		new FigureFactory( figureImageFactory ).getKing(pos,true);
		figureImageFactory.verify();
		
		figureImageFactory = new FigureImageFactoryMock( "Queen",true );
		new FigureFactory( figureImageFactory ).getQueen(pos,true);
		figureImageFactory.verify();
		
		figureImageFactory = new FigureImageFactoryMock( "Bishop",true );
		new FigureFactory( figureImageFactory ).getBishop(pos,true);
		figureImageFactory.verify();
		
		figureImageFactory = new FigureImageFactoryMock( "Knight",false );
		new FigureFactory( figureImageFactory ).getKnight(pos,false);
		figureImageFactory.verify();
		
		figureImageFactory = new FigureImageFactoryMock( "Rock",false );
		new FigureFactory( figureImageFactory ).getRock(pos,false);
		figureImageFactory.verify();
		
		figureImageFactory = new FigureImageFactoryMock( "Pawn",false );
		new FigureFactory( figureImageFactory ).getPawn(pos,false);
		figureImageFactory.verify();
	}
	
	public void testSetFigureFactory()
	{
		try
		{
			Position pos = Position.get( "b3" );
		
			FigureImageFactoryMock figureImageFactory = new FigureImageFactoryMock( "King",true );
			FigureFactory figureFactory= new FigureFactory( figureImageFactory );
			figureFactory.setFigureImageFactory( null );
			figureFactory.getBishop( pos,true );
			fail( "unused FigureImageFactory" );
		} catch (NullPointerException e){}
	}
	
	public void testGetFigureByString()
	{
		FigureImageFactoryMock figureImageFactory = new FigureImageFactoryMock( "King",true );
		FigureFactory figureFactory= new FigureFactory( figureImageFactory );

		String king_des  ="King-white-e1-0";
		String rock_des  ="Rock-black-h8-2";
		String pawn_des  ="Pawn-white-e4-true";
		String queen_des ="Queen-black-d8";
		
		Figure king = figureFactory.getFigureByString( king_des );
		assertTrue( king instanceof King );
		assertTrue( king.isWhite() );
		assertTrue( king.canParticipateInRochade() );
		
		Figure rock = figureFactory.getFigureByString( rock_des );
		assertTrue(  rock instanceof Rock );
		assertFalse( rock.isWhite() );
		assertFalse( rock.canParticipateInRochade() );
		
		Figure pawn = figureFactory.getFigureByString( pawn_des );
		assertTrue( pawn instanceof Pawn );
		assertTrue( pawn.isWhite() );
		assertTrue( pawn.canBeHitByEnpasent() );
		
		Figure queen = figureFactory.getFigureByString( queen_des );
		assertTrue( queen instanceof Queen );
		assertFalse( queen.isWhite() );
	}
}
