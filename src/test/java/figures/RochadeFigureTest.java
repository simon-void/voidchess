/*
 * Created on 09.10.2006
 */

package figures;

import helper.*;
import image.*;

import junit.framework.TestCase;

public class RochadeFigureTest
extends TestCase
{
	public void testGetInitialPosition()
	{
		Position initial = Position.get( "e1" );
		Position second  = Position.get( "d1" );
		
		FigureImage figureImage = new FigureImageMock(10,20,30);
		King king = new King( figureImage,true,initial );
		
		assertEquals( king.getInitialPosition(),initial );
		king.figureMoved( Move.get( initial,second ) );
		assertEquals( king.getInitialPosition(),initial );
	}

}
