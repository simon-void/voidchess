package helper;

import figures.Pawn;
import junit.framework.TestCase;

/**
 * @author stephan
 */
public class ExtendedMoveTest extends TestCase
{
	private ExtendedMove extendedMove;

	public ExtendedMoveTest(String arg0)
	{
		super(arg0);
	}
	
	protected void setUp() throws Exception
	{
		extendedMove = 
			new ExtendedMove( 
				Position.get("a1"),
				Position.get("d1"),
				null,
				null,
				true,
				false,
				false,
				false );
	}
	
	public void testFrom()
	{
		assertTrue( extendedMove.from.equalsPosition( Position.get("a1")) );
	}
	
	public void testTo()
	{
		assertTrue( extendedMove.to.equalsPosition( Position.get("d1")) );
	}
	
	public void testGetFigure()
	{
		assertTrue( extendedMove.getFigure()==null );
	}
	
	public void testGetColorOfMove()
	{
		assertTrue( extendedMove.getColorOfMove() );
	}
	
	public void testIsRochade()
	{
		assertFalse( extendedMove.isRochade() );
	}
	
	public void testIsEnpassent()
	{
		assertFalse( extendedMove.isEnpassent() );
	}

	public void testHasHitFigure()
	{
		assertFalse( extendedMove.hasHitFigure() );
		ExtendedMove hitPawn = 
			new ExtendedMove( 
				Position.get( "b5" ),
				Position.get( "a6" ),
				null,
				new Pawn( null,false,Position.get( "a5" ),true ),
				true,
				false,
				true,
				false
			);
		assertTrue( hitPawn.hasHitFigure() );
		ExtendedMove hitFigure = 
			new ExtendedMove( 
				Position.get( "b5" ),
				Position.get( "a6" ),
				new Pawn( null,false,Position.get( "a6" ),false ),
				null,
				true,
				false,
				true,
				false );
		assertTrue( hitFigure.hasHitFigure() );
	}
}
