package figures;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import image.*;
import board.*;
import helper.*;

import static org.mockito.Mockito.mock;
/**
 * @author stephan
 */
public class RockTest extends TestCase
{
	public RockTest(String arg0)
	{
		super(arg0);
	}
	
	public void testIsReachable()
	{
		String des     = "white 0 Pawn-white-c2-false Rock-white-c4-2 "
		                +"Knight-black-g4";
		ChessGame game = new ChessGame( des );
		
		FigureImage figureImage = new FigureImageMock(10,20,30);
		Position from           = Position.get( "c4" );
		Position to1            = Position.get( "c3" );
		Position to2            = Position.get( "g4" );
		Position to3            = Position.get( "c2" );
		Position to4            = Position.get( "h4" );
		Position to5            = Position.get( "d5" );
    
		Rock rock = new Rock(figureImage,true,from);
		assertTrue( rock.isReachable(to1,game) );
		assertTrue( rock.isReachable(to2,game) );
		assertFalse( rock.isReachable(to3,game) );
		assertFalse( rock.isReachable(to4,game) );
		assertFalse( rock.isReachable(to5,game) );
		assertFalse( rock.isReachable(from,game) );
	}
	
	public void testUndoMove()
	{
		FigureImageFactoryMock figureImageFactory = new FigureImageFactoryMock( "Rock",true );
		FigureFactory     figureFactory = new FigureFactory( figureImageFactory );
		Rock rock = (Rock)figureFactory.getRock( Position.get("a1"),false );
		
		assertTrue( rock.canParticipateInRochade() );
		rock.figureMoved( Move.get("a1-b1") );
		assertFalse( rock.canParticipateInRochade() );
		rock.undoMove( Position.get("a1") );
		assertTrue( rock.canParticipateInRochade() );
	}
	
	public void testGetPossibleMoves()
	{
		String des     = "white 0 King-white-e2-0 Pawn-black-b4-false "
										+"Rock-white-e4-2 King-black-e8-0";
		SimpleArrayBoard game = new SimpleArrayBoard( des, mock(LastMoveProvider.class) );
    
		Figure rock = game.getFigure( Position.get( "e4" ) );
		List<Move> moveIter = new LinkedList<>();
		rock.getPossibleMoves(game,moveIter);
		assertEquals( moveIter.size(),11 );
	}
	
}
