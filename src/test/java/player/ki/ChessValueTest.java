/*
 * Created on 03.10.2006
 */

package player.ki;

import static org.testng.Assert.*;
import org.testng.annotations.*;

public class ChessValueTest
{
	private ChessValue wrapper = ChessValue.getInstance();
	
	@Test
	public void testChessvalueToString()
	{
		float value = wrapper.getFloatValue( -1 );
		assertEquals( "-1,00",wrapper.chessvalueToString( value ) );
		value = wrapper.getFloatValue( 5.49999999999f );
		assertEquals( "5,50",wrapper.chessvalueToString( value ) );
		
		value = wrapper.getDrawValue();
		assertEquals( "0,00",wrapper.chessvalueToString( value ) );
		
		value = wrapper.getDrawValue();
		assertEquals( "0,00",wrapper.chessvalueToString( value ) );
		
		value = wrapper.getOtherPlayerIsMatt( 2 );
		assertEquals( "ich bin matt in 2",wrapper.chessvalueToString( value ) );
		
		value = wrapper.getThisComputerPlayerIsMatt( 12 );
		assertEquals( "du bist matt in 12",wrapper.chessvalueToString( value ) );
	}

  @Test
	public void testHasAlmostSameValue()
	{
		assertTrue(
			wrapper.hasAlmostSameValue(
				wrapper.getFloatValue(-0.1f),
				wrapper.getFloatValue(0.1f)
			)
		);
		assertTrue(
			wrapper.hasAlmostSameValue(
				wrapper.getDrawValue(),
				wrapper.getFloatValue(-0.2f)
			)
		);
		assertFalse(
			wrapper.hasAlmostSameValue(
				wrapper.getFloatValue(-0.1f),
				wrapper.getFloatValue(0.2f)
			)
		);
		assertFalse(
			wrapper.hasAlmostSameValue(
				wrapper.getFloatValue(-0.2f),
				wrapper.getFloatValue(0.2f)
			)
		);
		assertFalse(
			wrapper.hasAlmostSameValue(
				wrapper.getFloatValue(0.2f),
				wrapper.getFloatValue(0.5f)
			)
		);
		assertFalse(
			wrapper.hasAlmostSameValue(
				wrapper.getThisComputerPlayerIsMatt(1),
				wrapper.getThisComputerPlayerIsMatt(2)
			)
		);
		assertFalse(
			wrapper.hasAlmostSameValue(
				wrapper.getThisComputerPlayerIsMatt(1),
				wrapper.getOtherPlayerIsMatt(1)
			)
		);
		assertTrue(
			wrapper.hasAlmostSameValue(
				wrapper.getOtherPlayerIsMatt(1),
				wrapper.getOtherPlayerIsMatt(1)
			)
		);
		assertFalse(
			wrapper.hasAlmostSameValue(
				wrapper.getDrawValue(),
				wrapper.getOtherPlayerIsMatt(1)
			)
		);
	}
}
